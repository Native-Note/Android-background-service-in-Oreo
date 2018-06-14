package com.nativenote.ejogajogassignment.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.bosphere.filelogger.FL;
import com.nativenote.ejogajogassignment.R;
import com.nativenote.ejogajogassignment.applications.App;
import com.nativenote.ejogajogassignment.network.ServiceFactory;
import com.nativenote.ejogajogassignment.network.model.PlaceDetails;
import com.nativenote.ejogajogassignment.receiver.NotificationHelper;
import com.nativenote.ejogajogassignment.view.ContentModel;
import com.nativenote.ejogajogassignment.view.dagger.DaggerLocationComponent;
import com.nativenote.ejogajogassignment.view.dagger.LocationComponent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

public class LUService extends IntentService {
    private static final String TAG = "LUService";

    @Inject
    ServiceFactory serviceFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        LocationComponent component = DaggerLocationComponent.builder()
                .applicationComponent(((App) getApplication()).getComponent())
                .build();
        component.inject(this);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(EventLocationModel event) {
        List<Location> locations = event.getLocations();
        NotificationHelper notificationHelper = new NotificationHelper(this, locations);
        notificationHelper.buildForegroundNotification(this);
        notificationHelper.showNotification();
        subscribeData(locations.get(0).getLatitude(), locations.get(0).getLongitude());
    }

    public LUService() {
        super("LUService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    private void subscribeData(double latitude, double longitude) {
        serviceFactory.getPlaceDetails(latitude, longitude, new ServiceFactory.PlaceCallback() {
            @Override
            public void onSuccess(PlaceDetails data) {
                if (data != null) {
                    String text = "Latitude: " + data.getLat() + "\nLongitude: " + data.getLon() + "\nAddress: \n" + data.getDisplayName();

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                        if (pm != null && pm.isDeviceIdleMode()) {
//                            text += text + "\nDevice is in Idle Mode";
//                        }
//                    }

                    EventBus.getDefault().postSticky(new ContentModel(text));
                    FL.i("\n-------- --------\n" + text + "\n\n\n");
                } else {
                    FL.e(getResources().getString(R.string.error_data));
                }
            }

            @Override
            public void onError(String errorMessage) {
                FL.e(getResources().getString(R.string.error_data));
            }
        });
    }
}
