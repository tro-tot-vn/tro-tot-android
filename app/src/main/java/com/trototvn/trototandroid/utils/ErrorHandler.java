package com.trototvn.trototandroid.utils;

import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;

/**
 * Centralized error handler for API errors
 */
public class ErrorHandler {

    /**
     * Convert exception to user-friendly error message
     */
    public static String getErrorMessage(Throwable throwable) {
        if (throwable instanceof HttpException) {
            return handleHttpException((HttpException) throwable);
        } else if (throwable instanceof SocketTimeoutException) {
            return "Connection timeout. Please try again.";
        } else if (throwable instanceof UnknownHostException) {
            return "Unable to reach server. Please check your connection.";
        } else if (throwable instanceof JsonParseException) {
            return "Error parsing server response.";
        } else if (throwable instanceof IOException) {
            return "Network error. Please try again.";
        } else {
            return throwable.getMessage() != null ? throwable.getMessage() : "An unknown error occurred";
        }
    }

    private static String handleHttpException(HttpException exception) {
        switch (exception.code()) {
            case 400:
                return "Bad request. Please check your input.";
            case 401:
                return "Unauthorized. Please login again.";
            case 403:
                return "Access forbidden.";
            case 404:
                return "Resource not found.";
            case 500:
                return "Server error. Please try again later.";
            case 503:
                return "Service unavailable. Please try again later.";
            default:
                return "Error " + exception.code() + ": " + exception.message();
        }
    }

    /**
     * Check if error is authentication related (401, 403)
     */
    public static boolean isAuthError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            int code = ((HttpException) throwable).code();
            return code == 401 || code == 403;
        }
        return false;
    }

    /**
     * Check if error is network related
     */
    public static boolean isNetworkError(Throwable throwable) {
        return throwable instanceof IOException;
    }
}
