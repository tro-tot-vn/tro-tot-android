package com.trototvn.trototandroid.webrtc;

import android.content.Context;

import com.google.gson.Gson;
import com.trototvn.trototandroid.data.model.video.IceConfigDto;
import com.trototvn.trototandroid.data.model.video.IceServerDto;
import com.trototvn.trototandroid.utils.SocketEvents;
import com.trototvn.trototandroid.utils.SocketIOManager;

import org.webrtc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import timber.log.Timber;

/**
 * WebRtcManager - Lớp quản lý vòng đời PeerConnection, Media Tracks và Signaling thời gian thực
 */
@Singleton
public class WebRtcManager {

    private final Context context;
    private final SocketIOManager socketIOManager;
    private final Gson gson;
    private final CallAudioManager callAudioManager;

    private EglBase eglBase;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private VideoCapturer videoCapturer;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private VideoTrack remoteVideoTrack;

    private String roomId;
    private ScheduledExecutorService statsExecutor;

    private final PublishSubject<com.google.gson.JsonObject> iceStateChangedSubject = PublishSubject.create();
    private final PublishSubject<com.google.gson.JsonObject> peerStatsSubject = PublishSubject.create();
    private final PublishSubject<String> errorSubject = PublishSubject.create();

    // Callbacks cho UI đính kèm Renderer
    public interface RemoteVideoTrackListener {
        void onRemoteVideoTrackAdded(VideoTrack track);
    }

    public interface IceCandidateListener {
        void onIceCandidateGenerated(IceCandidate candidate);
    }

    private RemoteVideoTrackListener remoteVideoTrackListener;
    private IceCandidateListener iceCandidateListener;

    @Inject
    public WebRtcManager(
            @ApplicationContext Context context,
            SocketIOManager socketIOManager,
            Gson gson,
            CallAudioManager callAudioManager) {
        this.context = context;
        this.socketIOManager = socketIOManager;
        this.gson = gson;
        this.callAudioManager = callAudioManager;
    }

    public void setRemoteVideoTrackListener(RemoteVideoTrackListener listener) {
        this.remoteVideoTrackListener = listener;
        if (remoteVideoTrack != null && listener != null) {
            listener.onRemoteVideoTrackAdded(remoteVideoTrack);
        }
    }

    public void setIceCandidateListener(IceCandidateListener listener) {
        this.iceCandidateListener = listener;
    }

    public EglBase.Context getEglContext() {
        if (eglBase == null) {
            eglBase = EglBase.create();
        }
        return eglBase.getEglBaseContext();
    }

    /**
     * Khởi tạo WebRTC engine cho phòng gọi
     */
    public void init(IceConfigDto iceConfig, String roomId) {
        this.roomId = roomId;
        Timber.d("Khởi tạo WebRtcManager cho phòng: %s", roomId);

        // Bật định tuyến âm thanh
        callAudioManager.start();

        // 1. Tạo EglBase và Khởi tạo PeerConnectionFactory
        if (eglBase == null) {
            eglBase = EglBase.create();
        }

        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        PeerConnectionFactory.Options factoryOptions = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(
                eglBase.getEglBaseContext(), true, true);
        DefaultVideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(
                eglBase.getEglBaseContext());

        factory = PeerConnectionFactory.builder()
                .setOptions(factoryOptions)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        // 2. Tạo Camera VideoCapturer, Local Video & Audio Source/Track
        videoCapturer = createVideoCapturer(context);
        if (videoCapturer != null) {
            SurfaceTextureHelper textureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            VideoSource videoSource = factory.createVideoSource(videoCapturer.isScreencast());
            videoCapturer.initialize(textureHelper, context, videoSource.getCapturerObserver());
            videoCapturer.startCapture(1280, 720, 30);

            localVideoTrack = factory.createVideoTrack("ARDMSv0", videoSource);
            localVideoTrack.setEnabled(true);
        } else {
            Timber.w("Không thể khởi tạo VideoCapturer (có thể do thiếu quyền/thiết bị không hỗ trợ camera)");
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        localAudioTrack = factory.createAudioTrack("ARDAMs1", audioSource);
        localAudioTrack.setEnabled(true);

        // 3. Phân tích ICE Servers từ API trả về và Cấu hình PeerConnection
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        if (iceConfig != null && iceConfig.iceServers != null) {
            for (IceServerDto dto : iceConfig.iceServers) {
                PeerConnection.IceServer.Builder builder = PeerConnection.IceServer.builder(dto.urls);
                if (dto.username != null) {
                    builder.setUsername(dto.username);
                }
                if (dto.credential != null) {
                    builder.setPassword(dto.credential);
                }
                iceServers.add(builder.createIceServer());
            }
        }

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;

        peerConnection = factory.createPeerConnection(rtcConfig, peerConnectionObserver);

        // 4. Đính kèm Media Tracks vào PeerConnection
        if (peerConnection != null) {
            if (localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack, List.of("ARDMSs0"));
            }
            if (localAudioTrack != null) {
                peerConnection.addTrack(localAudioTrack, List.of("ARDMSs0"));
            }
        }

        // Đăng ký các sự kiện Socket phản hồi mạng và bắt đầu đếm giây gửi stats
        registerSocketListeners();
        startStatsTimer();
    }

