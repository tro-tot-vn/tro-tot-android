package com.trototvn.trototandroid.ui.main.savedposts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.repository.ProfileRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class SavedPostsViewModel extends ViewModel {
    private final ProfileRepository profileRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Resource<List<Post>>> savedPosts = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> unsaveStatus = new MutableLiveData<>();

    @Inject
    public SavedPostsViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public LiveData<Resource<List<Post>>> getSavedPostsLiveData() {
        return savedPosts;
    }

    public LiveData<Resource<Void>> getUnsaveStatusLiveData() {
        return unsaveStatus;
    }

    public void fetchSavedPosts() {
        savedPosts.setValue(Resource.loading(null));
        disposables.add(profileRepository.getSavedPosts()
                .subscribe(
                        savedPosts::setValue,
                        throwable -> savedPosts.setValue(Resource.error(throwable.getMessage(), null))
                ));
    }

    public void unsavePost(int postId) {
        disposables.add(profileRepository.unsavePost(postId)
                .subscribe(
                        unsaveStatus::setValue,
                        throwable -> unsaveStatus.setValue(Resource.error(throwable.getMessage(), null))
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
