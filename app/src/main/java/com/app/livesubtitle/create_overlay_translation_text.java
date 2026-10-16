package com.app.livesubtitle;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.Objects;

public class create_overlay_translation_text extends Service {

    private String TAG = "create_overlay_translation_text";

    public create_overlay_translation_text() {
    }

    @SuppressLint("StaticFieldLeak")
    public static GlobalOverlay mGlobalOverlay;

    @SuppressLint("StaticFieldLeak")
    public static View overlay_translation_text_container;

    @SuppressLint("StaticFieldLeak")
    public static EditText overlay_translation_text;

    @SuppressLint("StaticFieldLeak")
    public static View overlay_full_translation_text_container;

    @SuppressLint("StaticFieldLeak")
    public static EditText overlay_full_translation_text;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();

        Log.d(
                "create_overlay_translation_text",
                "Starting create_overlay_translation_text service"
        );

        create_translation_text_window();
    }

    @Override
    public void onDestroy() {
        if (mGlobalOverlay != null) {
            mGlobalOverlay.destroy();
            mGlobalOverlay = null;
        }

        overlay_translation_text_container = null;
        overlay_translation_text = null;
        overlay_full_translation_text_container = null;
        overlay_full_translation_text = null;

        super.onDestroy();
    }

    @SuppressLint("InflateParams")
    private void create_translation_text_window() {
        mGlobalOverlay = new GlobalOverlay(this);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //android.graphics.drawable.GradientDrawable subtitleBackground = new android.graphics.drawable.GradientDrawable();
        //subtitleBackground.setColor(Color.parseColor("#80000000"));
        //overlay_translation_text.setBackground(subtitleBackground);
        //overlay_full_translation_text.setBackground(subtitleBackground.getConstantState().newDrawable().mutate());

        overlay_translation_text_container = layoutInflater.inflate(R.layout.overlay_translation_text_container, null);
        overlay_translation_text_container.setMinimumWidth((int) (0.8 * DISPLAY_METRIC.DISPLAY_WIDTH));
        //overlay_translation_text.setBackgroundColor(Color.parseColor("#80000000"));
        //overlay_translation_text_container.setBackgroundColor(Color.TRANSPARENT);
        overlay_translation_text_container.setBackgroundColor(Color.parseColor("#80000000"));
        overlay_translation_text_container.setVisibility(View.INVISIBLE);
        overlay_translation_text = overlay_translation_text_container.findViewById(R.id.overlay_translation_text);
        overlay_translation_text.setWidth(overlay_translation_text_container.getWidth());
        overlay_translation_text.setBackgroundColor(Color.TRANSPARENT);
        //overlay_translation_text.setTextColor(Color.YELLOW);
        overlay_translation_text.setTextColor(Color.parseColor("#FFFF00"));
        overlay_translation_text.setHighlightColor(Color.TRANSPARENT);
        overlay_translation_text.setVisibility(View.INVISIBLE);
        //overlay_translation_text.setInputType(android.text.InputType.TYPE_NULL);

        overlay_full_translation_text_container = layoutInflater.inflate(R.layout.overlay_full_translation_text_container, null);
        overlay_full_translation_text_container.setMinimumWidth((int) (0.8 * DISPLAY_METRIC.DISPLAY_WIDTH));
        //overlay_full_translation_text.setBackgroundColor(Color.parseColor("#80000000"));
        //overlay_full_translation_text_container.setBackgroundColor(Color.TRANSPARENT);
        overlay_full_translation_text_container.setBackgroundColor(Color.parseColor("#80000000"));
        overlay_full_translation_text_container.setVisibility(View.INVISIBLE);
        overlay_full_translation_text = overlay_full_translation_text_container.findViewById(R.id.overlay_full_translation_text);
        overlay_full_translation_text.setWidth(overlay_full_translation_text_container.getWidth());
        overlay_full_translation_text.setBackgroundColor(Color.TRANSPARENT);
        //overlay_full_translation_text.setTextColor(Color.CYAN);
        overlay_full_translation_text.setTextColor(Color.parseColor("#FF00FFFF"));
        overlay_full_translation_text.setHighlightColor(Color.TRANSPARENT);
        overlay_full_translation_text.setVisibility(View.INVISIBLE);
        //overlay_full_translation_text.setInputType(android.text.InputType.TYPE_NULL);

        if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
            MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            if (TRANSLATION_TEXT.STRING.length() == 0) {
                if (overlay_translation_text_container != null) {
                    overlay_translation_text.setVisibility(View.INVISIBLE);
                    overlay_translation_text_container.setVisibility(View.INVISIBLE);
                }
            } else {
                if (create_overlay_translation_text.overlay_translation_text_container != null) {
                    overlay_translation_text.setVisibility(View.VISIBLE);
                    overlay_translation_text_container.setVisibility(View.VISIBLE);
                }
            }

        } else {
            //MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, MainActivity.mStreamVolume, 0);
            MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, MainActivity.mStreamVolume, 0);
            //MainActivity.audio.setStreamVolume(AudioManager.STREAM_MUSIC, MainActivity.mMusicVolume, 0);
            Log.d(TAG, "create_translation_text_window: mStreamVolume = " + MainActivity.mStreamVolume);
            //Log.d(TAG, "create_translation_text_window: mMusicVolume = " + MainActivity.mMusicVolume);
            if (create_overlay_translation_text.overlay_translation_text_container != null) {
                overlay_translation_text.setVisibility(View.INVISIBLE);
                overlay_translation_text_container.setVisibility(View.INVISIBLE);
                overlay_full_translation_text.setVisibility(View.INVISIBLE);
                overlay_full_translation_text_container.setVisibility(View.INVISIBLE);
            }
        }

        int h;
        if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh-Hans") || Objects.equals(LANGUAGE.DST, "zh-Hant")) {
            //h = 80;
            //h = 124;
            h = 75;
        } else {
            //h = 64;
            //h = 107;
            h = 62;
        }

        int overlayWidth = (int) (0.85 * DISPLAY_METRIC.DISPLAY_WIDTH);
        int overlayHeight = (int) (2 * h * getResources().getDisplayMetrics().density);
        int translationY = (int) (0.3 * DISPLAY_METRIC.DISPLAY_HEIGHT);
        int fullTranslationY = translationY - overlayHeight;

        mGlobalOverlay.addOverlayView(
                overlay_full_translation_text_container,
                overlayWidth,
                overlayHeight,
                0,
                fullTranslationY,
                null,
                null,
                new GlobalOverlay.OnRemoveOverlayListener() {
                    @Override public void onRemoveOverlay(View v, boolean isRemovedByUser) {
                        //stopSelf();
                    }
                }
        );
        mGlobalOverlay.addOverlayView(
                overlay_translation_text_container,
                overlayWidth,
                overlayHeight,
                0,
                translationY-4,
                null,
                null,
                new GlobalOverlay.OnRemoveOverlayListener() {
                    @Override public void onRemoveOverlay(View v, boolean isRemovedByUser) {
                        //stopSelf();
                    }
                }
        );

    }
}
