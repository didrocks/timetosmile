package fr.teamrocks.timetosmile.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.teamrocks.timetosmile.R;
import fr.teamrocks.timetosmile.entities.SmileData;

/**
 * TODO: document your custom view class.
 */
public class StatusOverlayView extends RelativeLayout {
    private static final String TAG = "StatusOverlayView";

    private String statusString;
    private String smileDurationString;
    private long smileDurationToday;

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

    public void updateSmileStatus(SMILING_STATUS smilingStatus, SmileData smileData) {
        switch (smilingStatus) {
            case NO_ONE:
                statusString = "Where are you?";
                break;
            case NONE:
                statusString = "Please smile…";
                break;
            case TIMID:
                statusString = "I see a timid smile. Almost there!";
                break;
            case SMILING:
                statusString = "What a great smile!";
                break;
        }
        smileDurationToday = smileData.getSmilingDuration();
        smileDurationString = smileData.getFormatTime();

        post(new Runnable() {
            @Override
            public void run() {
                mSmileDurationTodayProgress.setProgress((int) (smileDurationToday * 100 / SmileData.TARGETSMILINGTIME));
                mSmileDurationText.setText(smileDurationString);
                mStatusText.setText(statusString);
            }
        });
    }
}
