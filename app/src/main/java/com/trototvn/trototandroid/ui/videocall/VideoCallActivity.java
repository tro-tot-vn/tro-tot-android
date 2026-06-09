package com.trototvn.trototandroid.ui.videocall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.video.IceConfigDto;
import com.trototvn.trototandroid.databinding.ActivityVideoCallBinding;
import com.trototvn.trototandroid.services.CallForegroundService;
import com.trototvn.trototandroid.ui.base.BaseActivity;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketEvents;
import com.trototvn.trototandroid.utils.SocketIOManager;
import com.trototvn.trototandroid.utils.WebRtcManager;

import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * Giao diện đàm thoại Video Call (Video Call Conversation Screen).
 * Quản lý vòng đời kết nối WebRTC Peer, trao đổi SDP Offer/Answer,
 * hiển thị luồng Camera cục bộ/đối phương và giải phóng tài nguyên.
 */
@AndroidEntryPoint
public class VideoCallActivity extends BaseActivity<ActivityVideoCallBinding> {

    @Inject
    WebRtcManager webRtcManager;

    @Inject
    SocketIOManager socketIOManager;

    @Inject
    Gson gson;

    @Inject
    SessionManager sessionManager;

    private VideoCallViewModel viewModel;

    private String roomId;
    private String partnerId;
    private String partnerName;
    private boolean isCaller;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
                Boolean recordAudioGranted = result.get(Manifest.permission.RECORD_AUDIO);
                
