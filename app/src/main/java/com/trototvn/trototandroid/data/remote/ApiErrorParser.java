package com.trototvn.trototandroid.data.remote;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.HttpException;

/**
 * Parses backend error responses into clear, user-facing Vietnamese messages.
 *
 * <p>The backend normalizes EVERY error (tsoa validation, auth, not-found, business, 500) into the
 * {@code ResponseData} envelope: {@code {status, message, error:[...], data:null}} with the HTTP
 * status matching {@code status} (see tro-tot-vn-be error.middleware.ts). For non-2xx responses
 * Retrofit throws an {@link HttpException} whose error body holds that envelope, so we parse it here
 * rather than showing Retrofit's generic "HTTP 400" text.</p>
 *
 * <p>Resolution order: tsoa validation field → 403/423 by HTTP status → known error code →
 * HTTP-status fallback. Network failures get their own messages.</p>
 */
public final class ApiErrorParser {

    /** Backend error code (carried in ResponseData.message) -> Vietnamese message. */
    private static final Map<String, String> CODE_MAP = new HashMap<>();
    /** tsoa validation field name (error[].param) -> Vietnamese field label. */
    private static final Map<String, String> FIELD_VN = new HashMap<>();

    static {
        // Moderation
        CODE_MAP.put("REJECTION_REASON_REQUIRED", "Vui lòng nhập lý do từ chối");
        CODE_MAP.put("REQUIRED_REASON", "Vui lòng nhập lý do");
        CODE_MAP.put("ACTION_TYPE_REQUIRED", "Vui lòng chọn hành động duyệt");
        CODE_MAP.put("MODERATION_FAILED", "Kiểm duyệt thất bại, vui lòng thử lại");
        // Moderator management
        CODE_MAP.put("PHONE_ALREADY_EXISTS", "Số điện thoại đã được sử dụng");
        CODE_MAP.put("EMAIL_ALREADY_EXISTS", "Email đã được sử dụng");
        CODE_MAP.put("MODERATOR_NOT_FOUND", "Không tìm thấy kiểm duyệt viên");
        CODE_MAP.put("USER_NOT_FOUND", "Không tìm thấy người dùng");
        CODE_MAP.put("ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản");
        CODE_MAP.put("POST_NOT_FOUND", "Không tìm thấy bài đăng");
        CODE_MAP.put("UPDATE_PROFILE_FAILED", "Cập nhật hồ sơ thất bại");
        // Auth / session
        CODE_MAP.put("ACCESS_TOKEN_EXPIRED", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
        CODE_MAP.put("INVALID_ACCESS_TOKEN", "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại");
        CODE_MAP.put("UNAUTHORIZED", "Bạn cần đăng nhập để tiếp tục");
        // Generic
        CODE_MAP.put("NOT_FOUND", "Không tìm thấy dữ liệu");
        CODE_MAP.put("INTERNAL_ERROR", "Lỗi máy chủ, vui lòng thử lại sau");
        CODE_MAP.put("INTERNAL_SERVER_ERROR", "Lỗi máy chủ, vui lòng thử lại sau");

        FIELD_VN.put("firstName", "Họ");
        FIELD_VN.put("lastName", "Tên");
        FIELD_VN.put("email", "Email");
        FIELD_VN.put("phone", "Số điện thoại");
        FIELD_VN.put("gender", "Giới tính");
        FIELD_VN.put("birthday", "Ngày sinh");
        FIELD_VN.put("reason", "Lý do");
        FIELD_VN.put("actionType", "Hành động duyệt");
        FIELD_VN.put("status", "Trạng thái");
        FIELD_VN.put("oldPassword", "Mật khẩu hiện tại");
        FIELD_VN.put("newPassword", "Mật khẩu mới");
    }

    private ApiErrorParser() {
    }

    /**
     * @return a clear Vietnamese message for any throwable raised by an admin API call.
     */
    public static String message(Throwable t) {
        if (t instanceof HttpException) {
            return fromHttp((HttpException) t);
        }
        if (t instanceof SocketTimeoutException) {
            return "Hết thời gian kết nối, vui lòng thử lại";
        }
        if (t instanceof UnknownHostException) {
            return "Không thể kết nối máy chủ, vui lòng kiểm tra kết nối mạng";
        }
        if (t instanceof IOException) {
            return "Lỗi mạng, vui lòng thử lại";
        }
        return (t != null && t.getMessage() != null) ? t.getMessage() : "Đã có lỗi xảy ra";
    }

    /**
     * Translate a backend error code carried in a (rare) 2xx body whose {@code status != 200}.
     */
    public static String translateCode(String code, String fallback) {
        if (code == null || code.isEmpty()) {
            return fallback;
        }
        String mapped = CODE_MAP.get(code);
        if (mapped != null) {
            return mapped;
        }
        return fallback != null ? fallback : code;
    }

    private static String fromHttp(HttpException e) {
        int code = e.code();
        String messageCode = null;
        String param = null;

        String body = readErrorBody(e);
        if (body != null && !body.isEmpty()) {
            try {
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                if (obj.has("message") && !obj.get("message").isJsonNull()) {
                    messageCode = obj.get("message").getAsString();
                }
                if (obj.has("error") && obj.get("error").isJsonArray()) {
                    JsonArray arr = obj.getAsJsonArray("error");
                    if (arr.size() > 0 && arr.get(0).isJsonObject()) {
                        JsonObject detail = arr.get(0).getAsJsonObject();
                        if (detail.has("param") && !detail.get("param").isJsonNull()) {
                            param = detail.get("param").getAsString();
                        }
                    }
                }
            } catch (Exception ignored) {
                // Non-JSON / unexpected body -> fall back to HTTP status below
            }
        }

        // 1) tsoa validation: surface the offending field clearly
        if ("VALIDATION_ERROR".equals(messageCode)) {
            return validationMessage(param);
        }
        // 2) 403/423 carry a misleading message code ("INTERNAL_ERROR") -> resolve by status
        if (code == 403) {
            return "Bạn không có quyền thực hiện thao tác này";
        }
        if (code == 423) {
            return "Tài khoản đang bị khóa";
        }
        // 3) known business / auth code
        if (messageCode != null) {
            String mapped = CODE_MAP.get(messageCode);
            if (mapped != null) {
                return mapped;
            }
        }
        // 4) HTTP-status fallback
        return httpMessage(code);
    }

    private static String validationMessage(String param) {
        if (param != null) {
            String field = FIELD_VN.get(param);
            if (field != null) {
                return field + " không hợp lệ";
            }
        }
        return "Dữ liệu nhập không hợp lệ";
    }

    private static String httpMessage(int code) {
        switch (code) {
            case 400:
                return "Yêu cầu không hợp lệ";
            case 401:
                return "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại";
            case 403:
                return "Bạn không có quyền thực hiện thao tác này";
            case 404:
                return "Không tìm thấy dữ liệu";
            case 408:
                return "Hết thời gian kết nối, vui lòng thử lại";
            case 423:
                return "Tài khoản đang bị khóa";
            case 500:
            case 502:
            case 503:
                return "Lỗi máy chủ, vui lòng thử lại sau";
            default:
                return "Đã có lỗi xảy ra (mã " + code + ")";
        }
    }

    private static String readErrorBody(HttpException e) {
        try {
            if (e.response() != null && e.response().errorBody() != null) {
                return e.response().errorBody().string();
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }
}
