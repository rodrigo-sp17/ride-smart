package com.github.ridesmart;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.ridesmart.entities.RouteDAO;
import com.github.ridesmart.entities.RouteDetails;
import com.github.ridesmart.entities.RouteNode;
import com.github.ridesmart.entities.Turn;

@Database(entities = {RouteDetails.class, Turn.class, RouteNode.class}, version = 2)
public abstract class RideDatabase extends RoomDatabase {

    private static RideDatabase INSTANCE;

    public abstract RouteDAO routeDAO();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE RouteDetails ADD COLUMN routeName TEXT DEFAULT ''");
        }
    };

    public static RideDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    RideDatabase.class,
                    "rideDB" )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }

        return INSTANCE;
    }
}
