package com.github.ridesmart;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RouteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRouteDetails(RouteDetails routeDetails);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTurns(Turn turn);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRouteNodes(RouteNode node);

    @Transaction
    @Query("SELECT * FROM routedetails")
    List<Route> getAllRoutes();

    @Update
    void updateRouteDetails(RouteDetails... routes);

    @Update
    void updateTurns(Turn... turns);

    @Update
    void updateRouteNodes(RouteNode... nodes);

    @Delete
    void deleteRouteDetails(RouteDetails... routes);

    @Delete
    void deleteTurns(Turn... turns);

    @Delete
    void deleteRouteNodes(RouteNode... nodes);
}
