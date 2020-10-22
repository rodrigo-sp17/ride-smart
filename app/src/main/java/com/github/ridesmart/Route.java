package com.github.ridesmart;

import android.location.Location;
import android.util.Log;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class Route {

    // Defines the beginning of a turn, in degrees
    @Ignore
    private static final float MIN_TURN_BEARING = 12;

    @Embedded
    public RouteDetails details;

    @Relation(
            parentColumn = "routeId",
            entityColumn = "routeCreatorId"
    )
    public List<RouteNode> routeNodes;

    @Relation(
            parentColumn = "routeId",
            entityColumn = "routeCreatorId"
    )
    public List<Turn> turns;

    @Ignore
    private boolean isTurning;
    @Ignore
    private float lastBearingDifference;
    @Ignore
    private Turn currentTurn;

    public Route() {
        details = new RouteDetails();
        routeNodes = new ArrayList<>();
        turns = new ArrayList<>();
    }

    /**
     * Adds and processes a new location for this route.
     * @param location  the location to add to this route
     */
    public void addLocation(final Location location) {
        routeNodes.add(new RouteNode(location));

        // If size is less than 2, there are not enough points to determine a turn
        int size = routeNodes.size();
        if (size > 2) {
            // Adds this last leg distance to total route distance
            float lastLegDistance = routeNodes.get(size - 2).distanceTo(location);
            details.totalDistance += lastLegDistance;

            // Calculates the corrected bearing change
            float bearingDifference = routeNodes.get(size - 1).getBearing()
                    - routeNodes.get(size - 2).getBearing();
            bearingDifference = Turn.correctedBearingChange(bearingDifference);

            // If is turning to the same side of the last heading change, it is probably turning
            if (isTurning) {
                // If vehicle is turning to the same side, keep adding points to the turn
                if (bearingDifference * lastBearingDifference > 0) {
                    currentTurn.addTurnPoint(routeNodes.get(size - 2));
                } else {
                    // If vehicle is not turning anymore, closes turn and adds it to route turns
                    // list
                    currentTurn.closeTurn();
                    turns.add(currentTurn);
                    isTurning = false;
                }
            } else if (Math.abs(bearingDifference) > MIN_TURN_BEARING) {
                isTurning = true;
                // Adds the point of abrupt bearing change
                currentTurn = new Turn(routeNodes.get(size - 2));
            }
            lastBearingDifference = bearingDifference;
        }
    }

    public RouteDetails getDetails() {
        return details;
    }

    public List<RouteNode> getRouteNodes() {
        return routeNodes;
    }

    public List<Turn> getTurns() {
        return turns;
    }

    /**
     * Returns total route distance, in KM
     * @return total route distance in KM
     */
    public float getTotalDistance() {
        // TODO - implement proper distance conversion
        return details.totalDistance / 1000;
    }

    /**
     * Returns route duration, in milliseconds
     * @return  tbe duration of the route, in milliseconds
     */
    public long getDuration() {
        return details.getRouteDuration();
    }

    public void setDuration(Long millis) {
        details.routeDuration = millis;
    }

    /**
     * Returns average speed of route, in m/s
     * @return  average speed of route, in m/s
     */
    public double getAvgSpeed() {
        float totalSpeed = 0;
        // Java Streams not used because of compatibility issues
        for (RouteNode node : routeNodes) {
            totalSpeed += node.getSpeed();
        }

        // Returns average
        return totalSpeed / routeNodes.size();
    }

    /**
     * Returns maximum speed reached during the route
     * @return maximum speed, in m/s
     */
    public double getMaxSpeed() {
        double maxSpeed = 0;
        for (RouteNode node : routeNodes) {
            double speed = node.getSpeed();
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
        }

        return maxSpeed;
    }

    /**
     * Returns the number of turns of this route
     * @return number of turns
     */
    public long getNumTurns() {
        return turns.size();
    }
}
