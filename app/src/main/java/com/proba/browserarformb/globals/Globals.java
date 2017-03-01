package com.proba.browserarformb.globals;

import android.app.Application;
import android.util.Log;

import com.proba.browserarformb.model.LocationGPS;

import org.osmdroid.util.GeoPoint;

public class Globals extends Application {

    private static final String TAG = "Cip";

    private LocationGPS mCurrentLocation;

    public LocationGPS getCurrentLocation() {
        return mCurrentLocation;
    }

    public void setCurrentLocation(LocationGPS currentLocation) {
        mCurrentLocation = currentLocation;
    }

    public void setCurrentLocation(GeoPoint geoPoint) {
        mCurrentLocation = new LocationGPS(geoPoint.getLatitude(), geoPoint.getLongitude());
        Log.d(TAG, "iar am dat click pe lat:" + geoPoint.getLatitude() + ", lng:" + geoPoint.getLongitude());
    }
}