                if (cameraGranted != null && cameraGranted && recordAudioGranted != null && recordAudioGranted) {
                    Timber.d("Quyền CAMERA và RECORD_AUDIO đã được cấp đầy đủ");
                    viewModel.fetchIceConfig();
                } else {
                    Timber.w("Quyền CAMERA hoặc RECORD_AUDIO bị từ chối");
                    showToast("Ứng dụng cần quyền Camera và Micro để thực hiện cuộc gọi video");
                    finish();
                }
            });

    private boolean isMuted = false;
    private boolean isVideoDisabled = false;
    private boolean isPeerConnected = false;

    private Handler timerHandler;
    private int secondsElapsed = 0;

    private final SdpObserver sdpObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Timber.d("sdpObserver - Tạo thành công SDP: %s", sessionDescription.type.name());
            
            // Thiết lập mô tả cục bộ
            webRtcManager.setLocalDescription(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sdp) {}
                
                @Override
                public void onSetSuccess() {
                    Timber.d("sdpObserver - Thiết lập Local Description thành công");
                    
                    // Gửi SDP Offer hoặc Answer lên Signaling Server
                    JsonObject payload = new JsonObject();
                    payload.addProperty("roomId", roomId);
                    
                    JsonObject sdpJson = new JsonObject();
                    sdpJson.addProperty("type", sessionDescription.type.canonicalForm());
                    sdpJson.addProperty("sdp", sessionDescription.description);
                    
                    if (sessionDescription.type == SessionDescription.Type.OFFER) {
                        payload.add("offer", sdpJson);
                        socketIOManager.emit(SocketEvents.EMIT_OFFER, payload);
                        Timber.d("Đã gửi SDP Offer lên server");
                    } else if (sessionDescription.type == SessionDescription.Type.ANSWER) {
                        payload.add("answer", sdpJson);
                        socketIOManager.emit(SocketEvents.EMIT_ANSWER, payload);
                        Timber.d("Đã gửi SDP Answer lên server");
                    }
                }
                
                @Override
                public void onCreateFailure(String s) {}
                
                @Override
                public void onSetFailure(String s) {
                    Timber.e("sdpObserver - Thất bại khi thiết lập Local Description: %s", s);
                }
            }, sessionDescription);
        }

        @Override
        public void onSetSuccess() {
            Timber.d("sdpObserver - onSetSuccess");
        }

        @Override
        public void onCreateFailure(String s) {
            Timber.e("sdpObserver - onCreateFailure: %s", s);
        }

        @Override
        public void onSetFailure(String s) {
            Timber.e("sdpObserver - onSetFailure: %s", s);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Trích xuất intent extras trước khi gọi super.onCreate để setupViews() và observeData() sử dụng được ngay
        roomId = getIntent().getStringExtra("roomId");
        partnerId = getIntent().getStringExtra("partnerId");
        partnerName = getIntent().getStringExtra("partnerName");
        isCaller = getIntent().getBooleanExtra("isCaller", false);

        super.onCreate(savedInstanceState);

        // Đảm bảo Socket đã kết nối (phòng hờ trường hợp app ở Killed/Background state khi mở đàm thoại)
        String userId = sessionManager.getUserId();
        if (userId != null) {
            socketIOManager.connect(userId);
        }

        // Khởi động Foreground Service để giữ kết nối camera/micro
        Intent serviceIntent = new Intent(this, CallForegroundService.class);
        serviceIntent.setAction(CallForegroundService.ACTION_START_CALL);
        serviceIntent.putExtra(CallForegroundService.EXTRA_ROOM_ID, roomId);
        serviceIntent.putExtra(CallForegroundService.EXTRA_PARTNER_NAME, partnerName);
        startService(serviceIntent);

        // Kiểm tra và yêu cầu quyền trước khi kết nối đàm thoại
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        boolean hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (hasCamera && hasAudio) {
            Timber.d("Quyền CAMERA và RECORD_AUDIO đã được cấp từ trước");
            viewModel.fetchIceConfig();
        } else {
            Timber.d("Yêu cầu quyền CAMERA và RECORD_AUDIO");
            requestPermissionLauncher.launch(permissions);
        }
    }

    @Override
    protected void setupViews() {
        binding.tvPartnerName.setText(partnerName != null ? partnerName : "Người dùng");

        // Gán sự kiện cho các nút điều khiển đàm thoại
        binding.btnMute.setOnClickListener(v -> toggleMute());
        binding.btnVideo.setOnClickListener(v -> toggleVideo());
        binding.btnSwitchCamera.setOnClickListener(v -> webRtcManager.switchCamera());
        binding.btnHangup.setOnClickListener(v -> onHangupAction());
    }

    @Override
    protected void observeData() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(VideoCallViewModel.class);
        }
        viewModel.getIceConfigLiveData().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                initWebRtc(resource.getData());
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                showToast("Không thể kết nối máy chủ ICE: " + resource.getMessage());
                finish();
            }
        });
    }

    private void initWebRtc(IceConfigDto iceConfig) {
        // 1. Khởi tạo cấu hình WebRtcManager cục bộ
        webRtcManager.init(iceConfig, roomId);

        // 2. Khởi tạo Renderer hiển thị Camera
        binding.localVideoView.init(webRtcManager.getEglContext(), null);
        binding.localVideoView.setEnableHardwareScaler(true);
        binding.localVideoView.setMirror(true);

        binding.remoteVideoView.init(webRtcManager.getEglContext(), null);
        binding.remoteVideoView.setEnableHardwareScaler(true);

        // 3. Đính kèm Local Stream để hiển thị lên UI
        if (webRtcManager.getLocalVideoTrack() != null) {
            webRtcManager.getLocalVideoTrack().addSink(binding.localVideoView);
        }

        // 4. Lắng nghe luồng Stream đối phương gửi về để vẽ lên UI
        webRtcManager.setRemoteVideoTrackListener(track -> {
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                track.addSink(binding.remoteVideoView);
            });
        });

        // 5. Lắng nghe Candidate tạo thành công từ cục bộ để bắn qua Socket
        webRtcManager.setIceCandidateListener(candidate -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("roomId", roomId);
            
            JsonObject candidateJson = new JsonObject();
            candidateJson.addProperty("candidate", candidate.sdp);
            candidateJson.addProperty("sdpMid", candidate.sdpMid);
            candidateJson.addProperty("sdpMLineIndex", candidate.sdpMLineIndex);
            
            payload.add("candidate", candidateJson);
            socketIOManager.emit(SocketEvents.EMIT_ICE_CANDIDATE, payload);
        });

        // 6. Đăng ký các sự kiện socket phục vụ quá trình trao đổi SDP
        registerNegotiationSocketListeners();

        // 7. Gửi socket join room đàm thoại
        JsonObject joinPayload = new JsonObject();
        joinPayload.addProperty("roomId", roomId);
        socketIOManager.emit(SocketEvents.EMIT_JOIN_ROOM, joinPayload);
        Timber.d("Đã gửi socket joinRoom cho roomId: %s", roomId);
    }

    private void registerNegotiationSocketListeners() {
        socketIOManager.on(SocketEvents.LISTEN_PEER_CONNECTED, onPeerConnected);
        socketIOManager.on(SocketEvents.LISTEN_OFFER, onOfferReceived);
        socketIOManager.on(SocketEvents.LISTEN_ANSWER, onAnswerReceived);
        socketIOManager.on(SocketEvents.LISTEN_ICE_CANDIDATE, onIceCandidateReceived);
        socketIOManager.on(SocketEvents.LISTEN_ENDED, onCallEnded);
        socketIOManager.on(SocketEvents.LISTEN_REJECTED, onCallRejected);
        socketIOManager.on(SocketEvents.LISTEN_ERROR, onCallError);
        
        // Lắng nghe sự kiện đối phương thoát khỏi phòng (Sự kiện MỚI)
        socketIOManager.on("video:call:roomLeft", onPeerLeft);
        socketIOManager.on("video:call:participantLeft", onPeerLeft);
    }

    private void unregisterNegotiationSocketListeners() {
        socketIOManager.off(SocketEvents.LISTEN_PEER_CONNECTED, onPeerConnected);
        socketIOManager.off(SocketEvents.LISTEN_OFFER, onOfferReceived);
        socketIOManager.off(SocketEvents.LISTEN_ANSWER, onAnswerReceived);
        socketIOManager.off(SocketEvents.LISTEN_ICE_CANDIDATE, onIceCandidateReceived);
        socketIOManager.off(SocketEvents.LISTEN_ENDED, onCallEnded);
        socketIOManager.off(SocketEvents.LISTEN_REJECTED, onCallRejected);
        socketIOManager.off(SocketEvents.LISTEN_ERROR, onCallError);
        socketIOManager.off("video:call:roomLeft", onPeerLeft);
        socketIOManager.off("video:call:participantLeft", onPeerLeft);
    }

    // ========== Xử lý tín hiệu đàm thoại Socket.IO ==========

    private final io.socket.emitter.Emitter.Listener onPeerConnected = args -> {
        Timber.d("onPeerConnected - Đối phương đã trực tuyến");
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (isCaller) {
                Timber.d("Caller đang khởi tạo Negotiation (Tạo Offer)...");
                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                webRtcManager.createOffer(sdpObserver, constraints);
            } else {
                isPeerConnected = true;
                startTimer();
            }
        });
    };

    private final io.socket.emitter.Emitter.Listener onOfferReceived = args -> {
        Timber.d("onOfferReceived - Nhận được SDP Offer");
        if (args == null || args.length == 0 || args[0] == null) return;
        try {
            JsonObject envelope = gson.fromJson(args[0].toString(), JsonObject.class);
            if (envelope.has("data")) {
                JsonObject data = envelope.getAsJsonObject("data");
                JsonObject offerJson = data.getAsJsonObject("offer");
                String sdp = offerJson.get("sdp").getAsString();

                SessionDescription remoteSdp = new SessionDescription(
                        SessionDescription.Type.OFFER,
                        sdp
                );

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    webRtcManager.setRemoteDescription(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sdp) {}

                        @Override
                        public void onSetSuccess() {
                            Timber.d("Đã thiết lập Remote Description (OFFER) thành công");
                            MediaConstraints constraints = new MediaConstraints();
                            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                            // Callee tạo SDP Answer phản hồi
                            webRtcManager.createAnswer(sdpObserver, constraints);
                        }

                        @Override
                        public void onCreateFailure(String s) {}

                        @Override
                        public void onSetFailure(String s) {
                            Timber.e("Lỗi thiết lập Remote Description (OFFER): %s", s);
                        }
                    }, remoteSdp);
                });
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi phân tích cú pháp SDP Offer");
        }
    };

    private final io.socket.emitter.Emitter.Listener onAnswerReceived = args -> {
        Timber.d("onAnswerReceived - Nhận được SDP Answer");
        if (args == null || args.length == 0 || args[0] == null) return;
        try {
            JsonObject envelope = gson.fromJson(args[0].toString(), JsonObject.class);
            if (envelope.has("data")) {
                JsonObject data = envelope.getAsJsonObject("data");
                JsonObject answerJson = data.getAsJsonObject("answer");
                String sdp = answerJson.get("sdp").getAsString();

                SessionDescription remoteSdp = new SessionDescription(
                        SessionDescription.Type.ANSWER,
                        sdp
                );

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    webRtcManager.setRemoteDescription(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sdp) {}

                        @Override
                        public void onSetSuccess() {
                            Timber.d("Đã thiết lập Remote Description (ANSWER) thành công");
                            isPeerConnected = true;
                            startTimer();
                        }

                        @Override
                        public void onCreateFailure(String s) {}

                        @Override
                        public void onSetFailure(String s) {
                            Timber.e("Lỗi thiết lập Remote Description (ANSWER): %s", s);
                        }
                    }, remoteSdp);
                });
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi phân tích cú pháp SDP Answer");
        }
    };

    private final io.socket.emitter.Emitter.Listener onIceCandidateReceived = args -> {
        Timber.d("onIceCandidateReceived - Nhận được ICE Candidate đối phương");
        if (args == null || args.length == 0 || args[0] == null) return;
        try {
            JsonObject envelope = gson.fromJson(args[0].toString(), JsonObject.class);
            if (envelope.has("data")) {
                JsonObject data = envelope.getAsJsonObject("data");
                JsonObject candidateJson = data.getAsJsonObject("candidate");
                String sdp = candidateJson.get("candidate").getAsString();
                String sdpMid = candidateJson.get("sdpMid").getAsString();
                int sdpMLineIndex = candidateJson.get("sdpMLineIndex").getAsInt();

                IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    webRtcManager.addIceCandidate(candidate);
                });
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi phân tích cú pháp ICE Candidate");
        }
    };

    private final io.socket.emitter.Emitter.Listener onCallEnded = args -> {
        Timber.d("onCallEnded - Cuộc gọi kết thúc từ phía đối phương");
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            showToast("Cuộc gọi đã kết thúc");
            finish();
        });
    };

    private final io.socket.emitter.Emitter.Listener onCallRejected = args -> {
        Timber.d("onCallRejected - Cuộc gọi bị từ chối");
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            showToast("Cuộc gọi bị từ chối hoặc bận");
            finish();
        });
    };

    private final io.socket.emitter.Emitter.Listener onPeerLeft = args -> {
        Timber.d("onPeerLeft - Đối phương đã rời khỏi phòng đàm thoại");
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            showToast("Đối phương đã rời cuộc gọi");
            finish();
        });
    };

    private final io.socket.emitter.Emitter.Listener onCallError = args -> {
        Timber.e("onCallError - Lỗi từ Signaling Server");
        if (args != null && args.length > 0 && args[0] != null) {
            try {
                JsonObject envelope = gson.fromJson(args[0].toString(), JsonObject.class);
                if (envelope.has("message")) {
                    String message = envelope.get("message").getAsString();
                    runOnUiThread(() -> showToast("Lỗi cuộc gọi: " + message));
                }
            } catch (Exception e) {
                Timber.e(e, "Lỗi phân tích cú pháp error envelope");
            }
        }
        runOnUiThread(this::finish);
    };

    // ========== Điều khiển phần cứng và UI đàm thoại ==========

    private void toggleMute() {
        isMuted = !isMuted;
        webRtcManager.setLocalAudioEnabled(!isMuted);
        if (isMuted) {
            binding.btnMute.setImageResource(R.drawable.ic_mic_off);
            binding.btnMute.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            binding.btnMute.setImageResource(R.drawable.ic_mic);
            binding.btnMute.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3E4144")));
        }
    }

    private void toggleVideo() {
        isVideoDisabled = !isVideoDisabled;
        webRtcManager.setLocalVideoEnabled(!isVideoDisabled);
        if (isVideoDisabled) {
            binding.btnVideo.setImageResource(R.drawable.ic_video_off);
            binding.btnVideo.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            binding.cvLocalVideo.setVisibility(View.INVISIBLE);
        } else {
            binding.btnVideo.setImageResource(R.drawable.ic_video);
            binding.btnVideo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3E4144")));
            binding.cvLocalVideo.setVisibility(View.VISIBLE);
        }
    }

    private void startTimer() {
        if (timerHandler != null) return;

        timerHandler = new Handler(Looper.getMainLooper());
        secondsElapsed = 0;
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                secondsElapsed++;
                int mins = secondsElapsed / 60;
                int secs = secondsElapsed % 60;
                String timeString = String.format(java.util.Locale.getDefault(), "%02d:%02d", mins, secs);
                binding.tvTimer.setText(timeString);

                if (timerHandler != null) {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void onHangupAction() {
        Timber.d("Người dùng chủ động cúp máy");
        if (roomId != null) {
            JsonObject payload = new JsonObject();
            payload.addProperty("roomId", roomId);
            payload.addProperty("reason", "User hung up");
            socketIOManager.emit(SocketEvents.EMIT_ENDED, payload);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        // 1. Tắt Foreground Service
        Intent stopServiceIntent = new Intent(this, CallForegroundService.class);
        stopServiceIntent.setAction(CallForegroundService.ACTION_STOP_CALL);
        startService(stopServiceIntent);

        // 2. Tắt bộ đếm giây đàm thoại
        if (timerHandler != null) {
            timerHandler.removeCallbacksAndMessages(null);
            timerHandler = null;
        }

        // 3. Giải phóng bộ nhớ WebRTC theo thứ tự tuần tự tránh rò rỉ (Crucial Release Order)
        if (webRtcManager.getLocalVideoTrack() != null && binding.localVideoView != null) {
            webRtcManager.getLocalVideoTrack().removeSink(binding.localVideoView);
        }
        if (webRtcManager.getRemoteVideoTrack() != null && binding.remoteVideoView != null) {
            webRtcManager.getRemoteVideoTrack().removeSink(binding.remoteVideoView);
        }

        if (binding.localVideoView != null) {
            binding.localVideoView.release();
        }
        if (binding.remoteVideoView != null) {
            binding.remoteVideoView.release();
        }

        // Báo cho server rời phòng cuộc gọi để làm sạch phòng và báo cho client đối phương
        if (roomId != null) {
            JsonObject leavePayload = new JsonObject();
            leavePayload.addProperty("roomId", roomId);
            socketIOManager.emit(SocketEvents.EMIT_LEAVE_ROOM, leavePayload);
            Timber.d("Đã gửi socket leaveRoom cho roomId: %s", roomId);
        }

        // Hủy lắng nghe các sự kiện socket
        unregisterNegotiationSocketListeners();

        // Giải phóng luồng PeerConnection và Factory cục bộ
        webRtcManager.release();

        super.onDestroy();
    }
}
