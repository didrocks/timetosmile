package fr.teamrocks.timetosmile.entities;

import android.os.SystemClock;

import java.util.Date;

import fr.teamrocks.timetosmile.utils.TimeFormat;

/**
 * Smile Data storing current smile stats for a given day
 */
public class SmileData extends TimeFormat {
    private static final String TAG = "SmileData";

    public static final long TARGETSMILINGTIME = 60*1000;

    private final Date mDay;
    // Length of smiling time in milliseconds
    private long mSmilingDuration;

    // Last start smiling time in ms
    private long mCurrentSmileStartTime;
    private boolean isSmiling = false;

    /**
     * Create a brand new SmileData for today
     */
    public SmileData() {
        this(new Date(), 0);
    }

    /**
     * Restore a SmileData with a specific day and stored smilingTime
     *
     * @param day the day this Smile Data is attached to
     * @param smilingDuration the length of the smiling in ms for that day.
     */
    public SmileData(Date day, long smilingDuration) {
        mDay = day;
        mSmilingDuration = smilingDuration;
    }

    /**
     * Start the timer clock if it's a new smile
     */
    public void Smile() {
        if (isSmiling)
            return;

        isSmiling = true;
        mCurrentSmileStartTime = SystemClock.elapsedRealtime();
    }

    /**
     * Stop the timer if the person isn't smiling anymore
     */
    public void NoSmile() {
        if (!isSmiling)
            return;

        isSmiling = false;
        mSmilingDuration += SystemClock.elapsedRealtime() - mCurrentSmileStartTime;
    }

    /**
     * @return current smiling duration for that day
     */
    public long getmSmilingDuration() {
        if (isSmiling)
            return mSmilingDuration + SystemClock.elapsedRealtime() - mCurrentSmileStartTime;
        else
            return mSmilingDuration;
    }

    /**
     * @return chain to the current smiling duration
     */
    protected long getTime() {
        return getmSmilingDuration();
    }
}
