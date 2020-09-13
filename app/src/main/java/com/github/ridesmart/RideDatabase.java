package com.github.ridesmart;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RouteDetails.class, Turn.class, RouteNode.class}, version = 1)
public abstract class RideDatabase extends RoomDatabase {
    public abstract RouteDAO routeDAO();
}
