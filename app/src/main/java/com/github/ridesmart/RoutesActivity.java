package com.github.ridesmart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;

public class RoutesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RoutesAdapter routesAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public static RideDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        // Initiates database
        database = Room.databaseBuilder(getApplicationContext(), RideDatabase.class, "rideDB" )
                .allowMainThreadQueries()
                .build();

        // Sets up the toolbar
        Toolbar routesToolbar = findViewById(R.id.routes_toolbar);
        routesToolbar.setTitle(R.string.routes_toolbar_title);
        setSupportActionBar(routesToolbar);

        // Sets RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        routesAdapter = new RoutesAdapter();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(routesAdapter);
    }
}