package com.nativenote.ejogajogassignment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.LocationResult;
import com.nativenote.ejogajogassignment.service.EventLocationModel;
import com.nativenote.ejogajogassignment.service.LUService;

import org.greenrobot.eventbus.EventBus;

public class LocationReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES = "com.nativenote.ejogajogassignment.action.PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    EventBus.getDefault().postSticky(new EventLocationModel(result.getLocations()));
                    context.startService(new Intent(context, LUService.class));
                }
            }
        }
    }
}
