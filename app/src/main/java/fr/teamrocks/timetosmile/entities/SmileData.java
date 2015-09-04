package fr.teamrocks.timetosmile.entities;

import android.os.SystemClock;

import java.util.Date;

import fr.teamrocks.timetosmile.utils.TimeFormat;

public class SmileData extends TimeFormat {
    private static final String TAG = "SmileData";

    private final Date day;

    // Length of smiling time in milliseconds
    private long smilingDuration;
    private final long TARGETSMILINGTIME = 60*1000;

    // Last start smiling time in ms
    private long currentSmileStartTime;

    private boolean isSmiling = false;
    public boolean isSmiling() {
        return isSmiling;
    }

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
        this.day = day;
        this.smilingDuration = smilingDuration;
    }

    /**
     * Start the timer clock if it's a new smile
     */
    public void Smile() {
        if (isSmiling)
            return;

        isSmiling = true;
        currentSmileStartTime = SystemClock.elapsedRealtime();
    }

    /**
     * Stop the timer if the person isn't smiling anymore
     */
    public void NoSmile() {
        if (!isSmiling)
            return;

        isSmiling = false;
        smilingDuration += SystemClock.elapsedRealtime() - currentSmileStartTime;
    }

    /**
     * @return current smiling duration for that day
     */
    public long getSmilingDuration() {
        if (isSmiling)
            return smilingDuration + SystemClock.elapsedRealtime() - currentSmileStartTime;
        else
            return smilingDuration;
    }

    /**
     * @return chain to the current smiling duration
     */
    protected long getTime() {
        return getSmilingDuration();
    }
}
