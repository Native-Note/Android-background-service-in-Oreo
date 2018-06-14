package com.nativenote.ejogajogassignment.applications.module;

import android.content.Context;

import com.nativenote.ejogajogassignment.applications.ApplicationContext;
import com.nativenote.ejogajogassignment.applications.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    private final Context context;

    public ContextModule(Context context) {
        this.context = context.getApplicationContext();
    }

    @Provides
    @ApplicationScope
    @ApplicationContext
    public Context context() {
        return context;
    }
}
