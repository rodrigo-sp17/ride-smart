package com.github.ridesmart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class DisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = DisplayActivity.class.getSimpleName();

    private GoogleMap displayMap;
    private Polyline displayedTrack;
    private static final int DEFAULT_ZOOM = 15;

    private Route displayedRoute;

    private RideDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Toolbar displayToolbar = findViewById(R.id.display_toolbar);
        setSupportActionBar(displayToolbar);

        // Initiates database
        database = Room.databaseBuilder(getApplicationContext(), RideDatabase.class, "rideDB" )
                .allowMainThreadQueries()
                .build();

        // Gets map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.display_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        displayMap = googleMap;
        loadRoute(getIntent().getLongExtra("id",0));
    }

    private void loadRoute(long id) {
        displayedRoute = database.routeDAO().getRouteFromId(id);

        buildPolyline();
        markTurns();
    }

    private void buildPolyline() {
        PolylineOptions options = new PolylineOptions();
        options.color(Color.BLUE);

        List<RouteNode> nodes = displayedRoute.getRouteNodes();

        for (RouteNode n : nodes) {
            options.add(n.getPosition().getCoordinatesAsLatLng());
        }

        displayedTrack = displayMap.addPolyline(options);

        // Moves camera for 1st point off Polyline
        displayMap.moveCamera(CameraUpdateFactory.newLatLngZoom(options.getPoints().get(0), DEFAULT_ZOOM));
    }

    private void markTurns() {
        List<Turn> turns = displayedRoute.getTurns();

        for (Turn t : turns) {
            // Converts speed to KM/H
            double turnSpeedInKmH = t.getAvgTurnSpeed() * 3.6;
            double entrySpeedInKmH = t.getMaxEntrySpeed() * 3.6;

            displayMap.addMarker(new MarkerOptions()
                    .position(t.getInitialTurnPosition().getCoordinatesAsLatLng())
                    .title("Initial - " + String.format("%.1f km/h", entrySpeedInKmH)));

            displayMap.addMarker(new MarkerOptions()
                    .position(t.getMiddleTurnPosition().getCoordinatesAsLatLng())
                    .title("Middle - " + String.format("%.1f km/h", turnSpeedInKmH)));


            displayMap.addMarker(new MarkerOptions()
                    .position(t.getFinalTurnPosition().getCoordinatesAsLatLng())
                    .title("Final - " + String.format("%.1f km/h", turnSpeedInKmH)));
        }
    }
}