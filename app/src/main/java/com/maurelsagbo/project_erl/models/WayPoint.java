package com.maurelsagbo.project_erl.models;

import com.google.gson.annotations.SerializedName;

public class WayPoint {

    @SerializedName("number")
    private int position;

    @SerializedName("lat")
    private double latitude;
    @SerializedName("lon")
    private double longitude;
    @SerializedName("alt")
    private double altitude;
    @SerializedName("rotation")
    private double rotation;
    @SerializedName("pitch")
    private int gimbalPitch;

    public WayPoint(int position, double latitude, double longitude, double altitude) {
        this.position = position;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.rotation = 0.0;
        this.gimbalPitch = 0;
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

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public int getGimbalPitch() {
        return gimbalPitch;
    }

    public void setGimbalPitch(int gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }
}
