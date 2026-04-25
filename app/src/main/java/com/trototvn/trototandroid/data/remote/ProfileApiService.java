package com.trototvn.trototandroid.data.remote;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.auth.ChangePasswordRequest;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.profile.CustomerProfile;
import com.trototvn.trototandroid.data.model.profile.Subscription;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Profile-specific API endpoints
 * Separated for clean organization
 */
public interface ProfileApiService {
    
    // ========== Profile ==========
    
    @GET("api/customer/my-profile")
    Single<ResponseData<CustomerProfile>> getMyProfile();
    
    @PUT("api/customer/my-profile")
    Single<ResponseData<Void>> updateProfile(@Body Map<String, Object> profileData);
    
    @Multipart
    @PUT("api/customer/my-profile")
    Single<ResponseData<Void>> updateProfileWithAvatar(
            @Part("firstName") okhttp3.RequestBody firstName,
            @Part("lastName") okhttp3.RequestBody lastName,
            @Part("email") okhttp3.RequestBody email,
            @Part("bio") okhttp3.RequestBody bio,
            @Part("gender") okhttp3.RequestBody gender,
            @Part("birthDate") okhttp3.RequestBody birthDate,
            @Part("currentCity") okhttp3.RequestBody currentCity,
            @Part("currentDistrict") okhttp3.RequestBody currentDistrict,
            @Part("currentJob") okhttp3.RequestBody currentJob,
            @Part okhttp3.MultipartBody.Part avatar
    );
    
    // ========== Saved Posts ==========
    
    @GET("api/customer/saved-posts")
    Single<ResponseData<List<Post>>> getSavedPosts();
    
    @POST("api/customer/save-post")
    Single<ResponseData<Void>> savePost(@Body Map<String, Integer> body);
    
    @HTTP(method = "DELETE", path = "api/customer/unsave-post", hasBody = true)
    Single<ResponseData<Void>> unsavePost(@Body Map<String, Integer> body);
    
    // ========== View History ==========
    
    @GET("api/customer/view-history")
    Single<ResponseData<List<Post>>> getViewHistory();
    
    // ========== Subscriptions ==========
    
    @GET("api/customer/subscriptions")
    Single<ResponseData<List<Subscription>>> getSubscriptions();
    
    @POST("api/customer/subscriptions")
    Single<ResponseData<Subscription>> createSubscription(@Body Map<String, String> body);
    
    @DELETE("api/customer/subscriptions/{id}")
    Single<ResponseData<Void>> deleteSubscription(@Path("id") int subscriptionId);
    
    // ========== Account ==========
    
    @POST("api/auth/change-password")
    Single<ResponseData<Void>> changePassword(@Body ChangePasswordRequest request);
}
