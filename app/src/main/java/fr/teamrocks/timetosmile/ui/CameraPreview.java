package fr.teamrocks.timetosmile.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

import fr.teamrocks.timetosmile.R;


public class CameraPreview extends ViewGroup {
    private static final String TAG = "CameraPreview";

    private Context mContext;

    private boolean mStartRequested;
    private boolean mSurfaceAvailable;

    private SurfaceView mSurfaceView;
    private CameraSource mCameraSource;

    private Point defaultCameraPreviewSize;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG, "NEW CAMERA PREVIEW");
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
        //setWillNotDraw(false);
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
            Log.d(TAG, "REALLY START");
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
        defaultCameraPreviewSize = displaySize;
    }

    /*
     * Camera surface itself
     */
    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            Log.e(TAG, "Surface created");
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
            Log.w(TAG, "New surface: " + width + ":" + height);
            // This is the width and height under the activity menubar: 1080:1536
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // this is for the design view
        if (defaultCameraPreviewSize == null) {
            defaultCameraPreviewSize = new Point(2560, 1340);
        }
        int width = defaultCameraPreviewSize.x;
        int height = defaultCameraPreviewSize.y;
        int offset_x = 0;
        Log.w(TAG, "screen default width requested for camera preview:" + width + ":" + height);
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
                Log.w(TAG, "camera preview real size:" + width + ":" + height);
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
        Log.w(TAG, "Surface size:" + layoutWidth + ":" + layoutHeight);

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int)(((float) layoutWidth / (float) width) * height);

        // We always match the width, even if that mean cutting some part of the height
        // If height is too tall using fit width, does fit height instead.
        /*if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int)(((float) layoutHeight / (float) height) * width);
            // we center it as well
            offset_x = (layoutWidth - childWidth) / 2;
        }*/

        Log.w(TAG, "Parent size " + layoutWidth + ":" + layoutHeight);
        for (int i = 0; i < getChildCount(); ++i) {
            Log.w(TAG, "Child number " + i + "    size:" + childWidth + ":" + childHeight);
            getChildAt(i).layout(offset_x, 0, offset_x + childWidth, childHeight);
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
