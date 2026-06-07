package com.trototvn.trototandroid.webrtc;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

/**
 * Quản lý định tuyến âm thanh (Loa ngoài, Loa trong) và Audio Focus cho cuộc gọi.
 */
@Singleton
public class CallAudioManager {

    private final AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean isSpeakerPhoneOn = false;

    @Inject
    public CallAudioManager(@ApplicationContext Context context) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Bắt đầu cấu hình âm thanh cho cuộc gọi (yêu cầu Audio Focus, MODE_IN_COMMUNICATION)
     */
    public void start() {
        Timber.d("Bắt đầu CallAudioManager");
        requestAudioFocus();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        // Mặc định bật loa ngoài đối với cuộc gọi video
        setSpeakerphoneOn(true);
    }

    /**
     * Dọn dẹp âm thanh sau khi cuộc gọi kết thúc
     */
    public void stop() {
        Timber.d("Dừng CallAudioManager");
        setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        abandonAudioFocus();
    }

    /**
     * Bật/tắt loa ngoài
     */
    public void setSpeakerphoneOn(boolean on) {
        isSpeakerPhoneOn = on;
        audioManager.setSpeakerphoneOn(on);
        Timber.d("Trạng thái Loa ngoài đặt thành: %b", on);
    }

    public boolean isSpeakerphoneOn() {
        return isSpeakerPhoneOn;
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(focusChange -> Timber.d("Thay đổi AudioFocus: %d", focusChange))
                    .build();

            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(
                    focusChange -> Timber.d("Thay đổi AudioFocus: %d", focusChange),
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            );
        }
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                audioFocusRequest = null;
            }
        } else {
            audioManager.abandonAudioFocus(focusChange -> {});
        }
    }
}
