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

import java.util.List;

public class FlightPDetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    protected static final String TAG = "FlightPDetailActivity";

    private Button prepare, start, upload, locate;

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
        // Initialization of the map
        if(gMap == null){
            gMap = googleMap;
        }
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
            case R.id.start_flight_p_btn:{
                // Put the LaunchFlightPlanActivity at the top of the backstack
                Intent intent = new Intent(this, LaunchFlightPActivity.class);
                intent.putExtra("flightPlanID", intentID);
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

    /**
     * Method that initialize the UI by getting the buttons and setting the on click listeners
     */
    private void initUI(){
        // Get buttons
        prepare = (Button) findViewById(R.id.prepare_flight_p_btn);
        start = (Button) findViewById(R.id.start_flight_p_btn);
        upload = (Button) findViewById(R.id.stop_flight_p_btn);
        locate = (Button) findViewById(R.id.locate_btn);

        // Set on click listener
        prepare.setOnClickListener(this);
        start.setOnClickListener(this);
        upload.setOnClickListener(this);
        locate.setOnClickListener(this);
    }
}
