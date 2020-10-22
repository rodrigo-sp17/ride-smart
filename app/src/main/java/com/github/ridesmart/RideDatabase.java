package com.github.ridesmart;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.github.ridesmart.entities.RouteDAO;
import com.github.ridesmart.entities.RouteDetails;
import com.github.ridesmart.entities.RouteNode;
import com.github.ridesmart.entities.Turn;

@Database(entities = {RouteDetails.class, Turn.class, RouteNode.class}, version = 1)
public abstract class RideDatabase extends RoomDatabase {
    public abstract RouteDAO routeDAO();
}
