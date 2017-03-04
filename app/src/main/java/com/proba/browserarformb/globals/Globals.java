package com.proba.browserarformb.globals;

import android.app.Application;
import android.location.Location;

import com.proba.browserarformb.model.LocationGPS;

import org.osmdroid.util.GeoPoint;

public class Globals extends Application {

    private static final String TAG = "Cip";

    private static LocationGPS mCurrentLocation;

    public synchronized void setCurrentLocation(Location location) {
        mCurrentLocation =  new LocationGPS(location.getLatitude(), location.getLongitude());;
    }

    public synchronized LocationGPS getCurrentLocation() {
        return mCurrentLocation;
    }

    public synchronized void setCurrentLocation(LocationGPS currentLocation) {
        mCurrentLocation = currentLocation;
    }

    public synchronized void setCurrentLocation(GeoPoint geoPoint) {
        mCurrentLocation = new LocationGPS(geoPoint.getLatitude(), geoPoint.getLongitude());
    }
}