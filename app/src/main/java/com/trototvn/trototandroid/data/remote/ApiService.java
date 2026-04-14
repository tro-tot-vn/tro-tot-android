package com.trototvn.trototandroid.data.remote;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.User;
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
import com.trototvn.trototandroid.data.model.post.ContactLogRequest;
import com.trototvn.trototandroid.data.model.post.HidePostRequest;
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
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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
    Single<ResponseData<RegisterResponse>> register(@Body RegisterRequest request);

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
    @GET("api/post/latest-post")
    Single<ResponseData<List<Post>>> getLatestPosts(@Query("limit") int limit);

    /**
     * GET - Post Detail by ID
     */
    @GET("api/post/{postId}/detail")
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
    @POST("api/customer/saved-posts")
    Single<ResponseData<Void>> savePost(@Body SavePostRequest request);

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
    @POST("api/customer/rate/{postId}")
    Single<ResponseData<Void>> addRating(
            @Path("postId") int postId,
            @Body AddRatingRequest request);

    /**
     * GET - Get ratings list with cursor pagination
     */
    @GET("api/customer/rate/{postId}")
    Single<ResponseData<RatingListResponse>> getRatings(
            @Path("postId") int postId,
            @Query("limit") int limit,
            @Query("cursor") String cursor // Date string
    );

    /**
     * GET - Get my rating on specific post
     * Requires authentication
     */
    @GET("api/customer/my-rate/{postId}")
    Single<ResponseData<Rating>> getMyRating(@Path("postId") int postId);

    /**
     * DELETE - Delete my rating
     * Requires authentication
     */
    @DELETE("api/customer/my-rate/{postId}")
    Single<ResponseData<Void>> deleteMyRating(@Path("postId") int postId);

    /**
     * GET - Get rating statistics (avg, count)
     */
    @GET("api/customer/avg-rate/{postId}")
    Single<ResponseData<RatingStats>> getRatingStats(@Path("postId") int postId);

    // ========== My Posts ==========

    /**
     * GET - Get my posts with status filter and pagination
     */
    @GET("api/post/my-posts")
    Single<ResponseData<MyPostsResponse>> getMyPosts(
            @Query("status") String status,
            @Query("cursor") Integer cursor,
            @Query("limit") int limit);

    /**
     * POST - Hide my post
     */
    @POST("api/post/hide")
    Single<ResponseData<Void>> hidePost(@Body HidePostRequest request);

    /**
     * POST - Unhide my post
     */
    @POST("api/post/unhide")
    Single<ResponseData<Void>> unhidePost(@Body HidePostRequest request);

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
     * GET - Lấy danh sách hội thoại.
     */
    @GET("api/chat/conversations")
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
     * POST - Gửi file/ảnh trong hội thoại.
     */
    @Multipart
    @POST("api/chat/conversations/{conversationId}/files")
    Single<ResponseData<MessageDto>> sendFileMessage(
            @Path("conversationId") long conversationId,
            @Part MultipartBody.Part file,
            @Part("content") RequestBody content);
}
