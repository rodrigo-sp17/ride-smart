package com.github.ridesmart.entities;

import android.location.Location;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "route_nodes")
public class RouteNode extends Location {

    @PrimaryKey(autoGenerate = true)
    private long routeNodeId;

    // Foreign key - the route that contains the node
    private long routeCreatorId;

    @Embedded
    private final Coordinate position;

    private final float bearing;

    private final float speed;

    private final long time;

    // Converts a Location object to a persistable RouteNode object, keeping relevant information
    public RouteNode(Location l) {
        super(l);
        position = new Coordinate(l.getLatitude(), l.getLongitude());
        bearing = l.getBearing();
        speed = l.getSpeed();
        time = l.getTime();
    }

    public RouteNode(long routeNodeId, Coordinate position, float bearing, float speed, long time) {
        super("");
        this.routeNodeId = routeNodeId;
        this.position = position;
        this.bearing = bearing;
        this.speed = speed;
        this.time = time;
    }

    public long getRouteNodeId() {
        return routeNodeId;
    }

    public Coordinate getPosition() {
        return position;
    }

    @Override
    public float getBearing() {
        return bearing;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public long getTime() {
        return time;
    }

    public long getRouteCreatorId() {
        return routeCreatorId;
    }

    public void setRouteCreatorId(long routeCreatorId) {
        this.routeCreatorId = routeCreatorId;
    }
}
