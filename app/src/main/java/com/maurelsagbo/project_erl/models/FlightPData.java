package com.maurelsagbo.project_erl.models;

import java.util.ArrayList;

public class FlightPData {

    private String mLocationName;
    private int id;
    private ArrayList<WayPoint> wayPoints;

    public FlightPData(ArrayList<WayPoint> wayPoints, String locationName){
        this.mLocationName = locationName;
        this.wayPoints = wayPoints;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public ArrayList<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
