package com.maurelsagbo.project_erl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.application.ERLApplication;
import com.maurelsagbo.project_erl.mapper.FlightPlanORM;
import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class FlightPDetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    protected static final String TAG = "FlightPDetailActivity";

    private Button prepare, start, pictures, locate, stop;

    private GoogleMap gMap;

    private float mSpeed = 5f;

    private double droneLocationLat, droneLocationLng;
    private Marker droneMarker = null;

    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;

    private WaypointAction photoAction = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 2);

    private List<WayPoint> waypoints;

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

        // Receive broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(ERLApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        // Add the listener for waypoint mission operator
        addListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
    }

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                // If we got here, the user's action was not recognized. Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate_btn: {
                updateDroneLocation();
                cameraUpdate();
                break;
            }
            case R.id.prepare_flight_p_btn: {
                uploadWayPointMission();
                break;
            }
            case R.id.start_flight_p_btn:{
                startWaypointMission();
                break;
            }
            case R.id.stop_flight_p_btn:{
                stopWaypointMission();
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
        waypoints = flightPlan.getWayPoints();

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
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 20f));
        }
    }

    /**
     * Method that initialize the UI by getting the buttons and setting the on click listeners
     */
    private void initUI(){
        // Get buttons
        prepare = (Button) findViewById(R.id.prepare_flight_p_btn);
        start = (Button) findViewById(R.id.start_flight_p_btn);
        stop = (Button) findViewById(R.id.stop_flight_p_btn);
        pictures = (Button) findViewById(R.id.get_pictures_btn);
        locate = (Button) findViewById(R.id.locate_btn);

        // Set on click listener
        prepare.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        pictures.setOnClickListener(this);
        locate.setOnClickListener(this);
    }

    /**
     * Method which take a string a show a toast message on screen
     * @param string
     */
    private void setResultToToast(final String string){
        FlightPDetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FlightPDetailActivity.this, string, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method which move the camera on the aircraft marker
     */
    private void cameraUpdate() {
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = 20f;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        gMap.moveCamera(cu);
    }

    //##############################################################################################################
    //################################################# DJI METHODS ################################################
    //##############################################################################################################

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
            refreshUI();
        }
    };

    /**
     * Method which is called everytime the connection status of the aircraft changes
     */
    private void onProductConnectionChange() {
        initFlightController();
    }

    /**
     * Method which get and initializes the flight controller
     */
    private void initFlightController() {
        BaseProduct product = ERLApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null){
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }

    /**
     * Method which configure the waypoint mision before take off
     */
    private void configWayPointMission() {
        // Transform the current waypoints in DJI format and add them to a list
        List<Waypoint> djiWaypoints = new ArrayList<>();
        for(WayPoint wp : waypoints){
            Waypoint mWaypoint = new Waypoint(wp.getLatitude(), wp.getLongitude(), (float)wp.getAltitude());
            mWaypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, wp.getGimbalPitch()));
            mWaypoint.addAction(photoAction);
            djiWaypoints.add(mWaypoint);
        }

        if (waypointMissionBuilder != null) {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

            // Add the DJI Waypoints list to the mission builder
            waypointMissionBuilder.waypointList(djiWaypoints);
        } else {
            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

            // Add the DJI Waypoints list to the mission builder
            waypointMissionBuilder.waypointList(djiWaypoints);
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }
    }

    /**
     * Method which upload a waypoint mission
     */
    private void uploadWayPointMission(){
        // Configure the waypoint mission
        configWayPointMission();

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("Mission upload successfully!");
                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription() + " retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });

    }

    /**
     * Method which start a waypoint mission
     */
    private void startWaypointMission(){
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });
    }

    /**
     * Method which stop a waypoint mission
     */
    private void stopWaypointMission(){
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });
    }

    /**
     * Method which checks the GPS coordinate of the aircraft
     *
     * @param latitude
     * @param longitude
     * @return true if the latitude and longitude are valid
     */
    public static boolean checkGpsCoordinates(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    /**
     * Method which update the aircraft current location
     */
    private void updateDroneLocation() {
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);

        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drone));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }
                if (checkGpsCoordinates(droneLocationLat, droneLocationLng)) {
                    droneMarker = gMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void refreshUI(){
        BaseProduct product = ERLApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            locate.setEnabled(true);
            start.setEnabled(true);
            stop.setEnabled(true);
            prepare.setEnabled(true);
            Log.v(TAG, "Connection to aircraft: True");
        } else {
            locate.setEnabled(false);
            start.setEnabled(false);
            stop.setEnabled(false);
            prepare.setEnabled(false);

            // Show message on screen
            setResultToToast("The aircraft is not connected.");
            Log.v(TAG, "Connection to aircraft: False");
        }
    }
}
