package com.github.ridesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;


public class RideActivity extends AppCompatActivity
        implements OnMapReadyCallback{

    private static final String TAG = RideActivity.class.getSimpleName();
    private GoogleMap map;
    private Polyline track;
    private PolylineOptions polylineOptions;
    private Route route;

    // Location permission handling
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int LOCATION_REQUEST_INTERVAL = 2000;
    private static final int LOCATION_REQUEST_FAST_INTERVAL = 500;
    private boolean locationPermissionGranted;

    // Location of mobile from fused location provider
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean requestingLocationUpdates;

    // Default zoom
    private static final int DEFAULT_ZOOM = 15;

    // View objects
    private Chronometer timeView;
    private long timeWhenStopped = 0;

    private TextView speedView;
    private TextView distanceView;
    private Button goButton;
    private Button resumeButton;
    private Button stopButton;

    // goButton state holder
    private boolean isRecording;

    // Database
    public static RideDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        // Sets up the toolbar
        Toolbar rideToolbar = (Toolbar) findViewById(R.id.ride_toolbar);
        setSupportActionBar(rideToolbar);

        // Initiates database
        database = Room.databaseBuilder(getApplicationContext(), RideDatabase.class, "rideDB" )
                .allowMainThreadQueries()
                .build();

        // View elements
        timeView = findViewById(R.id.time_view);
        speedView = findViewById(R.id.speed_view);
        distanceView = findViewById(R.id.distance_view);
        goButton = findViewById(R.id.go_button);
        resumeButton = findViewById(R.id.resume_button);
        stopButton = findViewById(R.id.stop_button);

        // Initiates map fragment
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_map));
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Instantiates a Location Callback
        locationCallback = new MonitorCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ride, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open:
                // Starts RoutesActivity
                Intent intent = new Intent(this, RoutesActivity.class);
                this.startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onGoClick(View view) {
        if (isRecording) {
            pauseRecording();
            goButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
        } else {
            startRecording();
            goButton.setText(R.string.go_button_pause);
            goButton.setBackgroundColor(Color.YELLOW);
        }
    }

    public void onResumeClick(View view) {
        resumeRecording();
        goButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
    }

    public void onStopClick(View view) {
        stopRecording();
        resumeButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void getLocationPermission() {
        /*
         * Requests location permission from user. If permission already granted, returns.
         */
        // TODO - Implement permission request for background location

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        updateLocationUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }


    private void updateLocationUI() {
        /*
         * As found in https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
         * Updates map with my location functions and buttons. If not allowed, requests permissions
         */
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Gets device location from fused location provider
         * From https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    private void startRecording() {
        createLocationRequest();
        startLocationUpdates();
        route = new Route();
        timeView.start();
        isRecording = true;
    }

    private void pauseRecording() {
        timeWhenStopped = timeView.getBase() - SystemClock.elapsedRealtime();
        timeView.stop();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        requestingLocationUpdates = false;
    }

    private void resumeRecording() {
        startLocationUpdates();
        timeView.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        timeView.start();
    }

    private void stopRecording() {
        // TODO - wrap route details
        long routeDuration = SystemClock.elapsedRealtime() - timeView.getBase();
        route.setRouteDuration(routeDuration);
        // TODO - save route to disk
        saveRoute(route);
    }

    private void saveRoute(Route route) {
        RouteDAO dao = database.routeDAO();
        long routeId = dao.insertRouteDetails(route.details);

        for (RouteNode node : route.routePoints) {
            node.routeCreatorId = routeId;
            dao.insertRouteNodes(node);
        }

        for (Turn turn : route.turns) {
            turn.routeCreatorId = routeId;
            dao.insertTurns(turn);
        }
    }


    private void startLocationUpdates() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
        requestingLocationUpdates = true;
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FAST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
    }

    private class MonitorCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //super.onLocationResult(locationResult);
            if (locationResult == null) {
                Log.println(Log.INFO,TAG,"Location result is null - returning...");
                return;
            }

            Location lastLocation = locationResult.getLastLocation();

            route.addLocation(lastLocation);

            // Sets speedView with last location speed info, in km/h
            // TODO - implement proper speed conversion
            String speed = String.format(Locale.getDefault(),"%.1f",
                    (lastLocation.getSpeed() * 3.6)) + " km/h";
            speedView.setText(speed);

            // Sets distanceView with total route distance, in KM
            String distance = String.format(Locale.getDefault(),"%.2f",
                    route.getTotalDistance()) + " km";
            distanceView.setText(distance);

            // Adds last location to PolylineOptions
            LatLng p = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            Log.println(Log.INFO, TAG, p.toString());
            polylineOptions.add(p);

            track = map.addPolyline(polylineOptions);
        }
    }
}