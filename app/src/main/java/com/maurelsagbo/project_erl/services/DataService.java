package com.maurelsagbo.project_erl.services;

import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;

import java.util.ArrayList;

public class DataService {

    private static DataService instance = new DataService();

    public static DataService getInstance() {
        return instance;
    }

    public ArrayList<FlightPlan> generateDummyData() {
        // This where we will put the rest request or SQLite querries
        ArrayList<FlightPlan> list = new ArrayList<>();
        FlightPlan roubaix;
        FlightPlan rostand;

        ArrayList<WayPoint> waypointRoubaix = new ArrayList<>();
//        waypointRoubaix.add(new WayPoint(0, 50.6962695, 3.1955269000000044, 29.080));
//        waypointRoubaix.add(new WayPoint(1, 50.6961001, 3.195656999999983, 29.080));
//        waypointRoubaix.add(new WayPoint(2, 50.6959566, 3.1957671999999775, 29.080));
//        waypointRoubaix.add(new WayPoint(3, 50.695794, 3.1958921000000373, 29.080));
//        waypointRoubaix.add(new WayPoint(4, 50.6956391, 3.196010200000046, 29.080));
//        waypointRoubaix.add(new WayPoint(5, 50.6954814, 3.19612230000007, 29.080));
//        waypointRoubaix.add(new WayPoint(6, 50.6953817, 3.19617930000004, 29.080));

        ArrayList<WayPoint> waypointRostand = new ArrayList<>();
//        waypointRostand.add(new WayPoint(0, 50.6954339, 3.196490600000061, 29.080));
//        waypointRostand.add(new WayPoint(1, 50.6954802, 3.1967660999999907, 29.080));
//        waypointRostand.add(new WayPoint(2, 50.6955298, 3.1970613999999387, 29.080));
//        waypointRostand.add(new WayPoint(3, 50.69558019999999, 3.197362099999964, 29.080));
//        waypointRostand.add(new WayPoint(4, 50.6956286, 3.197650299999964, 29.080));
//        waypointRostand.add(new WayPoint(5, 50.69567960000001, 3.197954299999992, 29.080));
//        waypointRostand.add(new WayPoint(6, 50.6957199, 3.1981991999999764, 29.080));

        roubaix = new FlightPlan(waypointRoubaix,"Roubaix");
        roubaix.setId(1);

        rostand = new FlightPlan(waypointRostand,"Rostand");
        rostand.setId(2);

        list.add(roubaix);
        list.add(rostand);

        return list;
    }
}
