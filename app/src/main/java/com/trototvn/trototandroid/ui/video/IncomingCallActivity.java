package com.trototvn.trototandroid.ui.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import timber.log.Timber;

/**
 * Giao diện cuộc gọi đến (Skeleton).
 * Sẽ được nâng cấp thiết kế đầy đủ ở Giai đoạn 4.
 */
public class IncomingCallActivity extends AppCompatActivity {

    public static final String ACTION_VIDEO_CALL_CANCELLED = "com.trototvn.trototandroid.ACTION_VIDEO_CALL_CANCELLED";

    private String roomId;

    private final BroadcastReceiver callCancelledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cancelledRoomId = intent.getStringExtra("roomId");
            if (roomId != null && roomId.equals(cancelledRoomId)) {
                Timber.d("IncomingCallActivity - Nhận broadcast hủy cuộc gọi cho phòng: %s", cancelledRoomId);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình đánh thức màn hình và hiển thị trên màn hình khóa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        roomId = getIntent().getStringExtra("roomId");
        Timber.d("IncomingCallActivity bắt đầu cho phòng: %s", roomId);

        // Đăng ký BroadcastReceiver để lắng nghe sự kiện cúp máy từ xa
        IntentFilter filter = new IntentFilter(ACTION_VIDEO_CALL_CANCELLED);
        ContextCompat.registerReceiver(
                this,
                callCancelledReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(callCancelledReceiver);
        } catch (Exception e) {
            Timber.e(e, "Lỗi unregister callCancelledReceiver");
        }
    }
}
