package fr.teamrocks.timetosmile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

import fr.teamrocks.timetosmile.ui.CameraPreview;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERM = 1;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private CameraSource mCameraSource = null;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraPreview) findViewById(R.id.cameraPreview);

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

        detector.setProcessor(new LargestFaceFocusingProcessor(detector, new FaceTracker()));

        if (!detector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            showNonRecoverableError(getString(R.string.error_missing_dependencies_title),
                    getString(R.string.error_missing_dependencies));
        }

        mCameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                // TODO: change FPS and check if changing preview size affects rendered image
                .setRequestedPreviewSize(320, 240)
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

    class FaceTracker extends Tracker<Face> {

        /*
         * New main face on the view
         */
        @Override
        public void onNewItem(int id, Face face) {
            Log.i(TAG, "Awesome person detected.  Hello!");
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            if (face.getIsSmilingProbability() > 0.75) {
                Log.i(TAG, "I see a smile.  They must really enjoy your app.");
            } else if (face.getIsSmilingProbability() > 0.5) {
                Log.i(TAG, "I see a timid smile. Almost there!");
            } else
                Log.i(TAG, "DUDEEEEE, SMILE!");
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            //Log.i(TAG, "Where are you?");
        }

        @Override
        public void onDone() {
            Log.i(TAG, "Elvis has left the building.");
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
