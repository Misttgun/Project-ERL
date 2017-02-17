package com.maurelsagbo.project_erl.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.adapters.FlightPAdapter;
import com.maurelsagbo.project_erl.mapper.FlightPlanORM;
import com.maurelsagbo.project_erl.mapper.WayPointORM;
import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;
import com.maurelsagbo.project_erl.services.DataService;

import java.util.ArrayList;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

public class FlightPActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected static final String TAG = "FlightPActivity";

    private RecyclerView recyclerView;
    private FlightPAdapter adapter;
    private TextView emptyText;
    private GoogleMap mMap;

    // Check if we can exit the application
    private Boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_p);

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create the flight plan activity toolbar
        Toolbar homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);

        // Get error text view in case of an empty recycler view
        emptyText = (TextView) findViewById(R.id.empty_recycler);

        // Get the recycler view and set fixed size to true
        recyclerView = (RecyclerView) findViewById(R.id.recycler_fp_location);
        recyclerView.setHasFixedSize(true);

        // Create the adapter if the array list is not empty
        if(!DataService.getInstance().generateDummyData().isEmpty()){
            adapter = new FlightPAdapter(DataService.getInstance().generateDummyData(), this);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        }

        // Create a layout manager and set it as the recycler view layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // Get the floating action button and set on click listener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.v(TAG, "Updating map from onMapReady");
        updateMap();
    }

    @Override
    public void onBackPressed() {
        if(exit){
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(menu != null){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_flight, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_fp:
                // User chose the "Créer" action, create a new flight plan
                return true;

            default:
                // If we got here, the user's action was not recognized. Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMap(){
        ArrayList<FlightPlan> flightPlans = (ArrayList<FlightPlan>)FlightPlanORM.getFlightPlans(this);
        double longitude;
        double latitude;

        if(!flightPlans.isEmpty()){
            longitude = flightPlans.get(0).getWayPoints().get(0).getLongitude();
            latitude = flightPlans.get(0).getWayPoints().get(0).getLatitude();
            LatLng temp = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 8f));
        }

        for(FlightPlan fp : flightPlans){
            longitude = fp.getWayPoints().get(0).getLongitude();
            latitude = fp.getWayPoints().get(0).getLatitude();
            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude));
            marker.title(fp.getLocationName());
            mMap.addMarker(marker);
        }
    }

    private void populateSQLite(){
        ArrayList<FlightPlan> flightPlans = DataService.getInstance().generateDummyData();
        ArrayList<WayPoint> wayPoints = new ArrayList<>();

        for(FlightPlan fp : flightPlans){
            FlightPlanORM.postFlightPlan(this, fp);
        }

        for(FlightPlan fp : flightPlans){
            wayPoints = fp.getWayPoints();
            for(WayPoint wp : wayPoints){
                WayPointORM.postWaypoint(this, wp, fp);
            }
        }
    }
}
