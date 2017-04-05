package com.maurelsagbo.project_erl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.missionmanager.DJIMission;
import dji.sdk.missionmanager.DJIMissionManager;
import dji.sdk.missionmanager.DJIWaypoint;
import dji.sdk.missionmanager.DJIWaypointMission;
import dji.sdk.products.DJIAircraft;

public class FlightPDetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DJIMissionManager.MissionProgressStatusCallback, DJICommonCallbacks.DJICompletionCallback {

    protected static final String TAG = "FlightPDetailActivity";

    private Button prepare, start, upload, locate, stop;

    private GoogleMap gMap;

    private float mSpeed = 3f;
    private float mAltitude = 5f;

    private double droneLocationLat, droneLocationLng;
    private Marker droneMarker = null;

    private DJIWaypointMission.DJIWaypointMissionFinishedAction mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.GoFirstWaypoint;
    private DJIWaypointMission.DJIWaypointMissionHeadingMode mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto;
    private DJIWaypointMission mWaypointMission;
    private DJIMissionManager mMissionManager;
    private DJIWaypoint.DJIWaypointAction photoAction = new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 2);
    private DJIWaypoint.DJIWaypointAction gimbalAction = new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, -90);

    private DJIFlightController mFlightController;

    private DJIBaseProduct product;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
        initMissionManager();
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
            case R.id.get_aircraft:
                // Check aircraft connection and refresh UI
                refreshUI();

                return true;

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
                prepareWayPointMission();
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
        upload = (Button) findViewById(R.id.upload_pictures_btn);
        locate = (Button) findViewById(R.id.locate_btn);

        // Disable buttons by defaults
        locate.setEnabled(false);
        start.setEnabled(false);
        stop.setEnabled(false);
        upload.setEnabled(false);
        prepare.setEnabled(false);

        // Set on click listener
        prepare.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        upload.setOnClickListener(this);
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
        initMissionManager();
    }

    /**
     * Method which initialize the mission manager
     */
    private void initMissionManager() {
        product = ERLApplication.getProductInstance();
        if (product == null || !product.isConnected()) {
            mMissionManager = null;
            return;
        } else {
            mMissionManager = product.getMissionManager();
            mMissionManager.setMissionProgressStatusCallback(this);
            mMissionManager.setMissionExecutionFinishedCallback(this);
        }

        mWaypointMission = new DJIWaypointMission();
    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus progressStatus) {

    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void onResult(DJIError error) {
        setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
    }

    /**
     * Method which get and initializes the flight controller
     */
    private void initFlightController() {
        product = ERLApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) product).getFlightController();
            }
        }
        if (mFlightController != null) {
            mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                @Override
                public void onResult(DJIFlightControllerCurrentState state) {
                    droneLocationLat = state.getAircraftLocation().getLatitude();
                    droneLocationLng = state.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }
    }

    /**
     * Method which configure the waypoint mision before take off
     */
    private void configWayPointMission() {
        if (mWaypointMission != null) {
            mWaypointMission.finishedAction = mFinishedAction;
            mWaypointMission.headingMode = mHeadingMode;
            mWaypointMission.autoFlightSpeed = mSpeed;

            for(WayPoint wp : waypoints){
                DJIWaypoint mWaypoint = new DJIWaypoint(wp.getLatitude(), wp.getLongitude(), mAltitude);
                mWaypoint.addAction(gimbalAction);
                mWaypoint.addAction(photoAction);

                //Add waypoints to Waypoint arraylist;
                mWaypointMission.addWaypoint(mWaypoint);
            }
        }
    }

    /**
     * Method which prepare a waypoint mission
     */
    private void prepareWayPointMission(){
        // Configure the waypoint mission
        configWayPointMission();
        if (mMissionManager != null && mWaypointMission != null) {
            DJIMission.DJIMissionProgressHandler progressHandler = new DJIMission.DJIMissionProgressHandler() {
                @Override
                public void onProgress(DJIMission.DJIProgressType type, float progress) {
                }
            };

            mMissionManager.prepareMission(mWaypointMission, progressHandler, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    setResultToToast(error == null ? "Mission Prepare Successfully" : error.getDescription());
                }
            });
        }

    }

    /**
     * Method which start a waypoint mission
     */
    private void startWaypointMission(){
        if (mMissionManager != null) {
            mMissionManager.startMissionExecution(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
                }
            });

        } else {
            setResultToToast("Prepare Mission First!");
        }
    }

    /**
     * Method which stop a waypoint mission
     */
    private void stopWaypointMission(){
        if (mMissionManager != null) {
            mMissionManager.stopMissionExecution(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
                }
            });

            if (mWaypointMission != null){
                mWaypointMission.removeAllWaypoints();
            }
        }
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
        product = ERLApplication.getProductInstance();
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
