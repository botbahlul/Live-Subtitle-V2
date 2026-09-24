package com.app.livesubtitle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CheckBox checkbox_debug_mode;
    private Spinner spinner_src_languages;
    private TextView textview_src_dialect;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_src;
    private Spinner spinner_dst_languages;
    private TextView textview_dst_dialect;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_dst;
    @SuppressLint("StaticFieldLeak")
    public static CheckBox checkbox_offline_mode;
    @SuppressLint("StaticFieldLeak")
    public static EditText voice_text;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_recognizing;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_overlaying;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_debug;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_output_messages;

    private ArrayList<String> arraylist_languages;
    private String [] countries;
    private String [] dialects;
    private Map<String, String> countries_dialects;
    private DisplayMetrics display;
    @SuppressLint("StaticFieldLeak")
    public static AudioManager audio;
    public static int mStreamVolume;

    //DON'T FORGET TO MODIFY AndroidManifest.xml
    //         <activity
    //            android:name=".MainActivity"
    //            android:configChanges="keyboardHidden|screenSize|orientation|screenLayout|navigation"


    @SuppressLint({"ClickableViewAccessibility", "QueryPermissionsNeeded", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkbox_debug_mode = findViewById(R.id.checkbox_debug_mode);
        spinner_src_languages = findViewById(R.id.spinner_src_languages);
        checkbox_offline_mode = findViewById(R.id.checkbox_offline_mode);
        spinner_dst_languages = findViewById(R.id.spinner_dst_languages);
        Button button_toggle_overlay = findViewById(R.id.button_toggle_overlay);
        textview_src_dialect = findViewById(R.id.textview_src_dialect);
        textview_src = findViewById(R.id.textview_src);
        textview_dst_dialect = findViewById(R.id.textview_dst_dialect);
        textview_dst = findViewById(R.id.textview_dst);
        voice_text = findViewById(R.id.voice_text);
        textview_recognizing = findViewById(R.id.textview_recognizing);
        textview_overlaying = findViewById(R.id.textview_overlaying);
        textview_debug = findViewById(R.id.textview_debug);
        textview_output_messages = findViewById(R.id.textview_output_messages);

        VOICE_TEXT.STRING = "";
        TRANSLATION_TEXT.STRING = "";
        PREFER_OFFLINE_STATUS.OFFLINE = checkbox_offline_mode.isChecked();

        audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mStreamVolume = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        //setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //audio.setSpeakerphoneOn(true);

        display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        float d = display.density;
        DISPLAY_METRIC.DISPLAY_WIDTH = display.widthPixels;
        DISPLAY_METRIC.DISPLAY_HEIGHT = display.heightPixels;
        DISPLAY_METRIC.DISPLAY_DENSITY = d;

        RECOGNIZING_STATUS.IS_RECOGNIZING = false;
        OVERLAYING_STATUS.IS_OVERLAYING = false;
        RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
        setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
        OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
        textview_overlaying.setText(OVERLAYING_STATUS.STRING);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        int h;
        if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh-CN") || Objects.equals(LANGUAGE.DST, "zh-TW")) {
            h = 75;
        }
        else {
            h = 62;
        }
        voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        }

        checkbox_debug_mode.setOnClickListener(view -> {
            if(((CompoundButton) view).isChecked()){
                textview_src_dialect.setVisibility(View.VISIBLE);
                textview_src.setVisibility(View.VISIBLE);
                textview_dst_dialect.setVisibility(View.VISIBLE);
                textview_dst.setVisibility(View.VISIBLE);
                textview_recognizing.setVisibility(View.VISIBLE);
                textview_overlaying.setVisibility(View.VISIBLE);
                textview_debug.setVisibility(View.VISIBLE);
                if (LANGUAGE.SRC_DIALECT != null) {
                    String lsd = "LANGUAGE.SRC_DIALECT = " + LANGUAGE.SRC_DIALECT;
                    textview_src_dialect.setText(lsd);
                }
                else {
                    textview_src_dialect.setHint("LANGUAGE.SRC_DIALECT");
                }

                if (LANGUAGE.SRC != null) {
                    String ls = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                    textview_src.setText(ls);
                }
                else {
                    textview_src.setHint("LANGUAGE.SRC");
                }

                if (LANGUAGE.DST_DIALECT != null) {
                    String ldd = "LANGUAGE.DST_DIALECT = " + LANGUAGE.DST_DIALECT;
                    textview_dst_dialect.setText(ldd);
                }
                else {
                    textview_dst_dialect.setHint("LANGUAGE.DST_DIALECT");
                }

                if (LANGUAGE.DST != null) {
                    String ld = "LANGUAGE.DST = " + LANGUAGE.DST;
                    textview_dst.setText(ld);
                }
                else {
                    textview_src.setHint("LANGUAGE.SRC");
                }
            }
            else {
                textview_src_dialect.setVisibility(View.GONE);
                textview_src.setVisibility(View.GONE);
                textview_dst_dialect.setVisibility(View.GONE);
                textview_dst.setVisibility(View.GONE);
                textview_recognizing.setVisibility(View.GONE);
                textview_overlaying.setVisibility(View.GONE);
                textview_debug.setVisibility(View.GONE);
            }
        });

        if(checkbox_debug_mode.isChecked()){
            textview_src_dialect.setVisibility(View.VISIBLE);
            textview_src.setVisibility(View.VISIBLE);
            textview_dst_dialect.setVisibility(View.VISIBLE);
            textview_dst.setVisibility(View.VISIBLE);
            textview_recognizing.setVisibility(View.VISIBLE);
            textview_overlaying.setVisibility(View.VISIBLE);
            textview_debug.setVisibility(View.VISIBLE);
            if (LANGUAGE.SRC_DIALECT != null) {
                String lsd = "LANGUAGE.SRC_DIALECT = " + LANGUAGE.SRC_DIALECT;
                textview_src_dialect.setText(lsd);
            }
            else {
                textview_src_dialect.setHint("LANGUAGE.SRC_DIALECT");
            }

            if (LANGUAGE.SRC != null) {
                String ls  = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                textview_src.setText(ls);
            }
            else {
                textview_src.setHint("LANGUAGE.SRC");
            }

            if (LANGUAGE.DST_DIALECT != null) {
                String ldd = "LANGUAGE.DST_DIALECT = " + LANGUAGE.DST_DIALECT;
                textview_dst_dialect.setText(ldd);
            }
            else {
                textview_dst_dialect.setHint("LANGUAGE.DST_DIALECT");
            }

            if (LANGUAGE.DST != null) {
                String ld = "LANGUAGE.DST = " + LANGUAGE.DST;
                textview_dst.setText(ld);
            }
            else {
                textview_src.setHint("LANGUAGE.SRC");
            }
        }

        else {
            textview_src_dialect.setVisibility(View.GONE);
            textview_src.setVisibility(View.GONE);
            textview_dst_dialect.setVisibility(View.GONE);
            textview_dst.setVisibility(View.GONE);
            textview_recognizing.setVisibility(View.GONE);
            textview_overlaying.setVisibility(View.GONE);
            textview_debug.setVisibility(View.GONE);
        }

        /*
        final Intent ri = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        PackageManager pm = getPackageManager();
        boolean isInstalled = isPackageInstalled("com.google.android.googlequicksearchbox", pm);
        if (!isInstalled) Toast.makeText(this,"Please install Googple app (com.google.android.googlequicksearchbox)",Toast.LENGTH_SHORT).show();
        if (isInstalled) ri.setPackage("com.google.android.googlequicksearchbox");
        this.sendOrderedBroadcast(ri,null,new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final Bundle extra = getResultExtras(false);
                        if (getResultCode() == Activity.RESULT_OK && extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                            arraylist_languages = extra.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
                            dialects = arraylist_languages.toArray(new String[0]);
                            for (int i = 0; i < dialects.length; i++) {
                                dialects[i] = dialects[i].trim();
                            }
                            if (arraylist_languages != null) {
                                for (int i = 0; i < arraylist_languages.size(); i++) {
                                    Locale locale = Locale.forLanguageTag(arraylist_languages.get(i));
                                    arraylist_languages.set(i, locale.getDisplayName().trim());
                                }
                                countries = arraylist_languages.toArray(new String[0]);
                                for (int i = 0; i < countries.length; i++) {
                                    countries[i] = countries[i].trim();
                                }
                                setup_spinner(arraylist_languages);
                            }
                        }
                    }
                },
                null,
                Activity.RESULT_OK,
                null,
                null
        );
        */

        final String GOOGLE_SEARCH_PACKAGE = "com.google.android.googlequicksearchbox";
        final Intent ri = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        PackageManager pm = getPackageManager();
        boolean isInstalled = isPackageInstalled(GOOGLE_SEARCH_PACKAGE, pm);

        if (!isInstalled) {
            Toast.makeText(this, "Please install Google app", Toast.LENGTH_SHORT).show();
        } else {
            ri.setPackage(GOOGLE_SEARCH_PACKAGE);
        }

        // Add background flag so it won't stuck in new android
        ri.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        this.sendOrderedBroadcast(ri, null, new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle extra = getResultExtras(false);
                        if (extra == null || !extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                            extra = intent.getExtras();
                        }

                        // CONDITION 1: RUNNING ON VERSION 11.XX.XX (Google App gives language list)
                        Log.d("MainActivity", "arraylist_languages = " + extra.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES));
                        if (extra != null && extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                            arraylist_languages = extra.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);

                            if (arraylist_languages != null && !arraylist_languages.isEmpty()) {
                                dialects = arraylist_languages.toArray(new String[0]);
                                for (int i = 0; i < dialects.length; i++) {
                                    dialects[i] = dialects[i].trim();
                                }

                                ArrayList<String> displayLanguages = new ArrayList<>();
                                for (int i = 0; i < arraylist_languages.size(); i++) {
                                    Locale locale = Locale.forLanguageTag(arraylist_languages.get(i));
                                    displayLanguages.add(locale.getDisplayName().trim());
                                }

                                countries = displayLanguages.toArray(new String[0]);
                                for (int i = 0; i < countries.length; i++) {
                                    countries[i] = countries[i].trim();
                                }

                                setup_spinner(displayLanguages);

                            } else {
                                loadLocaleLanguages();
                            }

                        } else {
                            // CONDITIONS 2: RUNNING ON VERSION 17.XX.XX (Broadcast was blocked by Google, taking from system)
                            loadLocaleLanguages();
                        }
                    }
                },
                null,
                Activity.RESULT_OK,
                null,
                null
        );

        spinner_src_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String src_country = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC_DIALECT = countries_dialects.get(src_country);
                if (LANGUAGE.SRC_DIALECT != null) {
                    LANGUAGE.SRC = LANGUAGE.SRC_DIALECT.split("-")[0];
                    switch (LANGUAGE.SRC_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.SRC = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.SRC = "zh-Hans";
                            break;
                    }
                }

                setText(textview_src_dialect, LANGUAGE.SRC_DIALECT);
                setText(textview_src, LANGUAGE.SRC);

                String dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST_DIALECT = countries_dialects.get(dst_country);
                if (LANGUAGE.DST_DIALECT != null) {
                    LANGUAGE.DST = LANGUAGE.DST_DIALECT.split("-")[0];
                    switch (LANGUAGE.DST_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.DST = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.DST = "zh-Hans";
                            break;
                    }
                }

                setText(textview_dst_dialect, LANGUAGE.DST_DIALECT);
                setText(textview_dst, LANGUAGE.DST);

                int h;
                if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh-Hans") || Objects.equals(LANGUAGE.DST, "zh-Hant")) {
                    h = 75;
                }
                else {
                    h = 62;
                }
                voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

                stop_voice_recognizer();
                stop_create_overlay_translation_text();
                stop_create_overlay_mic_button();

                if (OVERLAYING_STATUS.IS_OVERLAYING) {
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_off);
                    } else {
                        start_voice_recognizer();
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_on);
                    }
                    start_create_overlay_mic_button();
                    if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setBackgroundColor(Color.parseColor("#80000000"));

                    start_create_overlay_translation_text();
                }

                RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
                setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                OVERLAYING_STATUS.STRING =  "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                textview_overlaying.setText(OVERLAYING_STATUS.STRING);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                String src_country = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC_DIALECT = countries_dialects.get(src_country);
                if (LANGUAGE.SRC_DIALECT != null) {
                    LANGUAGE.SRC = LANGUAGE.SRC_DIALECT.split("-")[0];
                    switch (LANGUAGE.SRC_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.SRC = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.SRC = "zh-Hans";
                            break;
                    }
                }
                setText(textview_src_dialect, LANGUAGE.SRC_DIALECT);
                setText(textview_src, LANGUAGE.SRC);

                String dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST_DIALECT = countries_dialects.get(dst_country);
                if (LANGUAGE.DST_DIALECT != null) {
                    LANGUAGE.DST = LANGUAGE.DST_DIALECT.split("-")[0];
                    switch (LANGUAGE.DST_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.DST = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.DST = "zh-Hans";
                            break;
                    }
                }
                setText(textview_dst_dialect, LANGUAGE.DST_DIALECT);
                setText(textview_dst, LANGUAGE.DST);
            }
        });

        spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String src_country = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC_DIALECT = countries_dialects.get(src_country);
                if (LANGUAGE.SRC_DIALECT != null) {
                    LANGUAGE.SRC = LANGUAGE.SRC_DIALECT.split("-")[0];
                    switch (LANGUAGE.SRC_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.SRC = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.SRC = "zh-Hans";
                            break;
                    }
                }
                setText(textview_src_dialect, LANGUAGE.SRC_DIALECT);
                setText(textview_src, LANGUAGE.SRC);

                String dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST_DIALECT = countries_dialects.get(dst_country);
                if (LANGUAGE.DST_DIALECT != null) {
                    LANGUAGE.DST = LANGUAGE.DST_DIALECT.split("-")[0];
                    switch (LANGUAGE.DST_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.DST = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.DST = "zh-Hans";
                            break;
                    }
                }
                setText(textview_dst_dialect, LANGUAGE.DST_DIALECT);
                setText(textview_dst, LANGUAGE.DST);

                int h;
                if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh-Hans") || Objects.equals(LANGUAGE.DST, "zh-Hant")) {
                    h = 75;
                }
                else {
                    h = 62;
                }
                voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

                stop_voice_recognizer();
                stop_create_overlay_translation_text();
                stop_create_overlay_mic_button();
                if (OVERLAYING_STATUS.IS_OVERLAYING) {
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_off);
                    } else {
                        start_voice_recognizer();
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_on);
                    }
                    start_create_overlay_mic_button();
                    if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setBackgroundColor(Color.parseColor("#80000000"));

                    start_create_overlay_translation_text();
                }
                RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
                setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                OVERLAYING_STATUS.STRING =  "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                textview_overlaying.setText(OVERLAYING_STATUS.STRING);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                String src_country = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC_DIALECT = countries_dialects.get(src_country);
                if (LANGUAGE.SRC_DIALECT != null) {
                    LANGUAGE.SRC = LANGUAGE.SRC_DIALECT.split("-")[0];
                    switch (LANGUAGE.SRC_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.SRC = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.SRC = "zh-Hans";
                            break;
                    }
                }
                setText(textview_src_dialect, LANGUAGE.SRC_DIALECT);
                setText(textview_src, LANGUAGE.SRC);

                String dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST_DIALECT = countries_dialects.get(dst_country);
                if (LANGUAGE.DST_DIALECT != null) {
                    LANGUAGE.DST = LANGUAGE.DST_DIALECT.split("-")[0];
                    switch (LANGUAGE.DST_DIALECT) {
                        case "yue-Hant-HK":
                        case "cmn-Hant-TW":
                            LANGUAGE.DST = "zh-Hant";
                            break;
                        case "cmn-Hans-CN":
                        case "cmn-Hans-HK":
                            LANGUAGE.DST = "zh-Hans";
                            break;
                    }
                }
                setText(textview_dst_dialect, LANGUAGE.DST_DIALECT);
                setText(textview_dst, LANGUAGE.DST);
            }
        });

        button_toggle_overlay.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                OVERLAYING_STATUS.IS_OVERLAYING = !OVERLAYING_STATUS.IS_OVERLAYING;
                OVERLAYING_STATUS.STRING =  "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                if (OVERLAYING_STATUS.IS_OVERLAYING) {
                    if (Settings.canDrawOverlays(getApplicationContext())) {
                        start_create_overlay_mic_button();
                        start_create_overlay_translation_text();
                    }
                    else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        Runnable runnable = () -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                        executorService.execute(runnable);
                        handler.postDelayed(() -> {
                            if (Settings.canDrawOverlays(getApplicationContext())) {
                                start_create_overlay_mic_button();
                                start_create_overlay_translation_text();
                                OVERLAYING_STATUS.IS_OVERLAYING = true;
                                String os = "Overlay permission granted";
                                setText(textview_output_messages, os);
                            }
                            else {
                                OVERLAYING_STATUS.IS_OVERLAYING = false;
                                String os = "Please retry to tap TOGGLE OVERLAY button again";
                                setText(textview_output_messages, os);
                            }
                            OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                            setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                        }, 15000);
                    }
                } else {
                    stop_voice_recognizer();
                    stop_create_overlay_translation_text();
                    stop_create_overlay_mic_button();
                    RECOGNIZING_STATUS.IS_RECOGNIZING = false;
                    RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
                    setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                    OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                    setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                    setText(textview_output_messages, "");
                    VOICE_TEXT.STRING = "";
                    TRANSLATION_TEXT.STRING = "";
                    setText(voice_text, "");
                    String hints = "Recognized words";
                    voice_text.setHint(hints);
                    audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, (int)Double.parseDouble(String.valueOf((long)(audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 2))), 0);
                    if (create_overlay_translation_text.overlay_translation_text != null) {
                        setText(create_overlay_translation_text.overlay_translation_text, "");
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                    }
                    if (create_overlay_mic_button.mic_button != null) {
                        create_overlay_mic_button.mic_button.setVisibility(View.INVISIBLE);
                    }
                    setText(textview_output_messages, "");
                    VOICE_TEXT.STRING = "";
                    TRANSLATION_TEXT.STRING = "";
                    setText(voice_text, "");
                    RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
                    setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                    OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                    setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                    hints = "Recognized words";
                    voice_text.setHint(hints);
                }
            }
            return false;
        });

        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stop_create_overlay_translation_text();
        stop_create_overlay_mic_button();
        stop_voice_recognizer();
        audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mStreamVolume, AudioManager.ADJUST_SAME);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop_create_overlay_translation_text();
        stop_create_overlay_mic_button();
        stop_voice_recognizer();
        audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mStreamVolume, AudioManager.ADJUST_SAME);
    }

    public void setup_spinner(ArrayList<String> supported_languages)
    {
        countries_dialects = new HashMap<>();
        for (int i=0;i<supported_languages.size();i++) {
            countries_dialects.put(supported_languages.get(i), dialects[i]);
        }

        Collections.sort(supported_languages);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_textview_align, supported_languages);
        adapter.setDropDownViewResource(R.layout.spinner_textview_align);
        spinner_src_languages.setAdapter(adapter);
        spinner_src_languages.setSelection(supported_languages.indexOf("Indonesian (Indonesia)"));
        spinner_dst_languages.setAdapter(adapter);
        spinner_dst_languages.setSelection(supported_languages.indexOf("English (United States)"));
    }

    /*private void checkRecordAudioPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }*/

    /*private void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }
    }*/

    private void start_create_overlay_mic_button() {
        Intent i = new Intent(this, create_overlay_mic_button.class);
        startService(i);
    }

    private void stop_create_overlay_mic_button() {
        stopService(new Intent(this, create_overlay_mic_button.class));
    }

    private void start_create_overlay_translation_text() {
        Intent i = new Intent(this, create_overlay_translation_text.class);
        startService(i);
    }

    private void stop_create_overlay_translation_text() {
        stopService(new Intent(this, create_overlay_translation_text.class));
    }

    private void start_voice_recognizer() {
        Intent i = new Intent(this, VoiceRecognizer.class);
        startService(i);
    }

    private void stop_voice_recognizer() {
        stopService(new Intent(this, VoiceRecognizer.class));
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void setText(final TextView tv, final String text){
        new Handler(Looper.getMainLooper()).post(() -> tv.setText(text));
        //runOnUiThread(() -> tv.setText(text));
    }

    private void loadLocaleLanguages1() {
        ArrayList<String> localLanguages = new ArrayList<>();
        ArrayList<String> tempDialects = new ArrayList<>();
        ArrayList<String> tempCountries = new ArrayList<>();

        // Retrieve the complete local list available on the Android OS system.
        Locale[] availableLocales = Locale.getAvailableLocales();

        // Create a whitelist of major world languages fully supported by voice recognition
        // You can add or remove language codes (ISO 639-1) below based on your app's target market
        java.util.List<String> supportedVoiceLangs = java.util.Arrays.asList(
                // --- ASIA TIMUR & TENGGARA ---
                "id",   // Indonesia
                "in",   // Indonesia
                "jv",   // Jawa (Indonesia)
                "su",   // Sunda (Indonesia)
                "ms",   // Melayu (Malaysia / Brunei)
                "zh",   // Mandarin / China (Simplified, Traditional, Hong Kong, Taiwan)
                "ja",   // Jepang
                "ko",   // Korea
                "th",   // Thailand
                "vi",   // Vietnam
                "fil",  // Filipina / Tagalog
                "km",   // Khmer (Kamboja)
                "lo",   // Lao (Laos)
                "my",   // Burma (Myanmar)

                // --- ASIA SELATAN (INDIA & SEKITARNYA) ---
                "hi",   // Hindi
                "bn",   // Bengali (India / Bangladesh)
                "ta",   // Tamil (India / Singapura / Sri Lanka)
                "te",   // Telugu
                "kn",   // Kannada
                "mr",   // Marathi
                "gu",   // Gujarati
                "ml",   // Malayalam
                "ur",   // Urdu (India / Pakistan)
                "ne",   // Nepali
                "si",   // Sinhala (Sri Lanka)
                "pa",   // Punjabi

                // --- EROPA BARAT, UTARA & SELATAN ---
                "en",   // Inggris (AS, Inggris, Australia, India, Kanada, Nigeria, Ghana, dll.)
                "es",   // Spanyol (Spanyol, Meksiko, Argentina, Kolombia, dll.)
                "fr",   // Prancis (Prancis, Kanada, Swiss, Belgia)
                "de",   // Jerman (Jerman, Austria, Swiss)
                "it",   // Italia
                "nl",   // Belanda (Belanda, Belgia)
                "pt",   // Portugis (Brasil, Portugal)
                "sv",   // Swedia
                "no",   // Norwegia
                "da",   // Denmark
                "fi",   // Finlandia
                "is",   // Islandia
                "gl",   // Galisia
                "ca",   // Katala
                "eu",   // Basque

                // --- EROPA TIMUR & BALKAN ---
                "ru",   // Rusia
                "uk",   // Ukraina
                "tr",   // Turki
                "pl",   // Polandia
                "cs",   // Ceko
                "sk",   // Slowakia
                "hu",   // Hungaria
                "ro",   // Rumania
                "bg",   // Bulgaria
                "el",   // Yunani
                "hr",   // Kroasia
                "sr",   // Serbia
                "sl",   // Slovenia
                "et",   // Estonia
                "lv",   // Latvia
                "lt",   // Lithuania
                "sq",   // Albania
                "bs",   // Bosnia
                "mk",   // Makedonia

                // --- TIMUR TENGAH & ASIA TENGAH ---
                "ar",   // Arab (Mesir, Saudi, UAE, Irak, dll.)
                "fa",   // Persia / Farsi
                "he",   // Ibrani (Israel)
                "iw",   // Ibrani lama
                "ka",   // Georgia
                "hy",   // Armenia
                "az",   // Azerbaijani
                "kk",   // Kazakh
                "ky",   // Kirgiz
                "uz",   // Uzbek

                // --- AFRIKA ---
                "sw",   // Swahili (Tanzania, Kenya)
                "af",   // Afrikaans
                "zu",   // Zulu
                "xh",   // Xhosa
                "am",   // Amharik (Etiopia)
                "so",   // Somali
                "ha",   // Hausa
                "ig",   // Igbo
                "yo"    // Yoruba
        );

        for (Locale locale : availableLocales) {
            // Take the raw language code (e.g., "id", "en") and country code (e.g., "ID", "US")
            String language = locale.getLanguage();
            String country = locale.getCountry();

            // Filter: Only include locales with a clear combination of language AND country.
            if (!language.isEmpty() && !country.isEmpty() && supportedVoiceLangs.contains(language)) {

                // Manually format the language name to: "Language (Country)"
                // Example: "Indonesia (Indonesia)" or "English (United States)"
                String displayLanguage = locale.getDisplayLanguage(Locale.getDefault()).trim();
                String displayCountry = locale.getDisplayCountry(Locale.getDefault()).trim();
                String formattedName = displayLanguage + " (" + displayCountry + ")";
                // Get the standard dialect tag code (e.g., "id-ID", "en-US")
                String languageTag = locale.toLanguageTag();

                // Avoid duplicate items in the Spinner list.
                if (!localLanguages.contains(formattedName)) {
                    localLanguages.add(formattedName);
                    tempDialects.add(languageTag);
                    tempCountries.add(formattedName);
                }
            }
        }

        // Add it to global array so it can be read by the subtitle engine or voice recognizer
        dialects = tempDialects.toArray(new String[0]);
        countries = tempCountries.toArray(new String[0]);
        arraylist_languages = new ArrayList<>(localLanguages);

        // Safely add it to spinner in version 17.xx.xx
        setup_spinner(localLanguages);
    }

    private void loadLocaleLanguages() {
        ArrayList<String> localLanguages = new ArrayList<>();

        // Retrieves the list of language code standards (ISO 639) currently supported by the Android Engine
        String[] isoLanguages = Locale.getISOLanguages();
        ArrayList<String> tempDialects = new ArrayList<>();
        ArrayList<String> tempCountries = new ArrayList<>();

        // Limit the number of primary languages ​​retrieved so the Spinner doesn't become too crowded or heavy.
        for (String langCode : isoLanguages) {
            Locale locale = new Locale(langCode);
            String displayName = locale.getDisplayName().trim();

            // Filter to exclude strange language names or those consisting of numeric codes
            if (!displayName.isEmpty() && !localLanguages.contains(displayName) && displayName.length() < 30) {
                localLanguages.add(displayName);
                tempDialects.add(locale.toLanguageTag());
                tempCountries.add(displayName);
            }
        }

        // Convert back to your global array to keep it synchronized with your audio recording system.
        dialects = tempDialects.toArray(new String[0]);
        countries = tempCountries.toArray(new String[0]);

        // Safely insert the result into your spinner in version 17
        setup_spinner(localLanguages);
    }

}
