package com.maurelsagbo.project_erl.models;

public class FlightPData {

    private String mLocationName;
    private float mLatitude;
    private float mLongitude;

    public FlightPData(float latitude, float longitude, String locationName){
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mLocationName = locationName;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public float getLongitude() {
        return mLongitude;
    }
}
