package com.github.ridesmart;

import android.location.Location;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class Route {

    // Defines the beginning of a turn, in degrees
    @Ignore
    public final float MIN_TURN_BEARING = 20;

    @Embedded
    public RouteDetails details;

    @Relation(
            parentColumn = "routeId",
            entityColumn = "routeCreatorId"
    )
    public List<RouteNode> routePoints;

    @Relation(
            parentColumn = "routeId",
            entityColumn = "routeCreatorId"
    )
    public List<Turn> turns;

    @Ignore
    private boolean isTurning;
    @Ignore
    private float lastBearing;
    @Ignore
    private Turn currentTurn;

    public Route() {
        details = new RouteDetails();
        routePoints = new ArrayList<>();
        turns = new ArrayList<>();
    }

    public void addLocation(Location location) {
        routePoints.add(new RouteNode(location));

        int size = routePoints.size();
        if (size > 1) {
            // Adds this last leg distance to total route distance
            float lastLegDistance = routePoints.get(size - 2).distanceTo(location);
            details.totalDistance += lastLegDistance;

            // Calculates the corrected bearing change
            float lastBearingDifference = routePoints.get(size - 1).getBearing()
                    - routePoints.get(size - 2).getBearing();
            lastBearingDifference = Turn.correctedBearingChange(lastBearingDifference);


            if (isTurning) {
                // If vehicle is turning to the same side, keep adding points to the turn
                if (lastBearingDifference * lastBearing > 0) {
                    currentTurn.addTurnPoint(location);
                    lastBearing = lastBearingDifference;
                } else {
                    // If vehicle is not turning anymore, closes turn and adds it to route turns list
                    currentTurn.closeTurn();
                    turns.add(currentTurn);
                    isTurning = false;
                    lastBearing = 0;
                }
            } else if (Math.abs(lastBearingDifference) > MIN_TURN_BEARING) {
                // If vehicle is initiating a turn, creates turn object for recording turn points
                currentTurn = new Turn(location);
                lastBearing = location.getBearing();
                isTurning = true;
            }
        }

    }

    // Returns total route distance in KM/H
    public float getTotalDistance() {
        // TODO - implement proper distance conversion
        return details.totalDistance / 1000;
    }

    public void setRouteDuration(Long millis) {
        details.routeDuration = millis;
    }
}
