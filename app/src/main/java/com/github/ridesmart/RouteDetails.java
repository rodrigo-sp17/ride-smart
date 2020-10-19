package com.github.ridesmart;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.List;

@Entity
public class RouteDetails {

    @PrimaryKey(autoGenerate = true)
    public long routeId;

    // Total route distance in m
    public float totalDistance;

    // Route duration in millis
    public long routeDuration;

    public RouteDetails(long routeId, float totalDistance, long routeDuration) {
        this.routeId = routeId;
        this.totalDistance = totalDistance;
        this.routeDuration = routeDuration;
    }

    @Ignore
    public RouteDetails() {
        this.totalDistance = 0;
        this.routeDuration = 0;
    }

    public long getRouteId() {
        return this.routeId;
    }

    public void setRouteId(long id) {
        this.routeId = id;
    }
}
