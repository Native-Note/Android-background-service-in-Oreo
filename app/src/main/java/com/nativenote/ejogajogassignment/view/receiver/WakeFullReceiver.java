package com.nativenote.ejogajogassignment.view.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.nativenote.ejogajogassignment.service.LocationService;
import com.nativenote.ejogajogassignment.view.LocationFragment;

import timber.log.Timber;

/**
 * This wakeful receiver will start json download for white list every night at 1 AM.
 */
public class WakeFullReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.e("App is still running");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent service = new Intent(context, LocationService.class);
            if (LocationFragment.isServiceRunning(LocationService.class, context))
                context.stopService(service);

            // startWakefulService(context, service);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        }
    }
}