package com.proba.browserarformb.model;


public class LocationGPS {

    private double mLatitude;
    private double mLongitude;

    public LocationGPS(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public LocationGPS() {
        mLatitude = 0;
        mLongitude = 0;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }
}