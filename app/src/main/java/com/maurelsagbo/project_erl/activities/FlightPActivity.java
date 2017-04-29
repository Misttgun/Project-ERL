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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

public class FlightPActivity extends AppCompatActivity {

    protected static final String TAG = "FlightPActivity";

    private RecyclerView recyclerView;
    private FlightPAdapter adapter;
    private TextView emptyText;
    private RelativeLayout loadingView;

    // Check if we can exit the application
    private Boolean exit = false;

    private static Gson gson;

    final CharSequence[] items = {"Apprentissage", "Manuelle"};

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
        loadingView = (RelativeLayout) findViewById(R.id.loading_view);
        loadingView.setVisibility(View.GONE);

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
                new MaterialDialog.Builder(FlightPActivity.this)
                        .title("Choisissez une méthode")
                        .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if(which == 0){
                                    Intent intent = new Intent(FlightPActivity.this, CreateFlightPLearningActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(FlightPActivity.this, CreateFlightPActivity.class);
                                    startActivity(intent);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateFlightPlanList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFlightPlanList();
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
                Log.i(TAG, "Affichage de la progress bar");
                loadingView.setVisibility(View.VISIBLE);
                emptyText.setText("Récupération des plan de vol en cours...");
                emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                // Get the fight plans from server and then update the map
                populateSQLite(this);

                //updateFlightPlanList();
                return true;
            case R.id.get_pictures:
                Intent intent = new Intent(FlightPActivity.this, PlaybackActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized. Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method which updates the FlightPlan list in the view
     */
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

//    private void downloadFPById(final Context context, final long id){
//        JsonObjectRequest requestWP = new JsonObjectRequest(Request.Method.GET, "http://vps361908.ovh.net/dev/elittoral/api/flightplans/" + id, null,
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            try {
//                                String wayPointJson = response.getJSONArray("waypoints").toString();
//                                List<WayPoint> wayPoints = Arrays.asList(gson.fromJson(wayPointJson, WayPoint[].class));
//                                for(WayPoint wp : wayPoints){
//                                    WayPointORM.postWaypoint(context, wp, id);
//                                }
//                            } catch (JSONException e){
//                                Log.e(TAG, "Failed to parse JSON to Waypoint list");
//                                e.printStackTrace();
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                }
//            });
//
//        MySingleton.getInstance(this).getRequestQueue().add(requestWP);
//
//    }

    /**
     * Method which download all the Flight Plans and Waypoints associated from the server
     * @param callback
     */
    private void downloadAllFlightPlans(final StringRequestCallback callback){
        StringRequest requestFP = new StringRequest(Request.Method.GET,"http://vps361908.ovh.net/dev/elittoral/api/flightplans/dump",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        // Add a timeout of 1 minute for the request
        requestFP.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(this).getRequestQueue().add(requestFP);
    }

    /**
     * Method which add the Flight Plans and Waypoint to the SQLite database after the download for the server is successfull
     * @param context
     */
    private void populateSQLite(final Context context){
        downloadAllFlightPlans(new StringRequestCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Transform the string response to JSON and get the JSON Array with key "flightplans"
                    JSONObject flightPlanJson = new JSONObject(response);
                    JSONArray fpArray = flightPlanJson.getJSONArray("flightplans");

                    for(int i = 0; i < fpArray.length(); i++){
                        // Get the first flight plan JSON Object and get the JSON Array with key "waypoints"
                        JSONObject fpObject = fpArray.getJSONObject(i);
                        postFlightPlanToBDD(fpObject.toString(), context);
                    }

                    Log.i(TAG, "Dissimulation de la progress bar");
                    loadingView.setVisibility(View.GONE);
                    Log.i(TAG, "Updating flightplan list from response");
                    updateFlightPlanList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Method which take a flight plan json object and post it to the database
     * @param json
     * @param context
     * @throws JSONException
     */
    public static void postFlightPlanToBDD(String json, Context context) throws JSONException {
        JSONObject fpObject = new JSONObject(json);
        JSONArray wpArray = fpObject.getJSONArray("waypoints");

        // Transform the flightplan JSON Object to flight plan model and post it to the Database
        FlightPlan flightPlanTemp = gson.fromJson(fpObject.toString(), FlightPlan.class);
        long id = FlightPlanORM.postFlightPlan(context, flightPlanTemp);

        if(id != -1){
            WayPoint wayPoint;

            for(int j = 0; j < wpArray.length(); j++){
                JSONObject wpObject = wpArray.getJSONObject(j);
                JSONObject parameters = wpObject.getJSONObject("parameters");
                JSONObject coord = parameters.getJSONObject("coord");
                JSONObject gimbal = parameters.getJSONObject("gimbal");
                int number = wpObject.getInt("number");
                double latitude = coord.getDouble("lat");
                double longitue = coord.getDouble("lon");
                double altitude = coord.getDouble("alt");
                double rotation = parameters.getDouble("rotation");
                int pitch = gimbal.getInt("pitch");
                wayPoint = new WayPoint(number, latitude, longitue, altitude, rotation, pitch);
                WayPointORM.postWaypoint(context,wayPoint,id);
            }
        }
    }
}
