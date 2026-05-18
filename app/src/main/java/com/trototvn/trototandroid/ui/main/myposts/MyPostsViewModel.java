package com.trototvn.trototandroid.ui.main.myposts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.MyPost;
import com.trototvn.trototandroid.data.model.post.MyPostsResponse;
import com.trototvn.trototandroid.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * ViewModel for MyPostsFragment
 * Manages post listing states, filtering by status, endless cursor pagination, and hide/unhide logic
 */
@HiltViewModel
public class MyPostsViewModel extends ViewModel {

    private final PostRepository postRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private final MutableLiveData<Resource<List<MyPost>>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> actionStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> filterStatusLiveData = new MutableLiveData<>(null); // null means "ALL"

    private final List<MyPost> currentList = new ArrayList<>();
    private Integer nextCursor = null;
    private boolean hasMore = true;
    private boolean isLoading = false;
    private static final int LIMIT = 10;

    @Inject
    public MyPostsViewModel(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public LiveData<Resource<List<MyPost>>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<Resource<Void>> getActionStateLiveData() {
        return actionStateLiveData;
    }

    public LiveData<String> getFilterStatusLiveData() {
        return filterStatusLiveData;
    }

    /**
     * Set active filter status and trigger reload
     */
    public void setFilterStatus(String status) {
        if (isLoading) return;
        filterStatusLiveData.setValue(status);
        loadMyPosts(true);
    }

    /**
     * Load my posts with cursor pagination
     *
     * @param isRefresh true if we want to clear previous pages and reload from top
     */
    public void loadMyPosts(boolean isRefresh) {
        if (isLoading) return;
        if (!isRefresh && !hasMore) return;

        isLoading = true;
        if (isRefresh) {
            nextCursor = null;
            hasMore = true;
            postsLiveData.setValue(Resource.loading(null));
        }

        String status = filterStatusLiveData.getValue();

        disposable.add(postRepository.getMyPosts(status, nextCursor, LIMIT)
                .subscribe(resource -> {
                    isLoading = false;
                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                        MyPostsResponse response = resource.getData();
                        List<MyPost> newPosts = response.getDataPag();
                        nextCursor = response.getNextCursor();
                        hasMore = response.isHasMore();

                        if (isRefresh) {
                            currentList.clear();
                        }
                        if (newPosts != null) {
                            currentList.addAll(newPosts);
                        }
                        postsLiveData.setValue(Resource.success(new ArrayList<>(currentList)));
                    } else {
                        postsLiveData.setValue(Resource.error(resource.getMessage(), isRefresh ? null : new ArrayList<>(currentList)));
                    }
                }, throwable -> {
                    isLoading = false;
                    Timber.e(throwable, "Error loading user posts");
                    postsLiveData.setValue(Resource.error(throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", isRefresh ? null : new ArrayList<>(currentList)));
                }));
    }

    /**
     * Hide or unhide a post dynamically
     */
    public void togglePostVisibility(MyPost post) {
        boolean isHidden = "HIDDEN".equals(post.getStatus());
        int postId = post.getPostId();

        actionStateLiveData.setValue(Resource.loading(null));

        disposable.add((isHidden ? postRepository.unhidePost(postId) : postRepository.hidePost(postId))
                .subscribe(resource -> {
                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                        // Dynamically update the post's status in our local cache list for seamless transition
                        String newStatus = isHidden ? "APPROVED" : "HIDDEN"; // Note: Backend handles APPROVED on unhide
                        for (int i = 0; i < currentList.size(); i++) {
                            if (currentList.get(i).getPostId() == postId) {
                                currentList.get(i).setStatus(newStatus);
                                break;
                            }
                        }
                        postsLiveData.setValue(Resource.success(new ArrayList<>(currentList)));
                        actionStateLiveData.setValue(Resource.success(null));
                    } else {
                        actionStateLiveData.setValue(Resource.error(resource.getMessage(), null));
                    }
                }, throwable -> {
                    Timber.e(throwable, "Error toggling post visibility");
                    actionStateLiveData.setValue(Resource.error(throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", null));
                }));
    }

    public boolean hasMore() {
        return hasMore;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
