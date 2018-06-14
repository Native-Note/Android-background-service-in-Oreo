/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nativenote.ejogajogassignment.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.nativenote.ejogajogassignment.R;
import com.nativenote.ejogajogassignment.view.MainActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Class to process location results.
 */
public class NotificationHelper {
    private static int NOTIFY_ID = 1337;
    final private static String PRIMARY_CHANNEL = "default";

    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;

    public NotificationHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                    context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }
    }

    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     */
    private String getLocationResultTitle() {
        if (mLocations == null || mLocations.isEmpty()) {
            return mContext.getString(R.string.service_running);
        }
        String numLocationsReported = mContext.getResources().getQuantityString(
                R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    public String getLocationResultText() {
        if (mLocations == null || mLocations.isEmpty()) {
            return mContext.getString(R.string.no_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : mLocations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    public void showNotification() {
        getNotificationManager().notify(NOTIFY_ID, buildForegroundNotification(mContext));
    }

    public Notification buildForegroundNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(mContext, MainActivity.class);

            // Construct a task stack.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

            // Add the main Activity to the task stack as the parent.
            stackBuilder.addParentStack(MainActivity.class);

            // Push the content Intent onto the stack.
            stackBuilder.addNextIntent(notificationIntent);

            // Get a PendingIntent containing the entire back stack.
            PendingIntent notificationPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder = new Notification.Builder(mContext, PRIMARY_CHANNEL)
                    .setContentTitle(getLocationResultTitle())
                    .setContentText(getLocationResultText())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPendingIntent);

            return notificationBuilder.build();
        } else {
            NotificationCompat.Builder b = new NotificationCompat.Builder(context);
            b.setOngoing(true)
                    .setContentTitle(getLocationResultTitle())
                    .setContentText(getLocationResultText())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setTicker(context.getResources().getString(R.string.app_name));

            return b.build();
        }
    }
}
