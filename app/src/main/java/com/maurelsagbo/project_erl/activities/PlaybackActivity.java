package com.maurelsagbo.project_erl.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.application.ERLApplication;

import java.io.File;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.camera.DJICamera;
import dji.sdk.camera.DJIPlaybackManager;

public class PlaybackActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PlaybackActivity.class.getName();

    private Button mPreviousBtn, mNextBtn, mSelectBtn, mSelectAllBtn;
    private Button mSingleBtn, mMultipleBtn, mDownloadBtn, mDeleteBtn;
    private Button mPreviewBtn1, mPreviewBtn2, mPreviewBtn3, mPreviewBtn4, mPreviewBtn5, mPreviewBtn6, mPreviewBtn7, mPreviewBtn8;

    private final int SHOWTOAST = 1;
    private final int SHOW_DOWNLOAD_PROGRESS_DIALOG = 2;
    private final int HIDE_DOWNLOAD_PROGRESS_DIALOG = 3;

    private boolean isSinglePreview = true;
    private DJIPlaybackManager.DJICameraPlaybackState mPlaybackState;
    private DJICamera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        initUI();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initCameraCallBacks();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
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

    private void initUI() {
        mSingleBtn = (Button) findViewById(R.id.btn_single_btn);
        mMultipleBtn = (Button) findViewById(R.id.btn_multi_pre_btn);
        mSelectBtn = (Button) findViewById(R.id.btn_select_btn);
        mSelectAllBtn = (Button) findViewById(R.id.btn_select_all_btn);
        mDeleteBtn = (Button) findViewById(R.id.btn_delete_btn);
        mDownloadBtn = (Button) findViewById(R.id.btn_download_btn);
        mPreviousBtn = (Button) findViewById(R.id.btn_previous_btn);
        mNextBtn = (Button) findViewById(R.id.btn_next_btn);

        mPreviewBtn1 = (Button) findViewById(R.id.preview_button1);
        mPreviewBtn2 = (Button) findViewById(R.id.preview_button2);
        mPreviewBtn3 = (Button) findViewById(R.id.preview_button3);
        mPreviewBtn4 = (Button) findViewById(R.id.preview_button4);
        mPreviewBtn5 = (Button) findViewById(R.id.preview_button5);
        mPreviewBtn6 = (Button) findViewById(R.id.preview_button6);
        mPreviewBtn7 = (Button) findViewById(R.id.preview_button7);
        mPreviewBtn8 = (Button) findViewById(R.id.preview_button8);

        mSingleBtn.setOnClickListener(this);
        mMultipleBtn.setOnClickListener(this);
        mSelectBtn.setOnClickListener(this);
        mSelectAllBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mDownloadBtn.setOnClickListener(this);
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

        createProgressDialog();
    }

    protected void initCameraCallBacks() {
        if (ERLApplication.isPlaybackAvailable()){

            if (mCamera != null) {

                mCamera.getPlayback().setDJICameraPlayBackStateCallBack(new DJIPlaybackManager.DJICameraPlayBackStateCallBack() {
                    @Override
                    public void onResult(DJIPlaybackManager.DJICameraPlaybackState playbackState) {
                        if (null != playbackState) {

                            if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDisplay) ||
                                    playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MediaFilesDownload) ||
                                    playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)) {
                                isSinglePreview = false;
                            } else {
                                isSinglePreview = true;
                            }
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

    private ProgressDialog mDownloadDialog;

    private void createProgressDialog() {

        mDownloadDialog = new ProgressDialog(PlaybackActivity.this);
        mDownloadDialog.setTitle("Downloading File");
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(false);
    }

    private void ShowDownloadProgressDialog() {
        if (mDownloadDialog != null) {
            mDownloadDialog.show();
        }
    }

    private void HideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    showToast((String) msg.obj);
                    break;
                case SHOW_DOWNLOAD_PROGRESS_DIALOG:
                    ShowDownloadProgressDialog();
                    break;
                case HIDE_DOWNLOAD_PROGRESS_DIALOG:
                    HideDownloadProgressDialog();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_single_btn: {
                if (!isSinglePreview)
                    mCamera.getPlayback().enterSinglePreviewModeWithIndex(0);
                break;
            }
            case R.id.btn_multi_pre_btn: {
                if (isSinglePreview)
                    mCamera.getPlayback().enterMultiplePreviewMode();
                break;
            }
//            case R.id.btn_select_btn: {
//
//                if (mPlaybackState == null) {
//                    break;
//                }
//                if (mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDisplay)) {
//                    mCamera.getPlayback().enterMultipleEditMode();
//                } else if (mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)) {
//                    mCamera.getPlayback().exitMultipleEditMode();
//                }
//                break;
//            }

            case R.id.btn_select_btn:{
                switchCameraMode(DJICameraSettingsDef.CameraMode.Playback);
            }
            case R.id.btn_select_all_btn: {
                if (mPlaybackState == null) {
                    break;
                }
                if (mPlaybackState.isAllFilesInPageSelected) {
                    mCamera.getPlayback().unselectAllFilesInPage();
                } else {
                    mCamera.getPlayback().selectAllFilesInPage();
                }
                break;
            }
            case R.id.btn_delete_btn: {

                if (mPlaybackState == null) {
                    break;
                }
                if (mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)) {
                    mCamera.getPlayback().deleteAllSelectedFiles();

                } else if (mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.SinglePhotoPlayback)) {
                    mCamera.getPlayback().deleteCurrentPreviewFile();
                }
                break;
            }
            case R.id.btn_download_btn: {

                if (mPlaybackState == null) {
                    break;
                }

                File destDir =
                        new File(Environment.getExternalStorageDirectory().getPath() + "/ERLProject/");
                if (mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)) {

                    mCamera.getPlayback().downloadSelectedFiles(destDir,
                            new DJIPlaybackManager.CameraFileDownloadCallback() {
                                @Override
                                public void onStart() {
                                    handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
                                    if (mDownloadDialog != null) {
                                        mDownloadDialog.setProgress(0);
                                    }
                                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnStart"));
                                }

                                @Override
                                public void onEnd() {
                                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnEnd"));
                                }

                                @Override
                                public void onError(Exception e) {
                                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                                    handler.sendMessage(handler.obtainMessage(SHOWTOAST,
                                            "download selected files OnError :" + e.toString()));
                                }

                                @Override
                                public void onProgressUpdate(int i) {
                                    if (mDownloadDialog != null) {
                                        mDownloadDialog.setProgress(i);
                                    }
                                }
                            });

                }

                break;
            }
            case R.id.btn_previous_btn: {
                if (isSinglePreview) {
                    mCamera.getPlayback().multiplePreviewPreviousPage();
                } else {
                    mCamera.getPlayback().singlePreviewPreviousPage();
                }
                break;
            }
            case R.id.btn_next_btn: {
                if (isSinglePreview) {
                    mCamera.getPlayback().multiplePreviewNextPage();
                } else {
                    mCamera.getPlayback().singlePreviewNextPage();
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
            if (mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)){
                    mCamera.getPlayback().toggleFileSelectionAtIndex(var);
            }else if(mPlaybackState.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDisplay)){
                    mCamera.getPlayback().enterSinglePreviewModeWithIndex(var);
            }
        }
    }

    private void switchCameraMode(DJICameraSettingsDef.CameraMode cameraMode){

        if (mCamera != null) {
            mCamera.setCameraMode(cameraMode, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                    if (djiError == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            });
        }
    }
}
