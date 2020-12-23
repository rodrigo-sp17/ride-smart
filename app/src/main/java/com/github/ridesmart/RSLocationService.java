package com.github.ridesmart;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class RSLocationService extends Service {

    private static final String TAG = RSLocationService.class.getSimpleName();

    private static final int LOCATION_REQUEST_INTERVAL = 2000;
    private static final int LOCATION_REQUEST_FAST_INTERVAL = 500;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Location lastLocation;

    private NotificationManager notificationManager;
    private NotificationChannel channel;
    private Notification notification;
    private static final int NOTIFICATION_ID = 117;
    private static final String CHANNEL_ID = "channel_01";

    public static final String EXTRA_LOCATION = "teste";

    public RSLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "Description test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    // Binding functionality not implemented
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
    }

    public void startLocationUpdates() {
        Log.i(TAG, "Starting location updates...");

        notification = getNotification();
        startForeground(NOTIFICATION_ID, notification);

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not available. Could not start updates. " + e);
        }
    }

    public void stopLocationUpdates() {
        Log.i(TAG, "Stopping location updates");
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            stopForeground(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not available. Could not stop updates. " + e);
        }
    }

    private void onNewLocation(Location location) {
        lastLocation = location;

        // Notifies listeners of position change
        Intent intent = new Intent();
        intent.setAction("LOCATION_CHANGED");
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);
        Log.i(TAG,"New location broadcasted");
    }

    private void getLastLocation() {
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                lastLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location");
                            }
                        }});
        } catch (SecurityException e) {
            Log.e(TAG, "Lost location permission.", e);
        }
    }


    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FAST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, RSLocationService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(getText(R.string.notification_text))
                .setContentTitle(getText(R.string.app_name))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .setWhen(System.currentTimeMillis())
                .build();

        return n;
    }




}
