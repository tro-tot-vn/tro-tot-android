package com.trototvn.trototandroid.ui.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Base ViewModel with RxJava support
 */
public abstract class BaseViewModel extends ViewModel {

    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * Add disposable to composite for auto-cleanup
     */
    protected void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    /**
     * Helper method to handle Resource state transitions
     */
    protected <T> void handleLoading(MutableLiveData<Resource<T>> liveData) {
        liveData.setValue(Resource.loading(null));
    }

    protected <T> void handleSuccess(MutableLiveData<Resource<T>> liveData, T data) {
        liveData.setValue(Resource.success(data));
    }

    protected <T> void handleError(MutableLiveData<Resource<T>> liveData, String message) {
        liveData.setValue(Resource.error(message, null));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
