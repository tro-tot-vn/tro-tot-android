package com.trototvn.trototandroid.ui.main.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.post.RecommendationResponse;
import com.trototvn.trototandroid.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * ViewModel for Home screen
 * Manages UI state and business logic for latest posts and recommendations
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final PostRepository postRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    // Latest Posts
    private final MutableLiveData<Resource<List<Post>>> latestPosts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingMoreLatestPosts = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasMoreLatestPosts = new MutableLiveData<>(false);
    
    private int currentLatestPostsPage = 0;
    private final int latestPostsPageSize = 10;
    private final List<Post> allLatestPosts = new ArrayList<>();
    // Recommendations
    private final MutableLiveData<Resource<List<Post>>> recommendations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasMoreRecommendations = new MutableLiveData<>(false);
    
    // Recommendation state
    private Integer recommendationLogId;
    private int currentPage = 0;
    private final int pageSize = 20;
    private final List<Post> allRecommendations = new ArrayList<>();

    @Inject
    public HomeViewModel(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // ========== Latest Posts ==========

    public LiveData<Resource<List<Post>>> getLatestPosts() {
        return latestPosts;
    }

    public LiveData<Boolean> getIsLoadingMoreLatestPosts() {
        return isLoadingMoreLatestPosts;
    }

    public LiveData<Boolean> getHasMoreLatestPosts() {
        return hasMoreLatestPosts;
    }

    public void loadLatestPosts() {
        currentLatestPostsPage = 0;
        allLatestPosts.clear();
        
        latestPosts.setValue(Resource.loading(null));
        loadLatestPostsPage(1);
    }

    public void loadMoreLatestPosts() {
        if (Boolean.TRUE.equals(isLoadingMoreLatestPosts.getValue()) || 
            Boolean.FALSE.equals(hasMoreLatestPosts.getValue())) {
            return;
        }
        
        loadLatestPostsPage(currentLatestPostsPage + 1);
    }

    private void loadLatestPostsPage(int page) {
        if (page == 1) {
            latestPosts.setValue(Resource.loading(null));
        } else {
            isLoadingMoreLatestPosts.setValue(true);
        }
        
        disposable.add(
                postRepository.getLatestPosts(page, latestPostsPageSize)
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                        currentLatestPostsPage = page;
                                        
                                        if (page == 1) {
                                            allLatestPosts.clear();
                                        }
                                        
                                        List<Post> newData = resource.getData();
                                        if (newData != null && !newData.isEmpty()) {
                                            allLatestPosts.addAll(newData);
                                            hasMoreLatestPosts.setValue(newData.size() == latestPostsPageSize);
                                        } else {
                                            hasMoreLatestPosts.setValue(false);
                                        }
                                        
                                        latestPosts.setValue(Resource.success(new ArrayList<>(allLatestPosts)));
                                        isLoadingMoreLatestPosts.setValue(false);
                                    } else {
                                        if (page == 1) {
                                            latestPosts.setValue(Resource.error("Không thể tải bài đăng mới", null));
                                        }
                                        isLoadingMoreLatestPosts.setValue(false);
                                        hasMoreLatestPosts.setValue(false);
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Error loading latest posts");
                                    if (page == 1) {
                                        latestPosts.setValue(Resource.error("Không thể tải bài đăng mới", null));
                                    }
                                    isLoadingMoreLatestPosts.setValue(false);
                                    hasMoreLatestPosts.setValue(false);
                                }
                        )
        );
    }

    // ========== Recommendations ==========

    public LiveData<Resource<List<Post>>> getRecommendations() {
        return recommendations;
    }

    public LiveData<Boolean> getIsLoadingMore() {
        return isLoadingMore;
    }

    public LiveData<Boolean> getHasMoreRecommendations() {
        return hasMoreRecommendations;
    }

    /**
     * Load first page of recommendations
     */
    public void loadRecommendations() {
        // Reset state
        currentPage = 0;
        allRecommendations.clear();
        recommendationLogId = null;
        
        recommendations.setValue(Resource.loading(null));
        
        loadRecommendationsPage(1);
    }

    /**
     * Load more recommendations (pagination)
     */
    public void loadMoreRecommendations() {
        if (Boolean.TRUE.equals(isLoadingMore.getValue()) || 
            Boolean.FALSE.equals(hasMoreRecommendations.getValue())) {
            return; // Already loading or no more data
        }
        
        loadRecommendationsPage(currentPage + 1);
    }

    private void loadRecommendationsPage(int page) {
        if (page == 1) {
            recommendations.setValue(Resource.loading(null));
        } else {
            isLoadingMore.setValue(true);
        }
        
        disposable.add(
                postRepository.getRecommendations(page, pageSize, recommendationLogId)
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                        RecommendationResponse response = resource.getData();
                                        
                                        // Update state
                                        currentPage = page;
                                        if (response.getRecommendationLogId() != null) {
                                            recommendationLogId = response.getRecommendationLogId();
                                        }
                                        
                                        // Append or set data
                                        if (page == 1) {
                                            allRecommendations.clear();
                                        }
                                        
                                        if (response.getData() != null && !response.getData().isEmpty()) {
                                            allRecommendations.addAll(response.getData());
                                        }
                                        
                                        // Update pagination state
                                        if (response.getPagination() != null) {
                                            hasMoreRecommendations.setValue(response.getPagination().isHasMore());
                                        } else {
                                            hasMoreRecommendations.setValue(false);
                                        }
                                        
                                        // Emit success with all data
                                        recommendations.setValue(Resource.success(new ArrayList<>(allRecommendations)));
                                        isLoadingMore.setValue(false);
                                    } else {
                                        if (page == 1) {
                                            recommendations.setValue(Resource.error("Không thể tải gợi ý", null));
                                        }
                                        isLoadingMore.setValue(false);
                                        hasMoreRecommendations.setValue(false);
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Error loading recommendations");
                                    if (page == 1) {
                                        recommendations.setValue(Resource.error("Không thể tải gợi ý", null));
                                    }
                                    isLoadingMore.setValue(false);
                                    hasMoreRecommendations.setValue(false);
                                }
                        )
        );
    }

    /**
     * Retry loading data
     */
    public void retry() {
        loadLatestPosts();
        loadRecommendations();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
