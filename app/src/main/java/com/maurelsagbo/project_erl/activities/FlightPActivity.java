package com.maurelsagbo.project_erl.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.adapters.FlightPAdapter;
import com.maurelsagbo.project_erl.models.FlightPData;
import com.maurelsagbo.project_erl.services.DataService;

import java.util.ArrayList;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

public class FlightPActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    protected static final String TAG = "FlightPActivity";
    final int PERMISSION_LOCATION = 111;

    private RecyclerView recyclerView;
    private FlightPAdapter adapter;
    private TextView emptyText;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    // Check if we can exit the application
    private Boolean exit = false;

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_p);

        // Setting up goolge play services integration
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

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
        if(!DataService.getInstance().getFlightPlans().isEmpty()){
            adapter = new FlightPAdapter(DataService.getInstance().getFlightPlans());
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            Log.v(TAG, "Requesting permissions");
        } else {
            Log.v(TAG, "Updating the map from onConnected");
            updateMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

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
        ArrayList<FlightPData> flightPlans = DataService.getInstance().getFlightPlans();

        if(!flightPlans.isEmpty()){
            LatLng temp = new LatLng(flightPlans.get(0).getLatitude(), flightPlans.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 6f));
        }

        for(FlightPData fp : flightPlans){
            MarkerOptions marker = new MarkerOptions().position(new LatLng(fp.getLatitude(), fp.getLongitude()));
            marker.title(fp.getLocationName());
            mMap.addMarker(marker);
        }
    }
}
