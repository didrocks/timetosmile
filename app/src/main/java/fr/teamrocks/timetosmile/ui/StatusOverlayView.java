package fr.teamrocks.timetosmile.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.teamrocks.timetosmile.R;
import fr.teamrocks.timetosmile.entities.SmileData;

/**
 * Custom overlay status with text and scrollbar
 */
public class StatusOverlayView extends RelativeLayout {
    private static final String TAG = "StatusOverlayView";

    private String mStatusString;
    private String mSmileDurationString;
    private long mSmileDurationToday;

    private TextView mStatusText;
    private TextView mSmileDurationText;
    private ProgressBar mSmileDurationTodayProgress;

    public enum SMILING_STATUS {
        NO_ONE,
        NONE,
        TIMID,
        SMILING
    };

    public StatusOverlayView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSmileDurationTodayProgress = (ProgressBar) findViewById(R.id.smileProgressBar);
        mSmileDurationText = (TextView) findViewById(R.id.smileProgressText);
        mStatusText = (TextView) findViewById(R.id.smileStatusText);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * Enable update element status
     * @param smilingStatus: Is the user smiling, present, timid?
     * @param smileData: the current smileData where we'll extract the values
     */
    public void updateSmileStatus(SMILING_STATUS smilingStatus, SmileData smileData) {
        switch (smilingStatus) {
            case NO_ONE:
                mStatusString = "Where are you?";
                break;
            case NONE:
                mStatusString = "Please smile…";
                break;
            case TIMID:
                mStatusString = "I see a timid smile. Almost there!";
                break;
            case SMILING:
                mStatusString = "What a great smile!";
                break;
        }

        long previousSmileDuration = mSmileDurationToday;
        mSmileDurationToday = smileData.getmSmilingDuration();
        final boolean durationChanged = (previousSmileDuration / 1000) != (mSmileDurationToday  / 1000);
        mSmileDurationString = smileData.getFormatTime();

        // update the elements in the UI thread
        post(new Runnable() {
            @Override
            public void run() {
                // as we use an interpolator, only change when the duration changed to set a new objective
                if (durationChanged) {
                    ObjectAnimator animation = ObjectAnimator.ofInt(mSmileDurationTodayProgress, "progress",
                            mSmileDurationTodayProgress.getProgress(), (int) Math.min(mSmileDurationToday,
                                    SmileData.TARGETSMILINGTIME));
                    animation.setDuration(1000);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();
                }
                mSmileDurationText.setText(mSmileDurationString);
                mStatusText.setText(mStatusString);
            }
        });
    }
}
