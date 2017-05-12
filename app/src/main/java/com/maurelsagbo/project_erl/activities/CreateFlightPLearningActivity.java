package com.maurelsagbo.project_erl.activities;

import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.application.ERLApplication;
import com.maurelsagbo.project_erl.models.WayPoint;
import com.maurelsagbo.project_erl.utilities.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class CreateFlightPLearningActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private static final String TAG = CreateFlightPLearningActivity.class.getName();

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;
    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    private FlightController mFlightController;

    protected TextureView mVideoSurface = null;
    private Button mConfigFP, mAddWaypoint, mClearFP;

    private List<WayPoint> waypoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flight_p_learning);
        initUI();

        // Initialize the create flight plan activity toolbar
        Toolbar detailToolbar = (Toolbar) findViewById(R.id.create_learn_toolbar);
        setSupportActionBar(detailToolbar);

        // Support back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if(mCodecManager != null){
                    // Send the raw H264 video data to codec manager for decoding
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }else {
                    Log.e(TAG, "mCodecManager is null");
                }
            }
        };
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

    protected  void onProductChange(){
        initPreviewer();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();
        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onReturn(View view){
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        mAddWaypoint = (Button)findViewById(R.id.add_wp_button);
        mConfigFP = (Button)findViewById(R.id.config_fp_button);
        mClearFP = (Button)findViewById(R.id.clear_fp_button);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mConfigFP.setOnClickListener(this);
        mConfigFP.setEnabled(false);
        mAddWaypoint.setOnClickListener(this);
        mClearFP.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_wp_button:{
                if(waypoints.size() <= 2){
                    addWaypoint();
                } else {
                    showToast("Vous pouvez ajouter 2 points au maximum.");
                }
                break;
            }
            case R.id.config_fp_button:{
                showConfigDialog();
                break;
            }
            case R.id.clear_fp_button:{
                waypoints.clear();
                if(mConfigFP.isEnabled()){
                    mConfigFP.setEnabled(false);
                }
                if(!mAddWaypoint.isEnabled()){
                    mAddWaypoint.setEnabled(true);
                }
            }
            default:
                break;
        }
    }

    private void initPreviewer() {
        BaseProduct product = ERLApplication.getProductInstance();
        if (product == null || !product.isConnected()) {
            showToast("Aircraft is disconnected");
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }

            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size() > 0) {
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = ERLApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
    }

    private void addWaypoint(){
        BaseProduct product = ERLApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
        if (mFlightController != null) {
            WayPoint wayPoint = new WayPoint();
            FlightControllerState state = mFlightController.getState();
            wayPoint.setLatitude(state.getAircraftLocation().getLatitude());
            wayPoint.setLongitude(state.getAircraftLocation().getLongitude());
            wayPoint.setAltitude(state.getAircraftLocation().getAltitude());
            wayPoint.setRotation(state.getAttitude().yaw);

            waypoints.add(wayPoint);
            showToast(wayPoint.toString());
        }

        if(waypoints.size() == 2){
            mConfigFP.setEnabled(true);
            mAddWaypoint.setEnabled(false);
        }
    }

    private void showConfigDialog(){
        LinearLayout fPSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_fp_builder, null);
        final EditText name = (EditText)fPSettings.findViewById(R.id.name_value);

        final TextView pitchValue = (TextView)fPSettings.findViewById(R.id.gimbal_value);
        final TextView hincValue = (TextView)fPSettings.findViewById(R.id.hinc_value);
        final TextView vincValue = (TextView)fPSettings.findViewById(R.id.vinc_value);

        CrystalSeekbar pitchSeekBar = (CrystalSeekbar) fPSettings.findViewById(R.id.gimbal_seekbar);
        CrystalSeekbar hincSeekBar = (CrystalSeekbar)fPSettings.findViewById(R.id.hinc_seekbar);
        CrystalSeekbar vincSeekBar = (CrystalSeekbar)fPSettings.findViewById(R.id.vinc_seekbar);

        // Set seekbar listeners
        pitchSeekBar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                pitchValue.setText(String.valueOf(value));
            }
        });

        hincSeekBar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                hincValue.setText(String.valueOf(value));
            }
        });

        vincSeekBar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                vincValue.setText(String.valueOf(value));
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setView(fPSettings)
                .setPositiveButton("Valider",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject jsonBody = createFlightPlanJson(name.getText().toString().trim(),
                                Integer.parseInt(hincValue.getText().toString()),
                                Integer.parseInt(vincValue.getText().toString()),
                                Integer.parseInt(pitchValue.getText().toString()));
                        postFlightPlanString(jsonBody);
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // If the name is empty, the positive button will stay disabled
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    (dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    (dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    /**
     * Method which help creates the json before posting to the server
     * @param fpName
     * @param horizontalInc
     * @param verticalInc
     * @param gimbalPitch
     * @return the JSON in string format
     */
    private JSONObject createFlightPlanJson(String fpName, int horizontalInc, int verticalInc, int gimbalPitch){
        // Create all the JSONObject
        JSONObject body = new JSONObject();
        JSONObject gimbalObject = new JSONObject();
        JSONObject coord1Object = new JSONObject();
        JSONObject coord2Object = new JSONObject();

        try{
            // Working on the Gimbal JSONObject
            gimbalObject.put("yaw", 0);
            gimbalObject.put("roll", 0);
            gimbalObject.put("pitch", -gimbalPitch);

            // Working on the Coord1 JSONObject
            coord1Object.put("lon", waypoints.get(0).getLongitude());
            coord1Object.put("lat", waypoints.get(0).getLatitude());

            // Working on the Coord2 JSONObject
            coord2Object.put("lon", waypoints.get(1).getLongitude());
            coord2Object.put("lat", waypoints.get(1).getLatitude());

            // Working on the Body JSONObject
            body.put("alt_end", waypoints.get(1).getAltitude());
            body.put("alt_start", waypoints.get(0).getAltitude());
            body.put("d_gimbal", gimbalObject);
            body.put("d_rotation", waypoints.get(0).getRotation());
            body.put("coord1", coord1Object);
            body.put("v_increment", verticalInc);
            body.put("coord2", coord2Object);
            body.put("h_increment", horizontalInc);
            body.put("save", true);
            body.put("flightplan_name", fpName);

            // Return the body request string
            return body;
        }catch (JSONException e) {
            Log.d(TAG, "Erreur de JSON");
            e.printStackTrace();
        }
        return null;
    }

    private void postFlightPlanString(final JSONObject body){
        String url = "http://vps361908.ovh.net/dev/elittoral/api/flightplans/build";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            FlightPActivity.postFlightPlanToBDD(response.toString(), CreateFlightPLearningActivity.this);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        onBackPressed();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //error.printStackTrace();
                        String json;

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400:
                                    json = new String(response.data);
                                    if(json != null) Log.e(TAG,json);
                                    break;
                            }
                            //Additional cases
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        MySingleton.getInstance(this).getRequestQueue().add(postRequest);
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(CreateFlightPLearningActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
