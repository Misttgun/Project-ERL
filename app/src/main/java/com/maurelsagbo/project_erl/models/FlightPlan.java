package com.maurelsagbo.project_erl.models;

import java.util.ArrayList;

public class FlightPlan {

    private long id;

    private String locationName;
    private ArrayList<WayPoint> wayPoints;

    public FlightPlan(ArrayList<WayPoint> wayPoints, String locationName){
        this.locationName = locationName;
        this.wayPoints = new ArrayList<>(wayPoints);
    }

    public FlightPlan(){

    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String name){
        this.locationName = name;
    }

    public ArrayList<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(ArrayList<WayPoint> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
