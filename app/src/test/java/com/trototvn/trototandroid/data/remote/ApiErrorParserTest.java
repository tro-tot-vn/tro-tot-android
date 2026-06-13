package com.trototvn.trototandroid.data.remote;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Verifies that ApiErrorParser turns every backend error shape + HTTP status into a clear
 * Vietnamese message. Bodies mirror tro-tot-vn-be error.middleware.ts / controller outputs.
 */
public class ApiErrorParserTest {

    private static HttpException http(int code, String json) {
        ResponseBody body = ResponseBody.create(MediaType.parse("application/json"), json);
        return new HttpException(Response.error(code, body));
    }

    @Test
    public void validationError_surfacesOffendingField() {
        String json = "{\"status\":400,\"message\":\"VALIDATION_ERROR\","
                + "\"error\":[{\"msg\":\"minLength 1\",\"param\":\"phone\",\"location\":\"body\"}],\"data\":null}";
        assertEquals("Số điện thoại không hợp lệ", ApiErrorParser.message(http(400, json)));
    }

    @Test
    public void validationError_unknownParam_fallsBackToGeneric() {
        String json = "{\"status\":400,\"message\":\"VALIDATION_ERROR\","
                + "\"error\":[{\"msg\":\"x\",\"param\":\"weird\"}],\"data\":null}";
        assertEquals("Dữ liệu nhập không hợp lệ", ApiErrorParser.message(http(400, json)));
    }

    @Test
    public void businessConflict_emailExists() {
        String json = "{\"status\":400,\"message\":\"EMAIL_ALREADY_EXISTS\",\"error\":[\"\"],\"data\":null}";
        assertEquals("Email đã được sử dụng", ApiErrorParser.message(http(400, json)));
    }

    @Test
    public void businessConflict_phoneExists() {
        String json = "{\"status\":400,\"message\":\"PHONE_ALREADY_EXISTS\",\"error\":[\"\"],\"data\":null}";
        assertEquals("Số điện thoại đã được sử dụng", ApiErrorParser.message(http(400, json)));
    }

    @Test
    public void notFound_post() {
        String json = "{\"status\":404,\"message\":\"POST_NOT_FOUND\",\"error\":[\"\"],\"data\":null}";
        assertEquals("Không tìm thấy bài đăng", ApiErrorParser.message(http(404, json)));
    }

    @Test
    public void rejectionReasonRequired() {
        String json = "{\"status\":400,\"message\":\"REJECTION_REASON_REQUIRED\",\"error\":[\"\"],\"data\":null}";
        assertEquals("Vui lòng nhập lý do từ chối", ApiErrorParser.message(http(400, json)));
    }

    @Test
    public void unauthorized_invalidToken() {
        String json = "{\"status\":401,\"message\":\"INVALID_ACCESS_TOKEN\","
                + "\"error\":[{\"msg\":\"INVALID_ACCESS_TOKEN\"}],\"data\":null}";
        assertEquals("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại",
                ApiErrorParser.message(http(401, json)));
    }

    @Test
    public void forbidden_resolvedByStatus_notMisleadingInternalError() {
        // 403 carries message "INTERNAL_ERROR" from the middleware default branch.
        String json = "{\"status\":403,\"message\":\"INTERNAL_ERROR\","
                + "\"error\":[{\"msg\":\"Insufficient permissions\"}],\"data\":null}";
        assertEquals("Bạn không có quyền thực hiện thao tác này",
                ApiErrorParser.message(http(403, json)));
    }

    @Test
    public void locked_account_423() {
        String json = "{\"status\":423,\"message\":\"INTERNAL_ERROR\","
                + "\"error\":[{\"msg\":\"Account is not active\"}],\"data\":null}";
        assertEquals("Tài khoản đang bị khóa", ApiErrorParser.message(http(423, json)));
    }

    @Test
    public void serverError_500() {
        String json = "{\"status\":500,\"message\":\"INTERNAL_ERROR\","
                + "\"error\":[{\"msg\":\"Server error\"}],\"data\":null}";
        assertEquals("Lỗi máy chủ, vui lòng thử lại sau", ApiErrorParser.message(http(500, json)));
    }

    @Test
    public void unknownStatus_fallsBackWithCode() {
        String json = "{\"status\":418,\"message\":\"WHATEVER\",\"error\":[],\"data\":null}";
        assertEquals("Đã có lỗi xảy ra (mã 418)", ApiErrorParser.message(http(418, json)));
    }

    @Test
    public void emptyOrNonJsonBody_fallsBackToHttpStatus() {
        assertEquals("Không tìm thấy dữ liệu", ApiErrorParser.message(http(404, "")));
        assertEquals("Yêu cầu không hợp lệ", ApiErrorParser.message(http(400, "<html>oops</html>")));
    }

    @Test
    public void networkErrors() {
        assertEquals("Hết thời gian kết nối, vui lòng thử lại",
                ApiErrorParser.message(new SocketTimeoutException()));
        assertEquals("Không thể kết nối máy chủ, vui lòng kiểm tra kết nối mạng",
                ApiErrorParser.message(new UnknownHostException()));
    }

    @Test
    public void translateCode_mapsKnown_elseFallback() {
        assertEquals("Email đã được sử dụng",
                ApiErrorParser.translateCode("EMAIL_ALREADY_EXISTS", "fallback"));
        assertEquals("fallback", ApiErrorParser.translateCode("UNKNOWN_CODE", "fallback"));
        assertEquals("fallback", ApiErrorParser.translateCode(null, "fallback"));
    }
}
