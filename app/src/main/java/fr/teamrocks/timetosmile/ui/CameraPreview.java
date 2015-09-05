package fr.teamrocks.timetosmile.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

/**
 * Display the camera preview to different sizes and enable overlay elements on image to be drawn
 */
public class CameraPreview extends RelativeLayout {
    private static final String TAG = "CameraPreview";

    private Context mContext;

    private boolean mStartRequested;
    private boolean mSurfaceAvailable;

    private SurfaceView mSurfaceView;
    private CameraSource mCameraSource;

    private Point mDefaultCameraPreviewSize;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "New camera preview");
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    public void start(CameraSource cameraSource) throws IOException {
        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void stop() {
        if (mCameraSource != null)
            mCameraSource.stop();
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() throws IOException {
        Log.d(TAG, "Start if ready: " + mStartRequested + " " + mSurfaceAvailable);
        if (mStartRequested && mSurfaceAvailable) {
            Log.d(TAG, "Really start camera");
            mCameraSource.start(mSurfaceView.getHolder());
            /*if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }*/
            mStartRequested = false;
        }
    }

    public void setDisplaySize(Point displaySize) {
        mDefaultCameraPreviewSize = displaySize;
    }

    /*
     * Camera surface itself
     */
    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            Log.d(TAG, "Surface created");
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "New surface: " + width + ":" + height);
            // This is the width and height under the activity menubar: 1080:1536
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // this is for the design view
        if (mDefaultCameraPreviewSize == null) {
            mDefaultCameraPreviewSize = new Point(bottom-top, right-left);
        }
        int width = mDefaultCameraPreviewSize.x;
        int height = mDefaultCameraPreviewSize.y;

        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
                Log.d(TAG, "Camera preview real size:" + width + ":" + height);
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;
        Log.d(TAG, "Surface size:" + layoutWidth + ":" + layoutHeight);

        // Computes height and width for potentially doing fit width.
        // We always match the width, even if that mean cutting some part of the height
        int childWidth = layoutWidth;
        int childHeight = (int)(((float) layoutWidth / (float) width) * height);

        for (int i = 0; i < getChildCount(); ++i) {
            Log.d(TAG, "New children: " + i);
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
