package com.trototvn.trototandroid.data.remote;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.User;
import com.trototvn.trototandroid.data.model.video.IceConfigDto;
import com.trototvn.trototandroid.data.model.auth.LoginRequest;
import com.trototvn.trototandroid.data.model.auth.LoginResponse;
import com.trototvn.trototandroid.data.model.auth.RefreshTokenRequest;
import com.trototvn.trototandroid.data.model.auth.RefreshTokenResponse;
import com.trototvn.trototandroid.data.model.auth.RegisterRequest;
import com.trototvn.trototandroid.data.model.auth.RegisterResponse;
import com.trototvn.trototandroid.data.model.chat.ConversationDto;
import com.trototvn.trototandroid.data.model.chat.MarkReadRequest;
import com.trototvn.trototandroid.data.model.chat.MessageDto;
import com.trototvn.trototandroid.data.model.chat.SendMessageRequest;
import com.trototvn.trototandroid.data.model.location.WardListResponse;
import com.trototvn.trototandroid.data.model.post.ContactLogRequest;
import com.trototvn.trototandroid.data.model.post.MyPostsResponse;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.data.model.post.RecommendationResponse;
import com.trototvn.trototandroid.data.model.post.SavePostRequest;
import com.trototvn.trototandroid.data.model.rating.AddRatingRequest;
import com.trototvn.trototandroid.data.model.rating.Rating;
import com.trototvn.trototandroid.data.model.rating.RatingListResponse;
import com.trototvn.trototandroid.data.model.rating.RatingStats;
import com.trototvn.trototandroid.data.model.search.SearchInteractionRequest;
import com.trototvn.trototandroid.data.model.search.SearchResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API Service interface
 */
public interface ApiService {

    // ========== Authentication ==========

    /**
     * POST - Login
     */
    @POST("api/auth/login")
    Single<ResponseData<LoginResponse>> login(@Body LoginRequest request);

    /**
     * POST - Register
     */
    @POST("api/auth/register")
    Single<ResponseData<String>> register(@Body RegisterRequest request);

    /**
     * POST - Send OTP for Registration
     */
    @POST("api/auth/send-otp-register")
    Single<ResponseData<com.trototvn.trototandroid.data.model.auth.OTPResponse>> sendOtpRegister(@Body com.trototvn.trototandroid.data.model.auth.OTPRequest request);

    /**
     * POST - Verify OTP for Registration
     */
    @POST("api/auth/verify-otp-register")
    Single<ResponseData<Boolean>> verifyOtpRegister(@Body com.trototvn.trototandroid.data.model.auth.VerifyOTPRequest request);

    /**
     * POST - Forgot Password (Send OTP)
     */
    @POST("api/auth/forgot-password")
    Single<ResponseData<com.trototvn.trototandroid.data.model.auth.OTPResponse>> forgotPassword(@Body com.trototvn.trototandroid.data.model.auth.OTPRequest request);

    /**
     * POST - Verify OTP (Forgot Password)
     */
    @POST("api/auth/verify-otp")
    Single<ResponseData<com.trototvn.trototandroid.data.model.auth.VerifyOTPResponse>> verifyOtp(@Body com.trototvn.trototandroid.data.model.auth.VerifyOTPRequest request);

    /**
     * POST - Reset Password
     */
    @POST("api/auth/reset-password")
    Single<ResponseData<String>> resetPassword(@Body com.trototvn.trototandroid.data.model.auth.ResetPasswordRequest request);

    /**
     * POST - Refresh Token
     * Using Call instead of RxJava/Single because Authenticator runs synchronously
     * Backend only returns { accessToken }, NOT full Token object
     */
    @POST("api/auth/refresh-token")
    retrofit2.Call<ResponseData<RefreshTokenResponse>> refreshToken(@Body RefreshTokenRequest request);

    // ========== Posts ==========

        /**
         * GET - Get Latest Posts
         * Returns 4 latest approved posts for home screen
         */
        @GET("api/posts/latest")
        Single<ResponseData<List<Post>>> getLatestPosts(@Query("page") int page, @Query("limit") int limit);

        /**
         * GET - Post Detail by ID
         */
        @GET("api/posts/{postId}")
        Single<ResponseData<PostDetail>> getPostDetail(@Path("postId") int postId);

    /**
     * GET - Get Personalized Recommendatio
     * Requires authentication
     */
    @GET("api/recommend")
    Single<RecommendationResponse> getRecommendations(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("logId") Integer logId);

