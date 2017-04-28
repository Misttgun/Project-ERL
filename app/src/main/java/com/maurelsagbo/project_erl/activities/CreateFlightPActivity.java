package com.maurelsagbo.project_erl.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.mapper.FlightPlanORM;
import com.maurelsagbo.project_erl.mapper.WayPointORM;
import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;

import java.util.ArrayList;
import java.util.List;

public class CreateFlightPActivity extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {

    protected static final String TAG = "CreateFlightPActivity";

    private GoogleMap gMap;
    private Button save, add, clear;

    private boolean isAdd = false;
    List<WayPoint> waypoints = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flight_p);

        // Initialization of UI
        initUI();

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.create_map);
        mapFragment.getMapAsync(this);

        // Initialize the create flight plan activity toolbar
        Toolbar detailToolbar = (Toolbar) findViewById(R.id.create_toolbar);
        setSupportActionBar(detailToolbar);

        // Support back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(menu != null){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_create_flight, menu);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_btn:{
                enableDisableAdd();
                break;
            }
            case R.id.clear_btn:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gMap.clear();
                        waypoints.clear();
                    }
                });
                break;
            }
            case R.id.save_btn:{
                saveFlighPlan(this);
            }
            default:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Initialization of the map
        if(gMap == null){
            gMap = googleMap;
            setUpMap();
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(isAdd){
            markWaypoint(latLng);
        } else {
            setResultToToast("Cannot add waypoint, click on ADD button.");
        }
    }

    /**
     * Method that initialize the UI by getting the buttons and setting the on click listeners
     */
    private void initUI(){
        // Get buttons
        save = (Button) findViewById(R.id.save_btn);
        add = (Button) findViewById(R.id.add_btn);
        clear = (Button) findViewById(R.id.clear_btn);

        // Set on click listener
        save.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    /**
     * Method that sets up the map
     */
    private void setUpMap(){
        // Add the listener for click for the map object
        gMap.setOnMapClickListener(this);
    }

    private void markWaypoint(LatLng point){
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        gMap.addMarker(markerOptions);
        WayPoint wayPoint = new WayPoint(waypoints.size()+1, point.latitude, point.longitude, 5.0, 0.0, 0);
        waypoints.add(wayPoint);
    }

    /**
     * Show the string to toast
     * @param string
     */
    private void setResultToToast(final String string){
        CreateFlightPActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CreateFlightPActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method that enable or disable the possibility to add waypoint to the map
     */
    private void enableDisableAdd(){
        if (isAdd == false) {
            isAdd = true;
            add.setText("Exit");
        }else{
            isAdd = false;
            add.setText("Add");
        }
    }

    /**
     * Method that show a dialog to put in the flight plan name
     */
    private void saveFlighPlan(final Context context){
        new MaterialDialog.Builder(this)
                .title("Nom du plan de vol")
                .input(R.string.fp_name_hint,R.string.fp_name_prefill ,false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        FlightPlan flightPlan;
                        if(!waypoints.isEmpty()){
                            flightPlan = new FlightPlan(waypoints, input.toString());
                            long id = FlightPlanORM.postFlightPlan(context, flightPlan);
                            for(WayPoint wp : waypoints){
                                WayPointORM.postWaypoint(context, wp, id);
                            }
                            // Put the FlightPlanActivity at the top of the backstack
                            Intent intent = new Intent(context, FlightPActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        } else {
                            Toast.makeText(CreateFlightPActivity.this, "Add waypoint first", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).show();
    }
}
