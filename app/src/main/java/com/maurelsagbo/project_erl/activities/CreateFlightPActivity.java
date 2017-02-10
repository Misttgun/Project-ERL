package com.maurelsagbo.project_erl.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.maurelsagbo.project_erl.R;

public class CreateFlightPActivity extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {

    protected static final String TAG = "CreateFlightPActivity";
    private GoogleMap gMap;
    private Button save, add, clear;

    public CreateFlightPActivity() {
        // Required empty public constructor
    }

    public static CreateFlightPActivity newInstance() {
        CreateFlightPActivity fragment = new CreateFlightPActivity();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_create_flight_p, container, false);

        return view;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

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
}