    /**
     * POST - Hybrid Vector Search
     * New search endpoint with better results
     */
    @GET("api/search")
    Single<SearchResponse> search(
            @Query("query") String query,
            @Query("city") String city,
            @Query("district") String district,
            @Query("ward") String ward,
            @Query("priceMin") Integer priceMin,
            @Query("priceMax") Integer priceMax,
            @Query("acreageMin") Integer acreageMin,
            @Query("acreageMax") Integer acreageMax,
            @Query("interiorCondition") String interiorCondition,
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    /**
     * POST - Log user click on search result
     */
    @POST("api/search/click")
    Single<ResponseData<Void>> logSearchClick(@Body SearchInteractionRequest.Click body);

    /**
     * POST - Submit search quality feedback
     */
    @POST("api/search/feedback")
    Single<ResponseData<Void>> submitSearchFeedback(@Body SearchInteractionRequest.Feedback body);

    // ========== Save Post ==========

    /**
     * POST - Save post to favorites
     * Requires authentication
     */
    @POST("api/customer/saved-posts/{postId}")
    Single<ResponseData<Void>> savePost(@Path("postId") int postId);

    /**
     * DELETE - Remove post from favorites
     * Requires authentication
     */
    @DELETE("api/customer/saved-posts/{postId}")
    Single<ResponseData<Void>> deleteSavedPost(@Path("postId") int postId);

    /**
     * GET - Fetch all saved posts for current customer
     * Requires authentication
     */
    @GET("api/customer/saved-posts")
    Single<ResponseData<List<com.trototvn.trototandroid.data.model.post.Post>>> getSavedPosts();

    /**
     * GET - Check if post is saved by current customer
     * Requires authentication
     */
    @GET("api/customer/saved-posts/{postId}/check")
    Single<ResponseData<Boolean>> checkSavedPost(@Path("postId") int postId);

    // ========== Interaction Logging ==========

    /**
     * POST - Log contact interaction (view phone)
     * Requires authentication
     */
    @POST("api/interactions/contact")
    Single<ResponseData<Void>> logContact(@Body ContactLogRequest request);

    // ========== Ratings & Reviews ==========

        /**
         * POST - Add rating to post
         * Requires authentication
         */
        @POST("api/customer/posts/{postId}/rate")
        Single<ResponseData<Void>> addRating(
                        @Path("postId") int postId,
                        @Body AddRatingRequest request);

        /**
         * GET - Get ratings list with cursor pagination
         */
        @GET("api/customer/posts/{postId}/rates")
        Single<ResponseData<RatingListResponse>> getRatings(
                        @Path("postId") int postId,
                        @Query("limit") int limit,
                        @Query("cursor") String cursor // Date string
        );

        /**
         * GET - Get my rating on specific post
         * Requires authentication
         */
        @GET("api/customer/posts/{postId}/rate")
        Single<ResponseData<Rating>> getMyRating(@Path("postId") int postId);

        /**
         * DELETE - Delete my rating
         * Requires authentication
         */
        @DELETE("api/customer/posts/{postId}/rate")
        Single<ResponseData<Void>> deleteMyRating(@Path("postId") int postId);

        /**
         * GET - Get rating statistics (avg, count)
         */
        @GET("api/customer/posts/{postId}/rate-avg")
        Single<ResponseData<RatingStats>> getRatingStats(@Path("postId") int postId);

        // ========== My Posts ==========

        /**
         * GET - Get my posts with status filter and pagination
         */
        @GET("api/posts/me")
        Single<ResponseData<MyPostsResponse>> getMyPosts(
                        @Query("status") String status,
                        @Query("cursor") Integer cursor,
                        @Query("limit") int limit);

        /**
         * POST - Hide my post using path parameter
         */
        @POST("api/posts/{postId}/hide")
        Single<ResponseData<Void>> hidePost(@Path("postId") int postId);

        /**
         * POST - Unhide my post using path parameter
         */
        @POST("api/posts/{postId}/unhide")
        Single<ResponseData<Void>> unhidePost(@Path("postId") int postId);

        // ========== Create & Edit Post ==========

        /**
         * GET - Get Wards by District ID (proxy to Cho Tot API)
         */
        @GET("api/location/wards/{districtId}")
        Single<WardListResponse> getWards(@Path("districtId") String districtId);

        /**
         * POST - Create rental post (multipart/form-data)
         */
        @Multipart
        @POST("api/posts")
        Single<ResponseData<Void>> createPost(
                @Part("title") RequestBody title,
                @Part("description") RequestBody description,
                @Part("price") RequestBody price,
                @Part("acreage") RequestBody acreage,
                @Part("streetNumber") RequestBody streetNumber,
                @Part("street") RequestBody street,
                @Part("ward") RequestBody ward,
                @Part("district") RequestBody district,
                @Part("city") RequestBody city,
                @Part("interiorStatus") RequestBody interiorStatus,
                @Part List<MultipartBody.Part> images,
                @Part MultipartBody.Part video);

        /**
         * PUT - Update rental post (multipart/form-data)
         */
        @Multipart
        @PUT("api/posts/{postId}")
        Single<ResponseData<Void>> editPost(
                @Path("postId") int postId,
                @Part("title") RequestBody title,
                @Part("description") RequestBody description,
                @Part("price") RequestBody price,
                @Part("acreage") RequestBody acreage,
                @Part("streetNumber") RequestBody streetNumber,
                @Part("street") RequestBody street,
                @Part("ward") RequestBody ward,
                @Part("district") RequestBody district,
                @Part("city") RequestBody city,
                @Part("interiorStatus") RequestBody interiorStatus,
                @Part("oldFiles") RequestBody oldFiles,
                @Part List<MultipartBody.Part> images,
                @Part MultipartBody.Part video);

        /**
         * GET - Get details of my post for editing
         */
        @GET("api/posts/me/{postId}")
        Single<ResponseData<PostDetail>> getDetailMyPost(@Path("postId") int postId);

    // ========== Example CRUD operations for User ==========

    /**
     * GET - Fetch list of users
     */
    @GET("users")
    Single<List<User>> getUsers();

    /**
     * GET - Fetch user by ID
     */
    @GET("users/{id}")
    Single<User> getUserById(@Path("id") int userId);

    /**
     * GET - Search users with pagination
     */
    @GET("users/search")
    Single<List<User>> searchUsers(
            @Query("query") String query,
            @Query("page") int page,
            @Query("limit") int limit);

    /**
     * POST - Create new user
     */
    @POST("users")
    Single<User> createUser(@Body User user);

    /**
     * PUT - Update existing user
     */
    @PUT("users/{id}")
    Single<User> updateUser(@Path("id") int userId, @Body User user);

    /**
     * DELETE - Delete user
     */
    @DELETE("users/{id}")
    Completable deleteUser(@Path("id") int userId);

    // ========== Chat ==========

    @GET("api/chat/conversations/{conversationId}/messages")
    Single<ResponseData<List<MessageDto>>> fetchChatHistory(
            @Path("conversationId") long conversationId,
            @Query("limit") int limit,
            @Query("offset") int offset);

    /**
     * GET - Sync missed messages when offline.
     */
    @GET("api/conversations/sync")
    Single<ResponseData<List<MessageDto>>> syncMissedMessages(
            @Query("since") String since,
            @Query("limit") int limit);

    /**
     * GET - Lấy danh sách hội thoại.
     */
    @GET("api/conversations")
    Single<ResponseData<List<ConversationDto>>> fetchConversations();

    /**
     * POST - Gửi tin nhắn.
     */
    @POST("api/chat/conversations/{conversationId}/messages")
    Single<ResponseData<MessageDto>> sendMessage(
            @Path("conversationId") long conversationId,
            @Body SendMessageRequest request);

    /**
     * POST - Đánh dấu tin nhắn đã đọc.
     */
    @POST("api/chat/messages/read")
    Single<ResponseData<Void>> markAsRead(
            @Body MarkReadRequest request);

    /**
     * DELETE - Xóa tin nhắn.
     */
    @DELETE("api/chat/messages/{messageId}")
    Call<Void> deleteMessage(@Path("messageId") long messageId);

    /**
     * POST - Gửi file/ảnh trong hội thoại.
     */
    @Multipart
    @POST("api/chat/conversations/{conversationId}/files")
    Single<ResponseData<MessageDto>> sendFileMessage(
            @Path("conversationId") long conversationId,
            @Part MultipartBody.Part file,
            @Part("content") RequestBody content);

    // ========== Notifications ==========

    /**
     * POST - Register FCM Token
     */
    @POST("api/notifications/tokens")
    Single<ResponseData<Object>> registerFcmToken(@Body com.trototvn.trototandroid.data.model.notification.FcmTokenRequest request);

    /**
     * POST - Unregister FCM Token (Logout)
     */
    @POST("api/notifications/tokens/unregister")
    Single<ResponseData<String>> unregisterFcmToken(@Body com.trototvn.trototandroid.data.model.notification.FcmTokenRequest request);

    // ========== Video Call ==========

    /**
     * GET - Retrieve ICE Server configurations for WebRTC
     */
    @GET("api/video-call/ice-config")
    Single<ResponseData<IceConfigDto>> getIceConfig();
}
