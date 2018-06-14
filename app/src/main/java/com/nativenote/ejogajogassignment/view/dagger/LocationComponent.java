package com.nativenote.ejogajogassignment.view.dagger;

import com.nativenote.ejogajogassignment.applications.component.ApplicationComponent;
import com.nativenote.ejogajogassignment.service.LUService;

import dagger.Component;

@LocationScope
@Component(modules = LocationModule.class, dependencies = ApplicationComponent.class)
public interface LocationComponent {
    void inject(LUService service);
}
