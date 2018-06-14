package com.nativenote.ejogajogassignment.applications.component;

import com.nativenote.ejogajogassignment.applications.ApplicationScope;
import com.nativenote.ejogajogassignment.applications.module.ServiceModule;
import com.nativenote.ejogajogassignment.network.ServiceFactory;

import dagger.Component;

@ApplicationScope
@Component(modules = {ServiceModule.class})
public interface ApplicationComponent {
    ServiceFactory getService();
}
