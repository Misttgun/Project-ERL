package com.maurelsagbo.project_erl.activities;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.application.ERLApplication;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.PlaybackManager;
import dji.sdk.camera.PlaybackManager.PlaybackState;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;


public class PlaybackActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private static final String TAG = PlaybackActivity.class.getName();

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    protected TextureView mVideoSurface = null;

    private Button mPreviousBtn, mNextBtn;
    private Button mPreviewBtn1, mPreviewBtn2, mPreviewBtn3, mPreviewBtn4, mPreviewBtn5, mPreviewBtn6, mPreviewBtn7, mPreviewBtn8;
    private ToggleButton mPreviewModeBtn;

    private boolean isSinglePreview = true;
    private PlaybackState mPlaybackState;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        // Initialize the UI
        initUI();

        // Create the playback activity toolbar
        Toolbar playbackToolbar = (Toolbar) findViewById(R.id.playback_toolbar);
        setSupportActionBar(playbackToolbar);

        // Support back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        initCameraCallBacks();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        uninitPreviewer();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
        switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
        uninitPreviewer();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
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

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);

        mPreviousBtn = (Button) findViewById(R.id.btn_previous_btn);
        mNextBtn = (Button) findViewById(R.id.btn_next_btn);

        // Get the Preview Toggle Button and set on check listener
        mPreviewModeBtn = (ToggleButton) findViewById(R.id.btn_preview_btn);
        mPreviewModeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!isSinglePreview)
                        mCamera.getPlaybackManager().enterSinglePreviewModeWithIndex(0);
                }else{
                    if (isSinglePreview)
                        mCamera.getPlaybackManager().enterMultiplePreviewMode();
                }
            }
        });

        // Change the toggle status if we are in single preview or not
        if(isSinglePreview){
            mPreviewModeBtn.setChecked(true);
        }else{
            mPreviewModeBtn.setChecked(false);
        }

        mPreviewBtn1 = (Button) findViewById(R.id.preview_button1);
        mPreviewBtn2 = (Button) findViewById(R.id.preview_button2);
        mPreviewBtn3 = (Button) findViewById(R.id.preview_button3);
        mPreviewBtn4 = (Button) findViewById(R.id.preview_button4);
        mPreviewBtn5 = (Button) findViewById(R.id.preview_button5);
        mPreviewBtn6 = (Button) findViewById(R.id.preview_button6);
        mPreviewBtn7 = (Button) findViewById(R.id.preview_button7);
        mPreviewBtn8 = (Button) findViewById(R.id.preview_button8);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mPreviousBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);

        mPreviewBtn1.setOnClickListener(this);
        mPreviewBtn2.setOnClickListener(this);
        mPreviewBtn3.setOnClickListener(this);
        mPreviewBtn4.setOnClickListener(this);
        mPreviewBtn5.setOnClickListener(this);
        mPreviewBtn6.setOnClickListener(this);
        mPreviewBtn7.setOnClickListener(this);
        mPreviewBtn8.setOnClickListener(this);
    }

    private void initPreviewer() {

        BaseProduct product = ERLApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast("Aircraft is disconnected");
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }

            mCamera = product.getCamera();

            // Set the preview mode to multiple
            mCamera.getPlaybackManager().enterMultiplePreviewMode();

            // Set the camera to playback mode
            switchCameraMode(SettingsDefinitions.CameraMode.PLAYBACK);

            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size() > 0) {
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {

        if (ERLApplication.isCameraModuleAvailable()){
            if (mCamera != null){
                // Reset the callback
                VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
            }
        }
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
        Log.e(TAG, "onSurfaceTextureSizeChanged");
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

    protected void initCameraCallBacks() {
        if (ERLApplication.isPlaybackAvailable()){

            if (mCamera != null) {

                mCamera.getPlaybackManager().setPlaybackStateCallback(new PlaybackManager.PlaybackState.CallBack() {
                    @Override
                    public void onUpdate(PlaybackManager.PlaybackState playbackState) {

                        if (null != playbackState) {

                            if (playbackState.getPlaybackMode().equals(SettingsDefinitions.
                                    PlaybackMode.MULTIPLE_MEDIA_FILE_PREVIEW) ||
                                    playbackState.getPlaybackMode().equals(SettingsDefinitions.
                                            PlaybackMode.MEDIA_FILE_DOWNLOAD) ||
                                    playbackState.getPlaybackMode().equals(SettingsDefinitions.
                                            PlaybackMode.MULTIPLE_FILES_EDIT)) {
                                isSinglePreview = false;
                            } else {
                                isSinglePreview = true;
                            }

                            mPlaybackState = playbackState;
                        }
                    }
                });
            }
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PlaybackActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous_btn: {
                if (isSinglePreview) {
                    mCamera.getPlaybackManager().proceedToPreviousSinglePreviewPage();
                } else {
                    mCamera.getPlaybackManager().proceedToPreviousMultiplePreviewPage();
                }
                break;
            }
            case R.id.btn_next_btn: {
                if (isSinglePreview) {
                    mCamera.getPlaybackManager().proceedToNextSinglePreviewPage();
                } else {
                    mCamera.getPlaybackManager().proceedToNextMultiplePreviewPage();
                }
                break;
            }
            case R.id.preview_button1: {
                previewBtnAction(0);
                break;
            }
            case R.id.preview_button2: {
                previewBtnAction(1);
                break;
            }
            case R.id.preview_button3: {
                previewBtnAction(2);
                break;
            }
            case R.id.preview_button4: {
                previewBtnAction(3);
                break;
            }
            case R.id.preview_button5: {
                previewBtnAction(4);
                break;
            }
            case R.id.preview_button6: {
                previewBtnAction(5);
                break;
            }
            case R.id.preview_button7: {
                previewBtnAction(6);
                break;
            }
            case R.id.preview_button8: {
                previewBtnAction(7);
                break;
            }
            default:
                break;
        }
    }

    private void previewBtnAction(int var){
        if ((mPlaybackState != null) && (mCamera != null)){
            if (mPlaybackState.getPlaybackMode().equals(SettingsDefinitions.PlaybackMode.MULTIPLE_FILES_EDIT)){
                mCamera.getPlaybackManager().toggleFileSelectionAtIndex(var);
            }else if(mPlaybackState.getPlaybackMode().equals(SettingsDefinitions.PlaybackMode.MULTIPLE_MEDIA_FILE_PREVIEW)){
                mCamera.getPlaybackManager().enterSinglePreviewModeWithIndex(var);
            }
        }
    }

    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode){
        if (mCamera != null) {
            mCamera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            });
        }
    }
}
