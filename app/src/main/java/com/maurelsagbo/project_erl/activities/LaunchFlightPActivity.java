package com.maurelsagbo.project_erl.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.models.WayPoint;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.missionmanager.DJIMissionManager;
import dji.sdk.missionmanager.DJIWaypointMission;

public class LaunchFlightPActivity extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {

    protected static final String TAG = "LaunchFlightPActivity";

    private GoogleMap gMap;
    private Button prepare, start, stop, locate;

    List<WayPoint> waypoints = new ArrayList<>();
    private Marker droneMarker = null;

    private float mSpeed = 10.0f;

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

        // Initialize the create flight plan activity toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.launch_toolbar);
        setSupportActionBar(toolbar);

        // Support back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    /**
     * Method that initialize the UI by getting the buttons and setting the on click listeners
     */
    private void initUI(){
        // Get buttons
        prepare = (Button) findViewById(R.id.prepare_flight_p_btn);
        start = (Button) findViewById(R.id.start_flight_p_btn);
        stop = (Button) findViewById(R.id.stop_flight_p_btn);
        locate = (Button) findViewById(R.id.locate_btn);

        // Set on click listener
        prepare.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        locate.setOnClickListener(this);
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

}
