package com.github.ridesmart.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class RouteDetails {

    @PrimaryKey(autoGenerate = true)
    private long routeId;

    private String routeName;

    // Total route distance in m
    private float totalDistance;

    // Route duration in millis
    private long routeDuration;

    public RouteDetails(long routeId, String routeName, float totalDistance, long routeDuration) {
        this.routeName = routeName;
        this.routeId = routeId;
        this.totalDistance = totalDistance;
        this.routeDuration = routeDuration;
    }

    @Ignore
    public RouteDetails() {
        this.totalDistance = 0;
        this.routeDuration = 0;
    }

    /**
     * Returns the id of the route.
     * @return id of the route
     */
    protected long getRouteId() {
        return this.routeId;
    }

    /**
     * Sets the id of the route.
     * @param id    database created id for this route
     */
    protected void setRouteId(long id) {
        this.routeId = id;
    }

    protected float getTotalDistance() {
        return totalDistance;
    }

    protected void setTotalDistance(float distance) {
        totalDistance = distance;
    }

    protected void addDistance(float distance) {
        totalDistance += distance;
    }

    protected long getRouteDuration() {
        return routeDuration;
    }

    protected void setRouteDuration(long millis) {
        routeDuration = millis;
    }

    protected String getRouteName() {
        if (routeName == null) {
            routeName = "";
        }
        return routeName;
    }

    protected void setRouteName(String routeName) {
        this.routeName = routeName;
    }
}
