package com.trototvn.trototandroid.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * String utility class for validation and formatting
 */
public class StringUtils {

    private static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    /**
     * Check if string is empty or null
     */
    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    /**
     * Check if string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password
     * At least 8 chars, 1 digit, 1 lowercase, 1 uppercase, 1 special char
     */
    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate phone number (10-11 digits)
     */
    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Capitalize first letter
     */
    public static String capitalize(String str) {
        if (isEmpty(str))
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Truncate string to max length
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Get initials from name
     */
    public static String getInitials(String name) {
        if (isEmpty(name))
            return "";

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /**
     * Format phone number (0123456789 -> 012-345-6789)
     */
    public static String formatPhoneNumber(String phone) {
        if (isEmpty(phone) || phone.length() < 10)
            return phone;

        return phone.substring(0, 3) + "-" +
                phone.substring(3, 6) + "-" +
                phone.substring(6);
    }
}
