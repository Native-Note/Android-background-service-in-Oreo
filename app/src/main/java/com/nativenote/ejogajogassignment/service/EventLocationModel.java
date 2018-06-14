package com.nativenote.ejogajogassignment.service;

import android.location.Location;

import java.util.List;

public class EventLocationModel {
    private List<Location> locations;

    public EventLocationModel(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
