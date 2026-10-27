package com.smartpantry.app.logic;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helpers for the optional expiry date (stored as text "yyyy-MM-dd").
 */
public final class ExpiryUtils {

    private ExpiryUtils() {
    }

    /** Items expiring within this many days (or already expired) are flagged when alerts are on. */
    public static final int EXPIRING_SOON_DAYS = 3;

    private static final String PATTERN = "yyyy-MM-dd";

    /** True if the text is a real calendar date in yyyy-MM-dd format. */
    public static boolean isValidDate(String iso) {
        return parse(iso) != null;
    }

    /** Formats a date for storage / display as yyyy-MM-dd. */
    public static String format(int year, int monthZeroBased, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, day);
    }

    /**
     * Whole days from {@code today} until the expiry date (negative if already expired).
     *
     * @return null if there is no (valid) expiry date
     */
    public static Integer daysUntil(String iso, Calendar today) {
        Calendar target = parse(iso);
        if (target == null) {
            return null;
        }
        Calendar start = (Calendar) today.clone();
        clearTime(start);
        long diff = target.getTimeInMillis() - start.getTimeInMillis();
        return (int) Math.round(diff / 86400000.0);
    }

    private static Calendar parse(String iso) {
        if (iso == null) {
            return null;
        }
        String text = iso.trim();
        if (text.length() != PATTERN.length()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(PATTERN, Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date date = format.parse(text, position);
        if (date == null || position.getIndex() != text.length()) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        clearTime(calendar);
        return calendar;
    }

    private static void clearTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
