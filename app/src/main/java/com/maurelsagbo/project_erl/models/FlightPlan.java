package com.maurelsagbo.project_erl.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FlightPlan {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String locationName;

    @SerializedName("waypoints")
    private List<WayPoint> wayPoints;

    public FlightPlan(List<WayPoint> wayPoints, String locationName){
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

    public List<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(List<WayPoint> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
