package com.maurelsagbo.project_erl.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.mapper.FlightPlanORM;
import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;

import java.util.ArrayList;
import java.util.List;

public class FlightPDetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    protected static final String TAG = "FlightPDetailActivity";

    private Button launch_btn;
    private Button upload_btn;

    private GoogleMap gMap;

    private long intentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_p_detail);

        // Initialization of UI
        initUI();

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_details);
        mapFragment.getMapAsync(this);

        // Create the detail flight plan activity toolbar
        Toolbar detailToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(detailToolbar);

        // Support back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get the intent from the flight plan activity
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            intentID = bundle.getLong("flightPlanID");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        updateMap(intentID);
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
        switch (v.getId()) {
            case R.id.launch_flight_p_btn:{
                // Put the LaunchFlightPlanActivity at the top of the backstack
                Intent intent = new Intent(this, LaunchFlightPActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Update the map with the flight plan list of waypoints
     * @param id
     */
    public void updateMap(long id){
        List<FlightPlan> flightPlans = (ArrayList<FlightPlan>) FlightPlanORM.getFlightPlans(this);
        List<WayPoint> waypoints = new ArrayList<>();

        double longitude;
        double latitude;

        for (FlightPlan fp : flightPlans){
            if(fp.getId() == id){
                waypoints = fp.getWayPoints();
                break;
            }
        }

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

    /**
     * Method that initialize the UI by getting the buttons and setting the on click listeners
     */
    private void initUI(){
        // Get buttons
        upload_btn = (Button) findViewById(R.id.launch_flight_p_btn);
        launch_btn = (Button) findViewById(R.id.upload_pictures_btn);

        // Set on click listener
        upload_btn.setOnClickListener(this);
        launch_btn.setOnClickListener(this);
    }
}
