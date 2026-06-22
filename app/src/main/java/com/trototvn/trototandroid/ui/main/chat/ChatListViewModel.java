package com.trototvn.trototandroid.ui.main.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.local.entity.ConversationUIModel;
import com.trototvn.trototandroid.data.repository.ChatRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * ChatListViewModel - Quản lý logic màn hình danh sách hội thoại.
 */
@HiltViewModel
public class ChatListViewModel extends BaseViewModel {

    private final ChatRepository chatRepository;

    private final MutableLiveData<List<ConversationUIModel>> conversationsLiveData = new MutableLiveData<>();
    private final BehaviorSubject<String> querySubject = BehaviorSubject.createDefault("");

    private String currentQuery = "";
    private int offset = 0;
    private boolean isLastPage = false;
    private boolean isLoading = false;

    @Inject
    public ChatListViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        observeConversations();
        fetchConversationsFromApi();
    }

    /**
     * Đồng bộ danh sách hội thoại từ server về Room DB (trang đầu tiên).
     */
    public void fetchConversationsFromApi(Runnable onComplete) {
        offset = 0;
        isLastPage = false;
        isLoading = true;
        addDisposable(chatRepository.fetchConversations(currentQuery, 20, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> {
                            isLoading = false;
                            offset = count;
                            if (count < 20) {
                                isLastPage = true;
                            }
                            Timber.d("Fetched conversations successfully, count: %d", count);
                            if (onComplete != null) onComplete.run();
                        },
                        error -> {
                            isLoading = false;
                            Timber.e(error, "Failed to fetch conversations");
                            if (onComplete != null) onComplete.run();
                        }));
    }

    private void fetchConversationsFromApi() {
        fetchConversationsFromApi(null);
    }

    /**
     * Tìm kiếm cuộc hội thoại theo tên đối phương.
     */
    public void search(String query) {
        final String searchQuery = query != null ? query : "";
        currentQuery = searchQuery;
        offset = 0;
        isLastPage = false;
        isLoading = true;

        querySubject.onNext(searchQuery);

        addDisposable(chatRepository.fetchConversations(searchQuery, 20, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> {
                            isLoading = false;
                            offset = count;
                            if (count < 20) {
                                isLastPage = true;
                            }
                            Timber.d("Search fetched successfully, count: %d", count);
                        },
                        error -> {
                            isLoading = false;
                            Timber.e(error, "Failed to fetch search results for: %s", searchQuery);
                        }));
    }

    /**
     * Tải thêm các cuộc hội thoại tiếp theo (Phân trang).
     */
    public void loadMoreConversations() {
        if (isLoading || isLastPage) {
            return;
        }
        isLoading = true;
        addDisposable(chatRepository.fetchConversations(currentQuery, 20, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> {
                            isLoading = false;
                            offset += count;
                            if (count < 20) {
                                isLastPage = true;
                            }
                            Timber.d("Fetched more conversations, count: %d, new offset: %d", count, offset);
                        },
                        error -> {
                            isLoading = false;
                            Timber.e(error, "Failed to fetch more conversations");
                        }));
    }

    /**
     * Quan sát luồng dữ liệu danh sách hội thoại từ Room (tự động chuyển đổi dựa trên từ khóa tìm kiếm).
     */
    private void observeConversations() {
        addDisposable(querySubject
                .distinctUntilChanged()
                .switchMap(query -> {
                    if (query == null || query.trim().isEmpty()) {
                        return chatRepository.observeConversations().toObservable();
                    } else {
                        return chatRepository.observeConversationsFiltered(query.trim()).toObservable();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        conversations -> conversationsLiveData.setValue(conversations),
                        error -> Timber.e(error, "Observe conversations error")));
    }

    public LiveData<List<ConversationUIModel>> getConversationsLiveData() {
        return conversationsLiveData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isLastPage() {
        return isLastPage;
    }
}
