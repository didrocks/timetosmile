package fr.teamrocks.timetosmile.entities;

import android.graphics.Point;
import android.util.Log;
import android.util.Size;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import fr.teamrocks.timetosmile.ui.StatusOverlayView;

public class FaceTracker extends Tracker<Face> {
    private static final String TAG = "FaceTracker";

    private SmileData smileData;
    private StatusOverlayView mStatusOverlay;

    /**
     * New FaceTracker constructor
     * @param smileData: the current new smile data to associate
     */
    public FaceTracker(SmileData smileData, StatusOverlayView statusOverlay) {
        super();
        this.smileData = smileData;
        mStatusOverlay = statusOverlay;
    }

    /**
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
        if (smileData == null) {
            Log.e(TAG, "No smile data associated");
            return;
        }
        if (face.getIsSmilingProbability() > 0.7) {
            smileData.Smile();
            mStatusOverlay.updateSmileStatus(StatusOverlayView.SMILING_STATUS.SMILING, smileData);
        } else if (face.getIsSmilingProbability() > 0.3) {
            smileData.NoSmile();
            mStatusOverlay.updateSmileStatus(StatusOverlayView.SMILING_STATUS.TIMID, smileData);
        } else {
            smileData.NoSmile();
            mStatusOverlay.updateSmileStatus(StatusOverlayView.SMILING_STATUS.NONE, smileData);
        }
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mStatusOverlay.updateSmileStatus(StatusOverlayView.SMILING_STATUS.NO_ONE, smileData);
    }

    @Override
    public void onDone() {
        smileData.NoSmile();
        mStatusOverlay.updateSmileStatus(StatusOverlayView.SMILING_STATUS.NO_ONE, smileData);
    }
}