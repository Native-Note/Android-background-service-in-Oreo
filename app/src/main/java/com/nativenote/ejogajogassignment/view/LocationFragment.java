package com.nativenote.ejogajogassignment.view;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nativenote.ejogajogassignment.R;
import com.nativenote.ejogajogassignment.databinding.FragmentMainBinding;
import com.nativenote.ejogajogassignment.listener.SwitchCheckChangeListener;
import com.nativenote.ejogajogassignment.service.LocationService;
import com.nativenote.ejogajogassignment.utiles.ConnectionDetector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.POWER_SERVICE;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    public static final String[] PERMISSION_LIST = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    public final static int REQUEST_CODE = 3999;

    private FragmentActivity mActivity;
    private FragmentMainBinding binding;

    private Snackbar snackbarPermissions;
    private Snackbar snackbarGps;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (FragmentActivity) context;
    }

    public LocationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        binding.setIsChecked(false);
        binding.setCallback(listener);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestOptimize();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasAllPermission())
            enableSwitch();
        else
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permission_txt), REQUEST_CODE, PERMISSION_LIST);
    }

    private void requestOptimize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (powerManager.isIgnoringBatteryOptimizations(mActivity.getApplicationContext().getPackageName())) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            } else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(ContentModel event) {
        binding.setLocText(event.getData());
    }

    private void enableSwitch() {
        binding.workingSwitch.setEnabled(true);
        if (isServiceRunning(LocationService.class, mActivity))
            binding.setIsChecked(true);
    }

    SwitchCheckChangeListener listener = checked -> {
        if (checked) {
            if (!ConnectionDetector.isNetworkPresent(mActivity)) {
                binding.setIsChecked(false);
                Snackbar.make(binding.getRoot(), mActivity.getResources().getString(R.string.no_internet), Snackbar.LENGTH_LONG).show();
                return;
            }
            checkLocationPermission();
        } else {
            if (isServiceRunning(LocationService.class, mActivity))
                stopLocationService();
        }
    };

    private void checkLocationPermission() {
        if (!hasAllPermission()) {
            binding.setIsChecked(false);
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permission_txt), REQUEST_CODE, PERMISSION_LIST);
        } else {
            checkGpsEnabled();
        }
    }

    private void checkGpsEnabled() {
        LocationManager lm = (LocationManager) mActivity.getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            reportGpsError();
        } else {
            resolveGpsError();

            if (isServiceRunning(LocationService.class, mActivity))
                stopLocationService();

            startLocationService();
        }
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void reportPermissionsError() {
        binding.setIsChecked(false);
        snackbarPermissions = Snackbar
                .make(binding.getRoot(),
                        getString(R.string.location_permission_required),
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, view -> {
                    resolvePermissionsError();
                    Intent intent = new Intent(Settings
                            .ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                    startActivity(intent);
                });

        snackbarPermissions.setActionTextColor(Color.RED);

        View sbView = snackbarPermissions.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbarPermissions.show();
    }

    private void resolvePermissionsError() {
        if (snackbarPermissions != null) {
            snackbarPermissions.dismiss();
            snackbarPermissions = null;
        }
    }

    private void reportGpsError() {
        binding.setIsChecked(false);

        snackbarGps = Snackbar
                .make(binding.getRoot(), getString(R.string.gps_required), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, view -> {
                    resolveGpsError();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                });
        snackbarGps.setActionTextColor(Color.RED);

        View sbView = snackbarGps.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbarGps.show();

    }

    private void resolveGpsError() {
        if (snackbarGps != null) {
            snackbarGps.dismiss();
            snackbarGps = null;
        }
    }

    private void startLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) mActivity.getApplicationContext().getSystemService(POWER_SERVICE);
            Intent intent = new Intent();
            if (!pm.isIgnoringBatteryOptimizations(mActivity.getApplicationContext().getPackageName())) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        }

        Intent intent = new Intent(mActivity, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity.startForegroundService(intent);
        } else {
            mActivity.startService(intent);
        }
    }

    private void stopLocationService() {
        Intent intent = new Intent(mActivity, LocationService.class);
        mActivity.stopService(intent);
    }


    private boolean hasAllPermission() {
        return EasyPermissions.hasPermissions(mActivity, PERMISSION_LIST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        enableSwitch();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        reportPermissionsError();
//        mActivity.finish();
    }

}
