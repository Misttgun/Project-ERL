package com.maurelsagbo.project_erl.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.adapters.FlightPAdapter;
import com.maurelsagbo.project_erl.mapper.FlightPlanORM;
import com.maurelsagbo.project_erl.mapper.WayPointORM;
import com.maurelsagbo.project_erl.models.FlightPlan;
import com.maurelsagbo.project_erl.models.WayPoint;
import com.maurelsagbo.project_erl.utilities.MySingleton;
import com.maurelsagbo.project_erl.utilities.StringRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

public class FlightPActivity extends AppCompatActivity {

    protected static final String TAG = "FlightPActivity";

    private RecyclerView recyclerView;
    private FlightPAdapter adapter;
    private TextView emptyText;
    private ProgressBar pb;

    // Check if we can exit the application
    private Boolean exit = false;

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_p);

        // Creation du Gson Builder
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        // Create the flight plan activity toolbar
        Toolbar homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);

        // Get error text view in case of an empty recycler view
        emptyText = (TextView) findViewById(R.id.empty_recycler);

        // Get the progress bar
        pb = (ProgressBar) findViewById(R.id.pb_loading);

        // Get the recycler view and set fixed size to true
        recyclerView = (RecyclerView) findViewById(R.id.recycler_fp_location);
        recyclerView.setHasFixedSize(true);

        List<FlightPlan> flightPlans = FlightPlanORM.getFlightPlans(this);

        // Create the adapter if the array list is not empty
        adapter = new FlightPAdapter(new ArrayList<FlightPlan>(), this);
        recyclerView.setAdapter(adapter);

        if(!flightPlans.isEmpty()){
            adapter.updateItems(flightPlans);
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
                Intent intent = new Intent(FlightPActivity.this, CreateFlightPActivity.class);
                startActivity(intent);
            }
        });
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
                // Get the fight plans from server and then update the map
                populateSQLite(this);

                //updateFlightPlanList();
                return true;

            default:
                // If we got here, the user's action was not recognized. Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateFlightPlanList(){
        List<FlightPlan> flightPlans = FlightPlanORM.getFlightPlans(this);

        // Update the adapter
        if(!flightPlans.isEmpty()){
            adapter.updateItems(flightPlans);
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    private void downloadFPById(final Context context, final long id){
        JsonObjectRequest requestWP = new JsonObjectRequest(Request.Method.GET, "http://vps361908.ovh.net/elittoral/api/flightplans/" + id, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String wayPointJson = response.getJSONArray("waypoints").toString();
                                Log.i(TAG, wayPointJson);
                                List<WayPoint> wayPoints = Arrays.asList(gson.fromJson(wayPointJson, WayPoint[].class));
                                for(WayPoint wp : wayPoints){
                                    WayPointORM.postWaypoint(context, wp, id);
                                }
                            } catch (JSONException e){
                                Log.e(TAG, "Failed to parse JSON to Waypoint list");
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

        MySingleton.getInstance(this).getRequestQueue().add(requestWP);

    }

    private void downloadFlightPlan(final StringRequestCallback callback){
        StringRequest requestFP = new StringRequest(Request.Method.GET,"http://vps361908.ovh.net/elittoral/api/flightplans/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Affichage de la progress bar");
                        pb.setVisibility(ProgressBar.VISIBLE);
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        MySingleton.getInstance(this).getRequestQueue().add(requestFP);
    }

    private void populateSQLite(final Context context){
        downloadFlightPlan(new StringRequestCallback() {
            @Override
            public void onSuccess(String response) {
                List<FlightPlan> tempFlightPlans = Arrays.asList(gson.fromJson(response, FlightPlan[].class));
                Log.i(TAG, response);
                for(FlightPlan fp : tempFlightPlans){
                    long id = FlightPlanORM.postFlightPlan(context, fp);
                    if(id != -1){
                        downloadFPById(context, fp.getId());
                    }
                }
                Log.i(TAG, "Dissimulation de la progress bar");
                pb.setVisibility(ProgressBar.INVISIBLE);
                Log.i(TAG, "Updating flightplan list from response");
                updateFlightPlanList();
            }
        });
    }
}
