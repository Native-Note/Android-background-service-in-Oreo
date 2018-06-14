package com.nativenote.ejogajogassignment.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nativenote.ejogajogassignment.receiver.LocationReceiver;
import com.nativenote.ejogajogassignment.receiver.NotificationHelper;
import com.nativenote.ejogajogassignment.view.ContentModel;
import com.nativenote.ejogajogassignment.view.receiver.WakeFullReceiver;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

public class LocationService extends Service {
    private static final int REQUEST_INTERVAL = 10000;
    private GoogleApiClient mGoogleApiClient;
    private PowerManager.WakeLock mWakelock;
    private static int FOREGROUND_ID = 1338;

    private void startLocationTracking() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mLocationRequestCallback)
                .addApi(LocationServices.API)
                .build();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    private GoogleApiClient.ConnectionCallbacks mLocationRequestCallback = new GoogleApiClient.ConnectionCallbacks() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LocationRequest request = new LocationRequest();
            request.setInterval(REQUEST_INTERVAL);
            request.setFastestInterval(REQUEST_INTERVAL / 2);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, getPendingIntent());

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationWakelockTag");
            mWakelock.acquire();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.e("Service onCreate");
        NotificationHelper helper = new NotificationHelper(this, null);
        startForeground(FOREGROUND_ID, helper.buildForegroundNotification(this));
        startLocationTracking();
        scheduleNextScan(this);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getPendingIntent());
        }

        if (mWakelock != null) {
            mWakelock.release();
        }
        Timber.e("Service destroyed");
        stopSchedule(this);
        super.onDestroy();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationReceiver.class);
        intent.setAction(LocationReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        if (ConnectionDetector.isNetworkPresent(this) && !isFetching) {
//            subscribeData(location.getLatitude(), location.getLongitude());
//        }
//    }

    private void scheduleNextScan(Context context) {
        int interval = 1000 * 30;
        long triggerAtTime = System.currentTimeMillis() + interval;

        Intent intent = new Intent(context, WakeFullReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 9909, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingIntent);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingIntent);
            }
        }
    }

    private void stopSchedule(Context context) {
        Intent intent = new Intent(context, WakeFullReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 9909, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.cancel(pendingIntent);
        }

        EventBus.getDefault().postSticky(new ContentModel(""));
    }
}
