package com.github.ridesmart;

import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "turns")
public class Turn {

    public enum TurnDirection {LEFT, RIGHT, UNDEFINED}

    @PrimaryKey (autoGenerate = true)
    public long turnId;

    // RouteId foreign key - identifies this turn as belonging to a specific route
    public long routeCreatorId;

    @Ignore
    // Establishes max turn bearing at 0.200s for a typical vehicle. Default is 45 (degrees).
    private final static float MAX_TURN_BEARING = 45;

    @Ignore
    private List<Location> turnPoints;

    @Embedded(prefix = "init_")
    public Coordinates initialTurnPosition;
    @Embedded(prefix = "mid_")
    public Coordinates middleTurnPosition;
    @Embedded(prefix = "final_")
    public Coordinates finalTurnPosition;

    public float avgTurnSpeed;
    public float maxEntrySpeed;
    public float turnBearing;

    @Ignore
    private TurnDirection turnDirection;


    public Turn(Location location) {
        turnPoints = new ArrayList<>();
        turnPoints.add(location);

        initialTurnPosition = new Coordinates(location.getLatitude(), location.getLongitude());

        avgTurnSpeed = 0;
        maxEntrySpeed = 0;
        turnDirection = TurnDirection.UNDEFINED;
        turnBearing = 0;
    }

    public Turn(long turnId, Coordinates initialTurnPosition, Coordinates middleTurnPosition,
                Coordinates finalTurnPosition, float avgTurnSpeed,
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
        maxEntrySpeed = turnPoints.get(0).getSpeed();
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

    private Coordinates calculateMiddleTurnPosition() {
        int middleIndex = turnPoints.size() / 2;
        Location l = turnPoints.get(middleIndex);
        return new Coordinates(l.getLatitude(), l.getLongitude());
    }

    private Coordinates calculateFinalTurnPosition() {
        int finalIndex = turnPoints.size() - 1;
        Location l = turnPoints.get(finalIndex);
        return new Coordinates(l.getLatitude(), l.getLongitude());
    }

    /** Checks if bearing change is greater than possible for a car. If true, it is probably
      * a transition between 360 and 0 degrees, and returns the corrected bearing change
     * */
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

    public void addTurnPoint(Location location) {
        turnPoints.add(location);
    }

    public float getAvgTurnSpeed() {
        return avgTurnSpeed;
    }

    public void setAvgTurnSpeed(float speed) {
        avgTurnSpeed = speed;
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




}