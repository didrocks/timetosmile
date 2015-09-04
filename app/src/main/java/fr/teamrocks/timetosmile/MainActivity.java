package fr.teamrocks.timetosmile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

import fr.teamrocks.timetosmile.entities.SmileData;
import fr.teamrocks.timetosmile.ui.CameraPreview;
import fr.teamrocks.timetosmile.entities.FaceTracker;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERM = 1;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private CameraSource mCameraSource = null;
    private CameraPreview mPreview;

    private Point displaySize = new Point();
    private SmileData todaySmileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(displaySize);
        mPreview.setDisplaySize(displaySize);

        // create today's smile data (TODO: can be retrieve later from database if same day)
        todaySmileData = new SmileData();

        // get Camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final Activity thisActivity = this;
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Log.w(TAG, "Was already denied, giving more rationale");

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(thisActivity, permissions, REQUEST_CAMERA_PERM);
                }
            };
            Snackbar.make(findViewById(R.id.rootView), getString(R.string.camera_perm_rationale),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.dialog_OK), listener)
            .show();
        } else
            ActivityCompat.requestPermissions(thisActivity, permissions, REQUEST_CAMERA_PERM);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERM: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCameraSource();
                } else {
                    Log.w(TAG, "Camera permission not granted");
                    showNonRecoverableError(getString(R.string.message_need_camera_permission_title),
                            getString(R.string.message_need_camera_permission));
                    return;
                }
            }
            default: {
                Log.e(TAG, "Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
            }
        }

    }

    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    protected void onDestroy() {
        super.onDestroy();
        mPreview.release();
    }

    /*
     * Create main camera source and face detector
     */
    private void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setProminentFaceOnly(true)
                .build();

        FaceTracker faceTracker = new FaceTracker(todaySmileData);
        detector.setProcessor(new LargestFaceFocusingProcessor(detector, faceTracker));

        if (!detector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            showNonRecoverableError(getString(R.string.error_missing_dependencies_title),
                    getString(R.string.error_missing_dependencies));
        }

        mCameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                // TO FIX: the only way (as the surface is created afterwards), we try to set the preview size to be
                // fullscreen (so with upper and bottom bar)
                // if we don't set it, the resolution may be lower
                .setRequestedPreviewSize(displaySize.x, displaySize.y)
                //.setRequestedPreviewSize(2560, 1340)
                //.setRequestedPreviewSize(320, 160)
                // TODO: change FPS and check if changing preview size affects rendered image
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        if (!checkPlayServices()) {
            showNonRecoverableError(getString(R.string.error_google_play_services_title),
                    getString(R.string.error_google_play_services));
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                // TODO: Add dialog here
                showNonRecoverableError(getString(R.string.error_missing_camera_title),
                        getString(R.string.error_missing_camera));
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /*
     * Return if Google Play Services can or is installed
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google API services not at the right version");
            if (googleAPI.isUserResolvableError(result)) {
                Log.d(TAG, "Requesting Google API services installation");
                googleAPI.getErrorDialog(this, result, REQUEST_CODE_RECOVER_PLAY_SERVICES)
                        .show();
            } else
                Log.d(TAG, "Google API services can't be installed");
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                Log.d(TAG, "Back from Google Play service (hopefully installed)");
                // onResume() is now called, doing another google play service check.
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * Show a non recoverable error message, where clicking Ok exits the activity
     */
    private void showNonRecoverableError(String title, String message) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_OK, listener)
                .show();
    }
}
