package com.maurelsagbo.project_erl.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.mapper.FlightPlanORM;
import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.missionmanager.DJIMissionManager;
import dji.sdk.missionmanager.DJIWaypointMission;

public class LaunchFlightPActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    protected static final String TAG = "LaunchFlightPActivity";

    private GoogleMap gMap;
    private Button capture, stop;

    List<WayPoint> waypoints = new ArrayList<>();
    private Marker droneMarker = null;

    private float mSpeed = 10.0f;

    private long intentID;

    private DJIWaypointMission mWaypointMission;
    private DJIMissionManager mMissionManager;
    private DJIFlightController mFlightController;

    private DJIWaypointMission.DJIWaypointMissionFinishedAction mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction;
    private DJIWaypointMission.DJIWaypointMissionHeadingMode mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_flight_p);

        // Initialization of UI
        initUI();

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_launch);
        mapFragment.getMapAsync(this);

        // Initialize the create flight plan activity toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.launch_toolbar);
        setSupportActionBar(toolbar);

        // Support back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the intent from the flight plan activity
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            intentID = bundle.getLong("flightPlanID");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(menu != null){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_detail_flight, menu);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Initialization of the map
        if(gMap == null){
            gMap = googleMap;
        }
        Log.i(TAG, "Appel Update Map avec Intent ID " + intentID);
        updateMap(intentID);
    }

    /**
     * Method that initialize the UI by getting the buttons and setting the on click listeners
     */
    private void initUI(){
        // Get buttons
        capture = (Button) findViewById(R.id.picture_btn);
        stop = (Button) findViewById(R.id.stop_flight_p_btn);

        // Set on click listener
        capture.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    /**
     * Show the string to toast
     * @param string
     */
    private void setResultToToast(final String string){
        LaunchFlightPActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LaunchFlightPActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Update the map with the flight plan list of waypoints
     * @param id
     */
    public void updateMap(long id){
        FlightPlan flightPlan = FlightPlanORM.getFlightPlanId(this, id);
        List<WayPoint> waypoints = flightPlan.getWayPoints();

        double longitude;
        double latitude;

        for (WayPoint wp : waypoints){
            latitude = wp.getLatitude();
            longitude = wp.getLongitude();
            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude));
            gMap.addMarker(marker);
        }

        if(!waypoints.isEmpty()){
            longitude = waypoints.get(0).getLongitude();
            latitude = waypoints.get(0).getLatitude();
            LatLng temp = new LatLng(latitude, longitude);
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 17f));
        }
    }
}
