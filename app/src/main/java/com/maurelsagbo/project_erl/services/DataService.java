package com.maurelsagbo.project_erl.services;

import com.maurelsagbo.project_erl.models.FlightPData;

import java.util.ArrayList;

public class DataService {

    private static DataService instance = new DataService();

    public static DataService getInstance() {
        return instance;
    }

    public ArrayList<FlightPData> getFlightPlans(){
        // This where we will put the rest request or SQLite querries
        ArrayList<FlightPData> list = new ArrayList<>();
//        list.add(new FlightPData(50.692705f, 3.177847f, "Roubaix"));
//        list.add(new FlightPData(50.545332f, 3.026566f, "Seclin"));
//        list.add(new FlightPData(50.695307f, 3.195721f, "Lycée Jean Rostand Roubaix"));
//        list.add(new FlightPData(50.692705f, 3.177847f, "Roubaix"));
//        list.add(new FlightPData(50.545332f, 3.026566f, "Seclin"));
//        list.add(new FlightPData(50.695307f, 3.195721f, "Lycée Jean Rostand Roubaix"));
//        list.add(new FlightPData(50.692705f, 3.177847f, "Roubaix"));
//        list.add(new FlightPData(50.545332f, 3.026566f, "Seclin"));
//        list.add(new FlightPData(50.695307f, 3.195721f, "Lycée Jean Rostand Roubaix"));

        return list;
    }
}
