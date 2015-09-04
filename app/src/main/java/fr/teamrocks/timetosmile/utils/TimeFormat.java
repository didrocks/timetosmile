package fr.teamrocks.timetosmile.utils;

/**
 * would be better as a mixin once available in java 8
 * some parts are from com.example.android.wearable.timer.util
 */

public abstract class TimeFormat {

    private static final String TWO_DIGITS = "%02d";
    private static final String ONE_DIGIT = "%01d";


    /*
     * Override this one for the unit we want to work on
     */
    protected abstract long getTime();

    public String getFormatTime() {
        long time = getTime();

        long seconds = time / 1000;
        long minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        long hours = minutes / 60;
        minutes = minutes - hours * 60;

        String hoursString = formatNumber(hours);
        String minutesString = formatNumber(minutes);
        // Seconds are always two digits
        String secondsString = String.format(TWO_DIGITS, seconds);

        // Most likely
        if (hoursString == null && minutesString == null) {
            return secondsString;
        }
        else if (hoursString == null) {
            return String.format("%s:%s", minutesString, secondsString);
        }
        return String.format("%s:%s:%s", hoursString, minutesString, secondsString);
    }

    /**
     * Return one or two string digits depending on the time
     *
     * @param number the number to format
     * @return a string formatted version of number. Null if <= 0
     */
    private String formatNumber(double number) {
        String result = null;
        if (number >= 10) {
            result = String.format(TWO_DIGITS, number);
        }
        else if (number > 0) {
            result = String.format(ONE_DIGIT, number);
        }
        return result;
    }
}
