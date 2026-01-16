package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.ChangePasswordRequest;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.profile.CustomerProfile;
import com.trototvn.trototandroid.data.model.profile.Subscription;
import com.trototvn.trototandroid.data.remote.ProfileApiService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * Implementation of ProfileRepository
 * Handles profile-related data operations with clean error handling
 */
@Singleton
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfileApiService apiService;

    @Inject
    public ProfileRepositoryImpl(ProfileApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<Resource<CustomerProfile>> getMyProfile() {
        return apiService.getMyProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<CustomerProfile>error(
                                "Không thể tải thông tin hồ sơ",
                                null
                        );
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching profile");
                    return Resource.error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null
                    );
                });
    }

    @Override
    public Single<Resource<Void>> updateProfile(CustomerProfile profile) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("firstName", profile.getFirstName());
        profileData.put("lastName", profile.getLastName());
        profileData.put("email", profile.getEmail());
        profileData.put("bio", profile.getBio());
        profileData.put("gender", profile.getGender());
        
        if (profile.getBirthday() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            profileData.put("birthDate", sdf.format(profile.getBirthday()));
        }
        
        profileData.put("currentCity", profile.getCurrentCity());
        profileData.put("currentDistrict", profile.getCurrentDistrict());
        profileData.put("currentJob", profile.getCurrentJob());

        return apiService.updateProfile(profileData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error updating profile");
                    return Resource.error(
                            "Không thể cập nhật hồ sơ",
                            null
                    );
                });
    }

    @Override
    public Single<Resource<Void>> updateProfileWithAvatar(CustomerProfile profile, File avatarFile) {
        // Create RequestBody instances for text fields
        RequestBody firstName = RequestBody.create(MediaType.parse("text/plain"), profile.getFirstName());
        RequestBody lastName = RequestBody.create(MediaType.parse("text/plain"), profile.getLastName());
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), profile.getEmail());
        RequestBody bio = RequestBody.create(MediaType.parse("text/plain"), profile.getBio() != null ? profile.getBio() : "");
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), profile.getGender());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        RequestBody birthDate = RequestBody.create(MediaType.parse("text/plain"), 
                profile.getBirthday() != null ? sdf.format(profile.getBirthday()) : "");
        
        RequestBody city = RequestBody.create(MediaType.parse("text/plain"), 
                profile.getCurrentCity() != null ? profile.getCurrentCity() : "");
        RequestBody district = RequestBody.create(MediaType.parse("text/plain"), 
                profile.getCurrentDistrict() != null ? profile.getCurrentDistrict() : "");
        RequestBody job = RequestBody.create(MediaType.parse("text/plain"), 
                profile.getCurrentJob() != null ? profile.getCurrentJob() : "");

        // Create MultipartBody.Part for avatar
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), avatarFile);
        MultipartBody.Part avatar = MultipartBody.Part.createFormData("avatarFile", avatarFile.getName(), requestFile);

        return apiService.updateProfileWithAvatar(
                        firstName, lastName, email, bio, gender, birthDate, city, district, job, avatar
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error updating profile with avatar");
                    return Resource.error(
                            "Không thể cập nhật ảnh đại diện",
                            null
                    );
                });
    }

    @Override
    public Single<Resource<List<Post>>> getSavedPosts() {
        return apiService.getSavedPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<List<Post>>error("Không thể tải tin đã lưu", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching saved posts");
                    return Resource.error("Lỗi kết nối", null);
                });
    }

    @Override
    public Single<Resource<Void>> savePost(int postId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("postId", postId);

        return apiService.savePost(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error saving post");
                    return Resource.error("Không thể lưu tin", null);
                });
    }

    @Override
    public Single<Resource<Void>> unsavePost(int postId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("postId", postId);

        return apiService.unsavePost(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error unsaving post");
                    return Resource.error("Không thể bỏ lưu tin", null);
                });
    }

    @Override
    public Single<Resource<List<Post>>> getViewHistory() {
        return apiService.getViewHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<List<Post>>error("Không thể tải lịch sử xem", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching view history");
                    return Resource.error("Lỗi kết nối", null);
                });
    }

    @Override
    public Single<Resource<List<Subscription>>> getSubscriptions() {
        return apiService.getSubscriptions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<List<Subscription>>error("Không thể tải đăng ký", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching subscriptions");
                    return Resource.error("Lỗi kết nối", null);
                });
    }

    @Override
    public Single<Resource<Subscription>> createSubscription(String city, String district) {
        Map<String, String> body = new HashMap<>();
        body.put("city", city);
        body.put("district", district);

        return apiService.createSubscription(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<Subscription>error("Không thể tạo đăng ký", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error creating subscription");
                    return Resource.error("Đăng ký đã tồn tại hoặc lỗi kết nối", null);
                });
    }

    @Override
    public Single<Resource<Void>> deleteSubscription(int subscriptionId) {
        return apiService.deleteSubscription(subscriptionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error deleting subscription");
                    return Resource.error("Không thể xóa đăng ký", null);
                });
    }

    @Override
    public Single<Resource<Void>> changePassword(String oldPassword, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);

        return apiService.changePassword(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error changing password");
                    return Resource.error("Mật khẩu cũ không đúng hoặc lỗi kết nối", null);
                });
    }
}
