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

    public WayPoint(){

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

    public void setPosition(int position) {
        this.position = position;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
