package com.github.ridesmart.entities;

import android.location.Location;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "turns")
public class Turn {

    public enum TurnDirection {LEFT, RIGHT, UNDEFINED}

    @PrimaryKey (autoGenerate = true)
    private long turnId;

    // RouteId foreign key - identifies this turn as belonging to a specific route
    private long routeCreatorId;

    @Ignore
    // Establishes max turn bearing at 0.200s for a typical vehicle. Default is 45 (degrees).
    private final static float MAX_TURN_BEARING = 180;

    @Ignore
    private List<Location> turnPoints;

    @Embedded(prefix = "init_")
    private Coordinate initialTurnPosition;
    @Embedded(prefix = "mid_")
    private Coordinate middleTurnPosition;
    @Embedded(prefix = "final_")
    private Coordinate finalTurnPosition;

    private float avgTurnSpeed;
    private float maxEntrySpeed;
    private float turnBearing;

    @Ignore
    private TurnDirection turnDirection;


    public Turn(Location location) {
        turnPoints = new ArrayList<>();
        turnPoints.add(location);

        initialTurnPosition = new Coordinate(location.getLatitude(), location.getLongitude());

        avgTurnSpeed = 0;
        maxEntrySpeed = location.getSpeed();
        turnDirection = TurnDirection.UNDEFINED;
        turnBearing = 0;
    }

    public Turn(long turnId, Coordinate initialTurnPosition, Coordinate middleTurnPosition,
                Coordinate finalTurnPosition, float avgTurnSpeed,
                float maxEntrySpeed, float turnBearing) {
        this.turnId = turnId;
        this.initialTurnPosition = initialTurnPosition;
        this.middleTurnPosition = middleTurnPosition;
        this.finalTurnPosition = finalTurnPosition;
        this.avgTurnSpeed = avgTurnSpeed;
        this.maxEntrySpeed = maxEntrySpeed;
        this.turnBearing = turnBearing;
    }

    // Provides turn statistics when the turn is ended
    public void closeTurn() {
        middleTurnPosition = calculateMiddleTurnPosition();
        finalTurnPosition = calculateFinalTurnPosition();
        avgTurnSpeed = calculateAvgTurnSpeed();
        turnBearing = calculateTurnBearing();
    }

    // Returns average from speed of all turn points
    private float calculateAvgTurnSpeed() {
        float sumSpeed = 0;
        int size = turnPoints.size();
        for (Location l : turnPoints) {
            sumSpeed += l.getSpeed();
        }
        return sumSpeed/size;
    }

    /* "Integrates" over all turning points, calculating total turn bearing and compensating for
     *  transition between 360 and 0 degrees.
    */
    private float calculateTurnBearing() {
        float totalBearingChange = 0;
        float currentBearingChange;
        for (int i = 1; i < turnPoints.size(); i++) {
            currentBearingChange = turnPoints.get(i).getBearing() - turnPoints.get(i - 1).getBearing();

            currentBearingChange = correctedBearingChange(currentBearingChange);

            totalBearingChange += currentBearingChange;
        }

        // Sets turn direction based on final bearing change result
        if (totalBearingChange > 0) {
            this.turnDirection = TurnDirection.RIGHT;
        } else {
            this.turnDirection = TurnDirection.LEFT;
        }

        return totalBearingChange;
    }

    private Coordinate calculateMiddleTurnPosition() {
        int middleIndex = turnPoints.size() / 2;
        Location l = turnPoints.get(middleIndex);
        return new Coordinate(l.getLatitude(), l.getLongitude());
    }

    private Coordinate calculateFinalTurnPosition() {
        int finalIndex = turnPoints.size() - 1;
        Location l = turnPoints.get(finalIndex);
        return new Coordinate(l.getLatitude(), l.getLongitude());
    }

    /**
     * Checks if bearing change is greater than possible for a car. If true, it is probably
     * a transition between 360 and 0 degrees, and returns the corrected bearing change
     *
     * @param bearingChange the bearing change to analyse, calculated as final bearing - initial
     *                      bearing
     * @return  the probable real bearing change, being < 0 for a left turn, and >0 for a right turn
     */
    public static float correctedBearingChange(float bearingChange) {
        float result = bearingChange;
        if (Math.abs(result) > MAX_TURN_BEARING) {
            if (result > 0) {
                result -= 360;
            } else {
                result += 360;
            }
        }
        return result;
    }

    public void setRouteCreatorId(long routeCreatorId) {
        this.routeCreatorId = routeCreatorId;
    }

    public void addTurnPoint(Location location) {
        turnPoints.add(location);
    }

    public long getTurnId() {
        return turnId;
    }

    public long getRouteCreatorId() {
        return routeCreatorId;
    }

    public float getAvgTurnSpeed() {
        return avgTurnSpeed;
    }

    public float getTurnBearing() {
        return turnBearing;
    }

    public TurnDirection getTurnDirection() {
        return turnDirection;
    }

    public float getMaxEntrySpeed() {
        return maxEntrySpeed;
    }

    public Coordinate getInitialTurnPosition() {
        return initialTurnPosition;
    }

    public Coordinate getMiddleTurnPosition() {
        return middleTurnPosition;
    }

    public Coordinate getFinalTurnPosition() {
        return finalTurnPosition;
    }
}
