package com.proba.browserarformb.model;


public class LocationGPS {

    private double mLatitude;
    private double mLongitude;

    public LocationGPS(){
    }

    public LocationGPS(double latitude, double longitude) {
        setLocationGPS(latitude, longitude);
    }

    public LocationGPS getLocationGPS() {
        return new LocationGPS(mLatitude, mLongitude);
    }

    public void setLocationGPS(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}