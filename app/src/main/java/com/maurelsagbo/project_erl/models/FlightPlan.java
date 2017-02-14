package com.maurelsagbo.project_erl.models;

import java.util.ArrayList;
import java.util.Date;

public class FlightPlan {

    private long id;

    private String mLocationName;
    private ArrayList<WayPoint> wayPoints;

    private Date date;

    public FlightPlan(ArrayList<WayPoint> wayPoints, String locationName){
        this.mLocationName = locationName;
        this.wayPoints = new ArrayList<>(wayPoints);
    }

    public FlightPlan(){

    }

    public String getLocationName() {
        return mLocationName;
    }

    public void setLocationName(String name){
        this.mLocationName = name;
    }

    public ArrayList<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
