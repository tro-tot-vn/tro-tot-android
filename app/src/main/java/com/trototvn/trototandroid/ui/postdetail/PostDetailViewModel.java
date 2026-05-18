package com.trototvn.trototandroid.ui.postdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.data.model.rating.Rating;
import com.trototvn.trototandroid.data.model.rating.RatingListResponse;
import com.trototvn.trototandroid.data.model.rating.RatingStats;
import com.trototvn.trototandroid.data.repository.PostDetailRepository;
import com.trototvn.trototandroid.data.repository.RatingRepository;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * PostDetailViewModel - manages post detail screen state and ratings
 * Clean architecture MVVM pattern
 */
@HiltViewModel
public class PostDetailViewModel extends BaseViewModel {

    private final PostDetailRepository repository;
    private final RatingRepository ratingRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<Resource<PostDetail>> postDetail = new MutableLiveData<>();
    private final MutableLiveData<Resource<RatingStats>> ratingStats = new MutableLiveData<>();
    private final MutableLiveData<Resource<Rating>> myRating = new MutableLiveData<>();
    private final MutableLiveData<Resource<RatingListResponse>> ratingsList = new MutableLiveData<>();

    @Inject
    public PostDetailViewModel(PostDetailRepository repository, RatingRepository ratingRepository, SessionManager sessionManager) {
        this.repository = repository;
        this.ratingRepository = ratingRepository;
        this.sessionManager = sessionManager;
    }

    public LiveData<Resource<PostDetail>> getPostDetail() {
        return postDetail;
    }

    public LiveData<Resource<RatingStats>> getRatingStats() {
        return ratingStats;
    }

    public LiveData<Resource<Rating>> getMyRating() {
        return myRating;
    }

    public LiveData<Resource<RatingListResponse>> getRatingsList() {
        return ratingsList;
    }

    /**
     * Load post detail by ID
     */
    public void loadPostDetail(int postId) {
        postDetail.setValue(Resource.loading(null));

        compositeDisposable.add(
                repository.getPostDetail(postId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                postDetail::setValue,
                                throwable -> {
                                    Timber.e(throwable, "Error loading post detail in ViewModel");
                                    postDetail.setValue(Resource.error("Lỗi khi tải tin đăng", null));
                                }
                        )
        );
    }

    /**
     * Load rating stats
     */
    public void loadRatingStats(int postId) {
        ratingStats.setValue(Resource.loading(null));

        compositeDisposable.add(
                ratingRepository.getRatingStats(postId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                ratingStats::setValue,
                                throwable -> {
                                    Timber.e(throwable, "Error loading rating stats");
                                    ratingStats.setValue(Resource.error("Lỗi khi tải điểm đánh giá", null));
                                }
                        )
        );
    }

    /**
     * Load current user's rating
     */
    public void loadMyRating(int postId) {
        if (!isAuthenticated()) {
            myRating.setValue(Resource.error("Chưa đăng nhập", null));
            return;
        }
        myRating.setValue(Resource.loading(null));

        compositeDisposable.add(
                ratingRepository.getMyRating(postId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                myRating::setValue,
                                throwable -> {
                                    Timber.e(throwable, "Error loading my rating");
                                    myRating.setValue(Resource.error("Chưa có đánh giá của bạn", null));
                                }
                        )
        );
    }

    /**
     * Load reviews list
     */
    public void loadRatings(int postId) {
        ratingsList.setValue(Resource.loading(null));

        compositeDisposable.add(
                ratingRepository.getRatings(postId, 50, null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                ratingsList::setValue,
                                throwable -> {
                                    Timber.e(throwable, "Error loading ratings list");
                                    ratingsList.setValue(Resource.error("Lỗi khi tải danh sách nhận xét", null));
                                }
                        )
        );
    }

    /**
     * Submit rating
     */
    public void submitRating(int postId, int stars, String comment) {
        if (!isAuthenticated()) return;

        compositeDisposable.add(
                ratingRepository.addRating(postId, stars, comment)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        // Reload all rating info on success
                                        loadRatingStats(postId);
                                        loadMyRating(postId);
                                        loadRatings(postId);
                                    }
                                },
                                throwable -> Timber.e(throwable, "Error submitting rating")
                        )
        );
    }

    /**
     * Delete rating
     */
    public void deleteRating(int postId) {
        if (!isAuthenticated()) return;

        compositeDisposable.add(
                ratingRepository.deleteMyRating(postId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        // Reload all rating info on success
                                        loadRatingStats(postId);
                                        loadMyRating(postId);
                                        loadRatings(postId);
                                    }
                                },
                                throwable -> Timber.e(throwable, "Error deleting rating")
                        )
        );
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        String token = sessionManager.getToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Get masked phone number for non-authenticated users
     * Example: 0987654321 -> 0987***
     */
    public String getMaskedPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return phone.substring(0, phone.length() - 3) + "***";
    }
}
