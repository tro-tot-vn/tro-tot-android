package com.trototvn.trototandroid.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date utility class for formatting and parsing
 */
public class DateUtils {

    // Common date formats
    public static final String FORMAT_DATE = "dd/MM/yyyy";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_DATETIME = "dd/MM/yyyy HH:mm";
    public static final String FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_FULL = "dd MMMM yyyy, HH:mm";

    /**
     * Format date to string
     */
    public static String formatDate(Date date, String format) {
        if (date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Parse string to date
     */
    public static Date parseDate(String dateStr, String format) {
        if (dateStr == null || dateStr.isEmpty())
            return null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get current date as string
     */
    public static String getCurrentDate(String format) {
        return formatDate(new Date(), format);
    }

    /**
     * Format to relative time (e.g., "2 hours ago", "Yesterday")
     */
    public static String getRelativeTime(Date date) {
        if (date == null)
            return "";

        long diffMillis = System.currentTimeMillis() - date.getTime();
        long diffSeconds = diffMillis / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;

        if (diffSeconds < 60) {
            return "Just now";
        } else if (diffMinutes < 60) {
            return diffMinutes + " minute" + (diffMinutes > 1 ? "s" : "") + " ago";
        } else if (diffHours < 24) {
            return diffHours + " hour" + (diffHours > 1 ? "s" : "") + " ago";
        } else if (diffDays == 1) {
            return "Yesterday";
        } else if (diffDays < 7) {
            return diffDays + " days ago";
        } else {
            return formatDate(date, FORMAT_DATE);
        }
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        if (date == null)
            return false;

        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
        String today = sdf.format(new Date());
        String dateStr = sdf.format(date);

        return today.equals(dateStr);
    }

    /**
     * Check if date is in past
     */
    public static boolean isPast(Date date) {
        return date != null && date.before(new Date());
    }

    /**
     * Check if date is in future
     */
    public static boolean isFuture(Date date) {
        return date != null && date.after(new Date());
    }

    /**
     * Get difference in days
     */
    public static long getDaysDifference(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return 0;

        long diffMillis = Math.abs(date1.getTime() - date2.getTime());
        return diffMillis / (24 * 60 * 60 * 1000);
    }
}
