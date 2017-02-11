package com.maurelsagbo.project_erl.models;

public class WayPoint {

    private int position;
    private double latitude;
    private double longitude;
    private double altitude;

    public WayPoint(int position, double latitude, double longitude, double altitude) {
        this.position = position;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public int getPosition() {
        return position;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }
}
