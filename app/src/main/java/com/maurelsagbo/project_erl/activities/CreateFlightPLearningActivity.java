package com.maurelsagbo.project_erl.activities;

import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.application.ERLApplication;
import com.maurelsagbo.project_erl.models.WayPoint;
import com.maurelsagbo.project_erl.utilities.InputFilterMax;
import com.maurelsagbo.project_erl.utilities.OnFocusChangeListenerMin;

import java.util.ArrayList;
import java.util.List;

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
        //mConfigFP.setEnabled(false);
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

//            mFlightController.setStateCallback(new FlightControllerState.Callback() {
//                WayPoint wayPoint;
//                @Override
//                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
//                    wayPoint.setLatitude(djiFlightControllerCurrentState.getAircraftLocation().getLatitude());
//                    wayPoint.setLongitude(djiFlightControllerCurrentState.getAircraftLocation().getLongitude());
//                    wayPoint.setAltitude(djiFlightControllerCurrentState.getAircraftLocation().getAltitude());
//                    wayPoint.setRotation(djiFlightControllerCurrentState.getAttitude().yaw);
//                }
//            });

        }

        if(waypoints.size() == 2){
            mConfigFP.setEnabled(true);
        }
    }

    private void showConfigDialog(){
        LinearLayout fPSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_fp_builder, null);
        EditText name = (EditText)fPSettings.findViewById(R.id.name_value);

        EditText pitch = (EditText)fPSettings.findViewById(R.id.pitch_value);
        pitch.setFilters(new InputFilter[]{ new InputFilterMax(CreateFlightPLearningActivity.this, "90")});
        pitch.setOnFocusChangeListener(new OnFocusChangeListenerMin(getApplicationContext(), 0));

        EditText vinc = (EditText)fPSettings.findViewById(R.id.vinc_value);
        vinc.setFilters(new InputFilter[]{ new InputFilterMax(CreateFlightPLearningActivity.this, "5")});
        vinc.setOnFocusChangeListener(new OnFocusChangeListenerMin(getApplicationContext(), 1));

        EditText hinc = (EditText)fPSettings.findViewById(R.id.hinc_value);
        hinc.setFilters(new InputFilter[]{ new InputFilterMax(CreateFlightPLearningActivity.this, "15")});
        hinc.setOnFocusChangeListener(new OnFocusChangeListenerMin(getApplicationContext(), 5));

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(fPSettings)
                .setPositiveButton("Valider",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(CreateFlightPLearningActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
