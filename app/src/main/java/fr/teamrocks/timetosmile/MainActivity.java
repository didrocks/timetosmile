package fr.teamrocks.timetosmile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERM = 1;

    private CameraSource mCameraSource = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.message_need_camera_permission_title)
                            .setMessage(R.string.message_need_camera_permission)
                            .setPositiveButton(R.string.dialog_OK, listener)
                            .show();
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
        mCameraSource.stop();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null)
            mCameraSource.release();
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
            // TODO: Add dialog here
            Log.w(TAG, "Detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(320, 240)
                .build();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                mCameraSource.start();
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                // TODO: Add dialog here
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    class FaceTracker extends Tracker<Face> {
        public void onNewItem(int id, Face face) {
            Log.i(TAG, "Awesome person detected.  Hello!");
        }

        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            if (face.getIsSmilingProbability() > 0.75) {
gi            }
            else
                Log.i(TAG, "DUDEEEEE, SMILE!");
        }

        public void onDone() {
            Log.i(TAG, "Elvis has left the building.");
        }
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
}
