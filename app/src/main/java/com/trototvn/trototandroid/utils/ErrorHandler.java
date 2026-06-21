package com.trototvn.trototandroid.utils;

import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;
import timber.log.Timber;

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
            return "Kết nối quá hạn. Vui lòng thử lại.";
        } else if (throwable instanceof UnknownHostException) {
            return "Không thể kết nối máy chủ. Vui lòng kiểm tra mạng.";
        } else if (throwable instanceof JsonParseException) {
            return "Lỗi xử lý dữ liệu từ máy chủ.";
        } else if (throwable instanceof IOException) {
            return "Lỗi mạng. Vui lòng thử lại.";
        } else {
            return throwable.getMessage() != null ? throwable.getMessage() : "Đã xảy ra lỗi không xác định";
        }
    }

    private static String handleHttpException(HttpException exception) {
        try {
            if (exception.response() != null && exception.response().errorBody() != null) {
                String errorBodyStr = exception.response().errorBody().string();
                JSONObject jsonObject = new JSONObject(errorBodyStr);
                
                String backendMessage = jsonObject.optString("message", "");
                
                if (!backendMessage.isEmpty()) {
                    if ("VALIDATION_ERROR".equals(backendMessage) && jsonObject.has("error")) {
                        org.json.JSONArray errorArray = jsonObject.optJSONArray("error");
                        if (errorArray != null && errorArray.length() > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < errorArray.length(); i++) {
                                JSONObject errObj = errorArray.optJSONObject(i);
                                if (errObj != null) {
                                    String param = errObj.optString("param", "");
                                    String msg = errObj.optString("msg", "");
                                    
                                    // Translate some common validation messages
                                    if (param.equals("password") && msg.contains("Not match")) {
                                        msg = "Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường và 1 số";
                                    }
                                    
                                    sb.append(param.isEmpty() ? "" : param + ": ").append(msg).append("\n");
                                }
                            }
                            return sb.toString().trim();
                        }
                    }

                    switch (backendMessage) {
                        case "ACCOUNT_NOT_FOUND":
                            return "Tài khoản không tồn tại.";
                        case "PASSWORD_NOT_MATCH":
                            return "Số điện thoại/Email hoặc mật khẩu không đúng.";
                        case "ACCOUNT_INACTIVE":
                            return "Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt.";
                        case "PHONE_ALREADY_EXISTS":
                            return "Số điện thoại này đã được sử dụng.";
                        case "EMAIL_ALREADY_EXISTS":
                        case "USER_ALREADY_EXISTS":
                        case "EMAIL_EXIST":
                            if (jsonObject.has("error")) {
                                org.json.JSONArray errArr = jsonObject.optJSONArray("error");
                                if (errArr != null && errArr.length() > 0 && "OTP_SENT_RECENTLY".equals(errArr.optString(0))) {
                                    return "Mã OTP đã được gửi gần đây. Vui lòng kiểm tra email hoặc thử lại sau ít phút.";
                                }
                            }
                            return "Tài khoản với Email hoặc Số điện thoại này đã tồn tại. Vui lòng đăng nhập hoặc sử dụng thông tin khác.";
                        case "INVALID_OTP":
                            return "Mã xác thực OTP không chính xác hoặc đã hết hạn.";
                        case "USER_NOT_FOUND":
                            return "Không tìm thấy người dùng với thông tin này.";
                        case "INVALID_ACCESS_TOKEN":
                        case "INVALID_TOKEN":
                            return "Phiên làm việc không hợp lệ, vui lòng đăng nhập lại.";
                        default:
                            if (!backendMessage.contains("_")) {
                                return backendMessage;
                            }
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi khi parse errorBody");
        }

        switch (exception.code()) {
            case 400:
                return "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.";
            case 401:
                return "Phiên đăng nhập hết hạn hoặc không có quyền truy cập.";
            case 403:
                return "Bạn không có quyền thực hiện thao tác này.";
            case 404:
                return "Không tìm thấy dữ liệu.";
            case 500:
                return "Lỗi máy chủ. Vui lòng thử lại sau.";
            case 503:
                return "Dịch vụ đang gián đoạn. Vui lòng thử lại sau.";
            default:
                return "Lỗi " + exception.code() + ": " + exception.message();
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
