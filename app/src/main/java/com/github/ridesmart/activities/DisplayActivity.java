package com.github.ridesmart.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.ridesmart.R;
import com.github.ridesmart.RideDatabase;
import com.github.ridesmart.entities.Route;
import com.github.ridesmart.entities.RouteNode;
import com.github.ridesmart.entities.Turn;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = DisplayActivity.class.getSimpleName();

    private GoogleMap displayMap;
    private Polyline displayedTrack;
    private static final int DEFAULT_ZOOM = 15;

    private Route displayedRoute;

    Toolbar displayToolbar;

    private TextView timeView;
    private TextView distanceView;
    private TextView avgSpeedView;
    private TextView maxSpeedView;
    private TextView turnsView;

    private RideDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        displayToolbar = findViewById(R.id.display_toolbar);
        setSupportActionBar(displayToolbar);
        displayToolbar.setTitle(R.string.display_toolbar_title);

        timeView = findViewById(R.id.time_text);
        distanceView = findViewById(R.id.total_distance_text);
        avgSpeedView = findViewById(R.id.avg_speed_text);
        maxSpeedView = findViewById(R.id.max_speed_text);
        turnsView = findViewById(R.id.num_turns_text);

        // Initiates database
        database = RideDatabase.getInstance(this);

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
        String title = String.format(Locale.getDefault(),
                "%s - %s",
                getResources().getString(R.string.display_toolbar_title),
                displayedRoute.getName());
        displayToolbar.setTitle(title);

        buildPolyline();
        markTurns();
        readDashboard();
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
        displayMap.moveCamera(CameraUpdateFactory.newLatLngZoom(options.getPoints().get(0),
                DEFAULT_ZOOM));
    }

    private void markTurns() {
        List<Turn> turns = displayedRoute.getTurns();

        for (Turn t : turns) {
            // Converts speed to KM/H
            double turnSpeedInKmH = t.getAvgTurnSpeed() * 3.6;
            double entrySpeedInKmH = t.getMaxEntrySpeed() * 3.6;

            displayMap.addMarker(new MarkerOptions()
                    .position(t.getInitialTurnPosition().getCoordinatesAsLatLng())
                    .title("Entry Speed - " + String.format(Locale.getDefault(), "%.1f km/h",
                            entrySpeedInKmH)));

            displayMap.addMarker(new MarkerOptions()
                    .position(t.getMiddleTurnPosition().getCoordinatesAsLatLng())
                    .title("Avg. Speed - " + String.format(Locale.getDefault(), "%.1f km/h",
                            turnSpeedInKmH)));

            displayMap.addMarker(new MarkerOptions()
                    .position(t.getFinalTurnPosition().getCoordinatesAsLatLng())
                    .title("Turn Exit"));
        }
    }

    // Fills fields with route data
    private void readDashboard() {
        String duration = String.format(Locale.getDefault(), "%d:%d:%d",
                TimeUnit.MILLISECONDS.toHours(displayedRoute.getDuration()),
                (TimeUnit.MILLISECONDS.toMinutes(displayedRoute.getDuration()) % 60),
                (TimeUnit.MILLISECONDS.toSeconds(displayedRoute.getDuration()) % 60)
                );
        timeView.setText(duration);

        // TODO - proper speed conversion
        String distance = String.format(Locale.getDefault(), "%.2f km",
                displayedRoute.getTotalDistance());
        distanceView.setText(distance);

        String avgSpeedInKMH = String.format(Locale.getDefault(), "%.1f km/h",
                (displayedRoute.getAvgSpeed() * 3.6));
        avgSpeedView.setText(avgSpeedInKMH);

        String maxSpeedInKMH = String.format(Locale.getDefault(), "%.1f km/h",
                (displayedRoute.getMaxSpeed() * 3.6));
        maxSpeedView.setText(maxSpeedInKMH);

        turnsView.setText(Long.valueOf(displayedRoute.getNumTurns()).toString());
    }
}