    private VideoCapturer createVideoCapturer(Context context) {
        CameraEnumerator enumerator;
        if (Camera2Enumerator.isSupported(context)) {
            enumerator = new Camera2Enumerator(context);
        } else {
            enumerator = new Camera1Enumerator(true);
        }
        final String[] deviceNames = enumerator.getDeviceNames();

        // 1. Ưu tiên Camera trước
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }

        // 2. Fallback sang Camera sau nếu không có camera trước
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        return null;
    }

    /**
     * Đồng bộ trạng thái ICE cục bộ lên Server
     */
    private void sendIceStateChange() {
        if (peerConnection == null || roomId == null) return;

        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("roomId", roomId);
        payload.addProperty("iceConnectionState", peerConnection.iceConnectionState().name());
        payload.addProperty("iceGatheringState", peerConnection.iceGatheringState().name());

        socketIOManager.emit(SocketEvents.EMIT_ICE_STATE_CHANGE, payload);
        Timber.d("Đã gửi sự kiện iceStateChange: %s", payload);
    }

    private void startStatsTimer() {
        stopStatsTimer();
        statsExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        statsExecutor.scheduleAtFixedRate(this::getAndSendStats, 5, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void stopStatsTimer() {
        if (statsExecutor != null) {
            statsExecutor.shutdown();
            statsExecutor = null;
        }
    }

    /**
     * Đọc thống kê WebRTC Peer Connection và gửi lên Server định kỳ
     */
    private void getAndSendStats() {
        if (peerConnection == null || roomId == null) return;

        peerConnection.getStats(report -> {
            com.google.gson.JsonObject statsJson = new com.google.gson.JsonObject();
            for (org.webrtc.RTCStats stats : report.getStatsMap().values()) {
                com.google.gson.JsonObject statsObj = new com.google.gson.JsonObject();
                statsObj.addProperty("id", stats.getId());
                statsObj.addProperty("type", stats.getType());
                statsObj.addProperty("timestamp", stats.getTimestampUs());
                for (java.util.Map.Entry<String, Object> entry : stats.getMembers().entrySet()) {
                    Object val = entry.getValue();
                    if (val instanceof Number) {
                        statsObj.addProperty(entry.getKey(), (Number) val);
                    } else if (val instanceof Boolean) {
                        statsObj.addProperty(entry.getKey(), (Boolean) val);
                    } else if (val != null) {
                        statsObj.addProperty(entry.getKey(), val.toString());
                    }
                }
                statsJson.add(stats.getId(), statsObj);
            }

            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("roomId", roomId);
            payload.add("stats", statsJson);

            socketIOManager.emit(SocketEvents.EMIT_CONNECTION_STATS, payload);
        });
    }

    // Đăng ký/Hủy lắng nghe luồng Socket trạng thái mạng thời gian thực
    private void registerSocketListeners() {
        socketIOManager.on(SocketEvents.LISTEN_ICE_STATE_CHANGED, this::onIceStateChanged);
        socketIOManager.on(SocketEvents.LISTEN_PEER_STATS, this::onPeerStats);
        socketIOManager.on(SocketEvents.LISTEN_ERROR, this::onSocketError);
    }

    private void unregisterSocketListeners() {
        socketIOManager.off(SocketEvents.LISTEN_ICE_STATE_CHANGED);
        socketIOManager.off(SocketEvents.LISTEN_PEER_STATS);
        socketIOManager.off(SocketEvents.LISTEN_ERROR);
    }

    private void onIceStateChanged(Object[] args) {
        Timber.d("Lắng nghe onIceStateChanged: %s", args[0]);
        try {
            com.google.gson.JsonObject envelope = gson.fromJson(args[0].toString(), com.google.gson.JsonObject.class);
            if (envelope.has("data")) {
                iceStateChangedSubject.onNext(envelope.getAsJsonObject("data"));
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi parse dữ liệu iceStateChanged");
        }
    }

    private void onPeerStats(Object[] args) {
        Timber.d("Lắng nghe onPeerStats: %s", args[0]);
        try {
            com.google.gson.JsonObject envelope = gson.fromJson(args[0].toString(), com.google.gson.JsonObject.class);
            if (envelope.has("data")) {
                peerStatsSubject.onNext(envelope.getAsJsonObject("data"));
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi parse dữ liệu peerStats");
        }
    }

    private void onSocketError(Object[] args) {
        Timber.e("Lắng nghe onSocketError: %s", args[0]);
        try {
            com.google.gson.JsonObject envelope = gson.fromJson(args[0].toString(), com.google.gson.JsonObject.class);
            String errorMsg = "Lỗi kết nối đàm thoại";
            if (envelope.has("message")) {
                errorMsg = envelope.get("message").getAsString();
            }
            errorSubject.onNext(errorMsg);
        } catch (Exception e) {
            Timber.e(e, "Lỗi parse dữ liệu error event");
            errorSubject.onNext(args[0].toString());
        }
    }

    // Luồng dữ liệu Reactive dành cho ViewModels
    public Observable<com.google.gson.JsonObject> getIceStateChanged() {
        return iceStateChangedSubject;
    }

    public Observable<com.google.gson.JsonObject> getPeerStats() {
        return peerStatsSubject;
    }

    public Observable<String> getError() {
        return errorSubject;
    }

    public PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public VideoTrack getLocalVideoTrack() {
        return localVideoTrack;
    }

    public VideoTrack getRemoteVideoTrack() {
        return remoteVideoTrack;
    }

    private final PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Timber.d("Thay đổi trạng thái kết nối ICE: %s", iceConnectionState);
            sendIceStateChange();
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {}

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Timber.d("Thay đổi trạng thái gom candidates ICE: %s", iceGatheringState);
            sendIceStateChange();
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Timber.d("Tạo thành công ICE Candidate cục bộ: %s", iceCandidate);
            if (iceCandidateListener != null) {
                iceCandidateListener.onIceCandidateGenerated(iceCandidate);
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Timber.d("onAddStream (Legacy stream listener): %s", mediaStream);
            if (mediaStream.videoTracks.size() > 0) {
                remoteVideoTrack = mediaStream.videoTracks.get(0);
                if (remoteVideoTrackListener != null) {
                    remoteVideoTrackListener.onRemoteVideoTrackAdded(remoteVideoTrack);
                }
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {}

        @Override
        public void onDataChannel(DataChannel dataChannel) {}

        @Override
        public void onRenegotiationNeeded() {}

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            MediaStreamTrack mediaStreamTrack = rtpReceiver.track();
            if (mediaStreamTrack != null) {
                Timber.d("onAddTrack (Unified plan track listener): %s", mediaStreamTrack.kind());
                if (mediaStreamTrack instanceof VideoTrack) {
                    remoteVideoTrack = (VideoTrack) mediaStreamTrack;
                    if (remoteVideoTrackListener != null) {
                        remoteVideoTrackListener.onRemoteVideoTrackAdded(remoteVideoTrack);
                    }
                }
            } else {
                Timber.w("onAddTrack: received RtpReceiver has no track");
            }
        }
    };

    /**
     * Giải phóng toàn bộ tài nguyên WebRTC tránh rò rỉ bộ nhớ
     */
    public void release() {
        Timber.d("Bắt đầu giải phóng tài nguyên WebRtcManager");
        stopStatsTimer();
        unregisterSocketListeners();
        callAudioManager.stop();

        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Timber.e(e, "Lỗi dừng capture hình ảnh");
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        localVideoTrack = null;
        localAudioTrack = null;
        remoteVideoTrack = null;
        roomId = null;

        if (factory != null) {
            factory.dispose();
            factory = null;
        }

        if (eglBase != null) {
            eglBase.release();
            eglBase = null;
        }
    }
}
