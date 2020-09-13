package com.github.ridesmart;

import android.location.Location;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "route_nodes")
public class RouteNode extends Location {

    @PrimaryKey(autoGenerate = true)
    public long routeNodeId;

    // Foreign key - the route that contains the node
    public long routeCreatorId;

    @Embedded
    public Coordinates position;

    public float bearing;

    public float speed;

    public long time;

    // Converts a Location object to a persistable RouteNode object, keeping relevant information
    public RouteNode(Location l) {
        super(l);
        position = new Coordinates(l.getLatitude(), l.getLongitude());
        bearing = l.getBearing();
        speed = l.getSpeed();
        time = l.getTime();
    }

    public RouteNode(long routeNodeId, Coordinates position, float bearing, float speed, long time) {
        super("");
        this.routeNodeId = routeNodeId;
        this.position = position;
        this.bearing = bearing;
        this.speed = speed;
        this.time = time;
    }
}
