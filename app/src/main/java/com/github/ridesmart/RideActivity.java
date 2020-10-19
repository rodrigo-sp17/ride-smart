package com.github.ridesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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
    private boolean locationPermissionGranted;

    // Location of mobile from fused location provider
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private RSLocationService locationService;
    private BroadcastReceiver receiver;
    private boolean requestingLocationUpdates;
    private Intent serviceIntent;

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
        Toolbar rideToolbar = findViewById(R.id.ride_toolbar);
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

        // Gets a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Instantiates the broadcast receiver
        receiver = new RSReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("LOCATION_CHANGED");
        this.registerReceiver(receiver, filter);
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
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
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

        goButton.setVisibility(View.VISIBLE);
        goButton.setText(R.string.go_button_new);
        goButton.setBackgroundColor(Color.GREEN);
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show rationale and request permissions
            showContextAlert();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        updateLocationUI();
    }

    // Shows an AlertDialog to provide context for location permissions request
    private void showContextAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rationale_title)
                .setMessage(R.string.rationale_message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(RideActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .create()
                .show();
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
            Log.e(TAG, "Could not update the location UI, no permissions", e);
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
        getLocationPermission();
        if (locationPermissionGranted) {
            isRecording = true;
            serviceIntent = new Intent(this, RSLocationService.class);
            startService(serviceIntent);

            // Starts new route with a polyline
            map.clear();
            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLUE);
            route = new Route();

            // Starts chronometer
            timeView.setBase(SystemClock.elapsedRealtime());
            timeView.start();

        } else {
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    private void pauseRecording() {
        timeWhenStopped = timeView.getBase() - SystemClock.elapsedRealtime();
        timeView.stop();
        stopService(serviceIntent);
        requestingLocationUpdates = false;
    }

    private void resumeRecording() {
        startService(serviceIntent);
        timeView.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        timeView.start();
    }

    private void stopRecording() {
        // TODO - wrap route details
        stopService(serviceIntent);
        long routeDuration = SystemClock.elapsedRealtime() - timeView.getBase();
        route.setRouteDuration(routeDuration);
        // TODO - save route to disk
        saveRoute(route);

        isRecording = false;
    }

    // Uses DAO to save route to database
    private void saveRoute(Route route) {
        RouteDAO dao = database.routeDAO();
        long routeId = dao.insertRouteDetails(route.details);

        for (RouteNode node : route.routeNodes) {
            node.routeCreatorId = routeId;
            dao.insertRouteNodes(node);
        }

        for (Turn turn : route.turns) {
            turn.routeCreatorId = routeId;
            dao.insertTurns(turn);
        }
    }

    private final class RSReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(RSLocationService.EXTRA_LOCATION);
            if (location != null) {

                // Gets last location from result, updates field and adds to route
                lastKnownLocation = location;
                route.addLocation(lastKnownLocation);

                // Moves camera to received position
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                /*
                    Sets speedView with last location speed info, in km/h
                    TODO - implement proper speed conversion
                */
                String speed = String.format(Locale.getDefault(),"%.1f",
                        (lastKnownLocation.getSpeed() * 3.6)) + " km/h";
                speedView.setText(speed);

                // Sets distanceView with total route distance, in KM
                String distance = String.format(Locale.getDefault(),"%.2f",
                        route.getTotalDistance()) + " km";
                distanceView.setText(distance);

                // Adds last location to PolylineOptions
                LatLng p = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                polylineOptions.add(p);

                // Updates Route Polyline
                track = map.addPolyline(polylineOptions);
            } else {
                Log.i(TAG, "Location is null");
            }
        }
    }
}