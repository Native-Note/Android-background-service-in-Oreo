package com.nativenote.ejogajogassignment.applications;

import android.app.Application;
import android.os.Environment;
import android.support.v7.app.AppCompatDelegate;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;
import com.nativenote.ejogajogassignment.applications.component.ApplicationComponent;
import com.nativenote.ejogajogassignment.applications.component.DaggerApplicationComponent;
import com.nativenote.ejogajogassignment.applications.module.ContextModule;
import com.nativenote.ejogajogassignment.network.ServiceFactory;

import java.io.File;

import timber.log.Timber;

public class App extends Application {
    ApplicationComponent component;

    ServiceFactory serviceFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Timber.plant(new Timber.DebugTree());

        FL.init(new FLConfig.Builder(this)
                .minLevel(FLConst.Level.V)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), "ejogajog_file_logger"))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);

        component = DaggerApplicationComponent.builder()
                .contextModule(new ContextModule(this))
                .build();

        serviceFactory = component.getService();
    }

    public ApplicationComponent getComponent() {
        return component;
    }
}
