package com.github.ridesmart.entities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.github.ridesmart.entities.Route;
import com.github.ridesmart.entities.RouteDetails;
import com.github.ridesmart.entities.RouteNode;
import com.github.ridesmart.entities.Turn;

import java.util.List;

@Dao
public interface RouteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRouteDetails(RouteDetails routeDetails);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTurns(Turn turn);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRouteNodes(RouteNode node);

    default long saveRoute(Route route) {
        long routeId = insertRouteDetails(route.getDetails());
        route.getDetails().setRouteId(routeId);

        for (RouteNode node : route.getRouteNodes()) {
            node.setRouteCreatorId(routeId);
            insertRouteNodes(node);
        }

        for (Turn turn : route.getTurns()) {
            turn.setRouteCreatorId(routeId);
            insertTurns(turn);
        }

        return routeId;
    }

    default void deleteRoute(Route route) {
        deleteRouteDetails(route.getDetails());

        for (RouteNode node : route.getRouteNodes()) {
            deleteRouteNodes(node);
        }

        for (Turn turn : route.getTurns()) {
            deleteTurns(turn);
        }
    }


    @Transaction
    @Query("SELECT * FROM routedetails")
    List<Route> getAllRoutes();

    @Transaction
    @Query("SELECT * FROM routedetails WHERE routeId = :id ")
    Route getRouteFromId(long id);

    @Update
    void updateRouteDetails(RouteDetails routeDetails);

    @Update
    void updateTurns(Turn turn);

    @Update
    void updateRouteNodes(RouteNode node);

    @Delete
    void deleteRouteDetails(RouteDetails routeDetails);

    @Delete
    void deleteTurns(Turn turn);

    @Delete
    void deleteRouteNodes(RouteNode node);
}
