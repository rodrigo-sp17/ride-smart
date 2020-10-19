package com.github.ridesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
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

        // Sets swipe callback
        ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback());
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        routesAdapter.notifyDataSetChanged();
    }

    // This class is used for the "swipe to delete" function
    public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        public SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            routesAdapter.deleteItem(position);
        }
    }
}