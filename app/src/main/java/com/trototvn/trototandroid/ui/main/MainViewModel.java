package com.trototvn.trototandroid.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.data.repository.ChatRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketIOManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * MainViewModel - Handles background processes for MainActivity (e.g., user logout operations)
 */
@HiltViewModel
public class MainViewModel extends BaseViewModel {

    private final AuthRepository authRepository;
    private final ChatRepository chatRepository;
    private final SessionManager sessionManager;
    private final SocketIOManager socketIOManager;

    private final MutableLiveData<Boolean> logoutCompleted = new MutableLiveData<>();
    private final MutableLiveData<ChatRepository.InAppNotificationEvent> inAppNotificationEvent = new MutableLiveData<>();
    private boolean isLoggingOut = false;

    public LiveData<ChatRepository.InAppNotificationEvent> getInAppNotificationEvent() {
        return inAppNotificationEvent;
    }

    public void clearInAppNotificationEvent() {
        inAppNotificationEvent.setValue(null);
    }

    @Inject
    public MainViewModel(
            AuthRepository authRepository,
            ChatRepository chatRepository,
            SessionManager sessionManager,
            SocketIOManager socketIOManager) {
        this.authRepository = authRepository;
        this.chatRepository = chatRepository;
        this.sessionManager = sessionManager;
        this.socketIOManager = socketIOManager;
    }

    public LiveData<Boolean> getLogoutCompleted() {
        return logoutCompleted;
    }

    /**
     * Initialize database handshake sync and socket connections if user is logged in
     */
    public void initSessionSync() {
        if (sessionManager.isLoggedIn()) {
            // 1. Start database handshake sync
            chatRepository.performHandshakeSync();

            // 2. Connect socket
            socketIOManager.connect(sessionManager.getUserId());

            // 3. Listen to incoming messages globally
            chatRepository.observeIncomingMessages();

            // 4. Listen to incoming calls globally
            chatRepository.observeIncomingCalls();

            // 5. Listen to in-app notification events
            addDisposable(
                    chatRepository.getInAppNotificationEvents()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    event -> inAppNotificationEvent.setValue(event),
                                    throwable -> Timber.e(throwable, "Error observing in-app notifications")
                            )
            );

            // 6. Listen to forced logout events globally
            addDisposable(
                    sessionManager.observeSessionExpiration()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    expired -> {
                                        if (expired && !isLoggingOut) {
                                            Timber.w("Forced logout triggered by session expiration.");
                                            logout();
                                        }
                                    },
                                    throwable -> Timber.e(throwable, "Error observing session expiration")
                            )
            );
        }
    }

    /**
     * Coordinate user logout (socket disconnect, FCM token unregister, local DB clear)
     */
    public void logout() {
        if (isLoggingOut) return;
        isLoggingOut = true;

        chatRepository.stopObservingIncomingMessages();
        chatRepository.stopObservingIncomingCalls();
        socketIOManager.disconnect();

        String fcmToken = sessionManager.getFcmToken();
        Completable unregisterCompletable;
        if (fcmToken != null && !fcmToken.isEmpty()) {
            unregisterCompletable = authRepository.unregisterFcmToken(fcmToken);
        } else {
            unregisterCompletable = Completable.complete();
        }

        addDisposable(
                Completable.mergeArrayDelayError(
                        unregisterCompletable.subscribeOn(Schedulers.io()),
                        chatRepository.clearAllData().subscribeOn(Schedulers.io())
                )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    sessionManager.clearSession();
                    logoutCompleted.setValue(true);
                })
                .subscribe(
                        () -> Timber.d("Logout process completed (FCM unregistration & database clear)"),
                        throwable -> Timber.e(throwable, "Errors occurred during logout process")
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.stopObservingIncomingMessages();
        chatRepository.stopObservingIncomingCalls();
    }
}
