package com.dvlab.runtracker.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class RunManager {

    private static final String TAG = RunManager.class.getSimpleName();
    public static final String ACTION_LOCATION = "come.dvlab.runtracker.ACTION_LOCATION";

    private static final String TEST_PROVIDER = "TEST_PROVIDER";

    private static RunManager runManager;
    private Context context;
    private LocationManager locationManager;

    // The private constructor forces users to use RunManager.get(Context)
    private RunManager(Context appContext) {
        context = appContext;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static RunManager get(Context c) {
        if (runManager == null) {
            // Use the application context to avoid leaking activities
            runManager = new RunManager(c.getApplicationContext());
        }
        return runManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(context, 0, broadcast, flags);
    }

    public void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");

        String provider = LocationManager.GPS_PROVIDER;

        // If you have the test provider and it's enabled, use it
        if (locationManager.getProvider(TEST_PROVIDER) != null && locationManager.isProviderEnabled(TEST_PROVIDER)) {
            provider = TEST_PROVIDER;
        }
        Log.d(TAG, "Using provider " + provider);

        // Get the last known location and broadcast it if you have one
        Location lastKnown = locationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
            // Reset the time to now
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }


        // Start updates from the location manager
        PendingIntent pi = getLocationPendingIntent(true);

        try {
            locationManager.requestLocationUpdates(provider, 0, 0, pi);
        } catch (SecurityException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        context.sendBroadcast(broadcast);
    }

    public void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates");

        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            locationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

}
