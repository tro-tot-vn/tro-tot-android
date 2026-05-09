package com.trototvn.trototandroid.ui.main.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.data.local.entity.ConversationUIModel;
import com.trototvn.trototandroid.data.repository.ChatRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * ChatListViewModel - Quản lý logic màn hình danh sách hội thoại.
 */
@HiltViewModel
public class ChatListViewModel extends BaseViewModel {

    private final ChatRepository chatRepository;

    private final MutableLiveData<List<ConversationUIModel>> conversationsLiveData = new MutableLiveData<>();

    @Inject
    public ChatListViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        fetchConversationsFromApi();
        observeConversations();
    }

    /**
     * Đồng bộ danh sách hội thoại từ server về Room DB.
     */
    public void fetchConversationsFromApi(Runnable onComplete) {
        addDisposable(chatRepository.fetchConversations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Timber.d("Fetched conversations successfully");
                            if (onComplete != null) onComplete.run();
                        },
                        error -> {
                            Timber.e(error, "Failed to fetch conversations");
                            if (onComplete != null) onComplete.run();
                        }));
    }

    private void fetchConversationsFromApi() {
        fetchConversationsFromApi(null);
    }

    /**
     * Quan sát luồng dữ liệu danh sách hội thoại từ Room
     */
    private void observeConversations() {
        addDisposable(chatRepository.observeConversations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        conversations -> conversationsLiveData.setValue(conversations),
                        error -> Timber.e(error, "Observe conversations error")));
    }

    public LiveData<List<ConversationUIModel>> getConversationsLiveData() {
        return conversationsLiveData;
    }
}
