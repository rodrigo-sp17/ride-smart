package com.github.ridesmart.entities;

import com.google.android.gms.maps.model.LatLng;

/**
 * Immutable type representing a Latitude/Longitude coordinate pair. It was implemented instead of
 * using Android`s default LatLng to allow for database embedded saving through a DAO.
 */
public class Coordinate {
    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getCoordinatesAsLatLng() {
        return new LatLng(latitude, longitude);
    }
}
