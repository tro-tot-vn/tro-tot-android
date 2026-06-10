package com.trototvn.trototandroid.ui.videocall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.ActivityIncomingCallBinding;
import com.trototvn.trototandroid.ui.base.BaseActivity;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketEvents;
import com.trototvn.trototandroid.utils.SocketIOManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * Giao diện cuộc gọi đến (Incoming Call Screen).
 * Quản lý nhạc chuông, bộ rung, đếm ngược tự động từ chối và bắn tín hiệu Socket.
 */
@AndroidEntryPoint
public class IncomingCallActivity extends BaseActivity<ActivityIncomingCallBinding> {

    public static final String ACTION_VIDEO_CALL_CANCELLED = "com.trototvn.trototandroid.ACTION_VIDEO_CALL_CANCELLED";

    @Inject
    SocketIOManager socketIOManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    com.trototvn.trototandroid.data.repository.ChatRepository chatRepository;

    private final ActivityResultLauncher<String[]> requestAcceptPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cameraGranted = result.get(android.Manifest.permission.CAMERA);
                Boolean recordAudioGranted = result.get(android.Manifest.permission.RECORD_AUDIO);
                if (cameraGranted != null && cameraGranted && recordAudioGranted != null && recordAudioGranted) {
                    acceptCall();
                } else {
                    Timber.w("IncomingCallActivity - Quyền CAMERA hoặc RECORD_AUDIO bị từ chối khi chấp nhận cuộc gọi");
                    showToast("Bạn cần cấp quyền Camera và Micro để trả lời cuộc gọi");
                    onDeclineAction();
                }
            });

    private String roomId;
    private String callerId;
    private String callerName;
    private String callerAvatar;

    private Ringtone ringtone;
    private Vibrator vibrator;

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = this::onDeclineAction;

    private final BroadcastReceiver callCancelledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cancelledRoomId = intent.getStringExtra("roomId");
            if (roomId != null && roomId.equals(cancelledRoomId)) {
                Timber.d("IncomingCallActivity - Nhận broadcast hủy cuộc gọi cho phòng: %s", cancelledRoomId);
                stopRingtoneAndVibrator();
                timeoutHandler.removeCallbacks(timeoutRunnable);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Cấu hình đánh thức màn hình khóa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        roomId = getIntent().getStringExtra("roomId");
        callerId = getIntent().getStringExtra("callerId");
        callerName = getIntent().getStringExtra("callerName");
        callerAvatar = getIntent().getStringExtra("callerAvatar");

        if (roomId == null || roomId.trim().isEmpty()) {
            Timber.e("IncomingCallActivity - roomId is null or empty, finishing activity immediately");
            finish();
            return;
        }

        chatRepository.setIncomingCallRinging(true);

        // Đảm bảo Socket đã kết nối khi Activity được mở trực tiếp từ FCM push khi app bị đóng (killed state)
        String userId = sessionManager.getUserId();
        if (userId != null) {
            socketIOManager.connect(userId);
        }

        Timber.d("IncomingCallActivity bắt đầu cho phòng: %s, người gọi: %s", roomId, callerName);

        // Đăng ký BroadcastReceiver lắng nghe tín hiệu hủy cuộc gọi từ FCM
        IntentFilter filter = new IntentFilter(ACTION_VIDEO_CALL_CANCELLED);
        ContextCompat.registerReceiver(
                this,
                callCancelledReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        // Bắt đầu đếm ngược 30 giây để tự động từ chối
        timeoutHandler.postDelayed(timeoutRunnable, 30000);
    }

    @Override
    protected void setupViews() {
        binding.tvName.setText(callerName != null ? callerName : getString(R.string.notification_title_default));

        // Tải ảnh đại diện bằng Glide (Performance crop)
        if (callerAvatar != null && !callerAvatar.isEmpty()) {
            Glide.with(this)
                    .load(callerAvatar)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        // Sự kiện các nút bấm
        binding.btnAccept.setOnClickListener(v -> onAcceptAction());
        binding.btnDecline.setOnClickListener(v -> onDeclineAction());

        // Phát nhạc chuông và chế độ rung đàm thoại
        startRingtoneAndVibrator();
    }

    @Override
    protected void observeData() {
    }

    private void startRingtoneAndVibrator() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                }
                ringtone.play();
            }

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 1000};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Lỗi khi phát nhạc chuông hoặc rung");
        }
    }

    private void stopRingtoneAndVibrator() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            ringtone = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    private void onAcceptAction() {
        Timber.d("Chấp nhận cuộc gọi cho phòng: %s", roomId);

        // 1. Kiểm tra kết nối Socket
        if (!socketIOManager.isConnected()) {
            showToast("Không có kết nối đến máy chủ cuộc gọi. Vui lòng đợi trong giây lát...");
            String userId = sessionManager.getUserId();
            if (userId != null) {
                socketIOManager.connect(userId);
            }
            return;
        }

        // 2. Kiểm tra quyền Camera & Micro trước khi kết nối
        String[] permissions = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
        };

        boolean hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean hasAudio = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (hasCamera && hasAudio) {
            acceptCall();
        } else {
            requestAcceptPermissionsLauncher.launch(permissions);
        }
    }

    private void acceptCall() {
        stopRingtoneAndVibrator();
        timeoutHandler.removeCallbacks(timeoutRunnable);

        if (roomId != null) {
            JsonObject payload = new JsonObject();
            payload.addProperty("roomId", roomId);
            socketIOManager.emit(SocketEvents.EMIT_ACCEPTED, payload);
        }

        // Định tuyến sang VideoCallActivity
        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("partnerId", callerId);
        intent.putExtra("partnerName", callerName);
        intent.putExtra("isCaller", false);
        startActivity(intent);

        finish();
    }

    private void onDeclineAction() {
        Timber.d("Từ chối cuộc gọi cho phòng: %s", roomId);
        stopRingtoneAndVibrator();
        timeoutHandler.removeCallbacks(timeoutRunnable);

        if (roomId != null && socketIOManager.isConnected()) {
            JsonObject payload = new JsonObject();
            payload.addProperty("roomId", roomId);
            payload.addProperty("reason", "Declined by user");
            socketIOManager.emit(SocketEvents.EMIT_REJECTED, payload);
        } else if (roomId != null) {
            Timber.w("onDeclineAction: Socket not connected, could not emit reject event to server");
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtoneAndVibrator();
        timeoutHandler.removeCallbacks(timeoutRunnable);
        chatRepository.setIncomingCallRinging(false); // Reset trạng thái đổ chuông
        try {
            unregisterReceiver(callCancelledReceiver);
        } catch (Exception e) {
            Timber.e(e, "Lỗi unregister callCancelledReceiver trong IncomingCallActivity");
        }
    }
}
