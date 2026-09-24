package com.app.livesubtitle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class VoiceRecognizer extends Service {

    public VoiceRecognizer() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private SpeechRecognizer speechRecognizer = null;
    public static Intent speechRecognizerIntent;
    private Timer timer;
    private TimerTask timerTask;
    private volatile int GOOGLE_TRANSLATE_ENDPOINT = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("VoiceRecognizer", "Starting VoiceRecognizer service"
        );

        int h;
        if (Objects.equals(LANGUAGE.SRC, "ja") || Objects.equals(LANGUAGE.SRC, "zh-Hans") || Objects.equals(LANGUAGE.SRC, "zh-Hant")) {
            h = 122;
        } else {
            h = 109;
        }
        MainActivity.voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

        String src_dialect = LANGUAGE.SRC_DIALECT;
        if (speechRecognizer != null) speechRecognizer.destroy();
        RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
        MainActivity.textview_recognizing.setText(RECOGNIZING_STATUS.STRING);
        OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
        MainActivity.textview_overlaying.setText(OVERLAYING_STATUS.STRING);

        // =========================================================
        // ENDPOINT TEST
        // =========================================================
        GOOGLE_TRANSLATE_ENDPOINT = 0;
        testGoogleTranslateEndpoints();

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, Objects.requireNonNull(getClass().getPackage()).getName());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            //speechRecognizerIntent.putExtra("android.speech.extra.HIDE_PARTIAL_TRAILING_PUNCTUATION", true);
            //speechRecognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
            //speechRecognizerIntent.putExtra("android.speech.extra.AUDIO_SOURCE",true);
            //speechRecognizerIntent.putExtra("android.speech.extra.GET_AUDIO",true);
            //speechRecognizerIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", AudioFormat.ENCODING_PCM_8BIT);
            speechRecognizerIntent.putExtra("android.speech.extra.SEGMENTED_SESSION", true);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, src_dialect);
            //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,3600000);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.google.android.googlequicksearchbox");
            //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle arg0) {
                    setText(MainActivity.textview_debug, "onReadyForSpeech");
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                                //sendMediaPlay();
                                MyNotificationListenerService.ensureOperaPlaying();
                            }
                        }, 200);
                    }
                }

                @Override
                public void onBeginningOfSpeech() {
                    setText(MainActivity.textview_debug, "onBeginningOfSpeech");
                    /*
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                                //sendMediaPlay();
                                MyNotificationListenerService.ensureOperaPlaying();
                            }
                        }, 200);
                    }
                    */
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    //setText(MainActivity.textview_debug, "onRmsChanged: " + rmsdB);
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    setText(MainActivity.textview_debug, "onBufferReceived: " + Arrays.toString(buffer));
                    /*
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                                //sendMediaPlay();
                                MyNotificationListenerService.ensureOperaPlaying();
                            }
                        }, 200);
                    }
                    */
                }

                @Override
                public void onEndOfSpeech() {
                    setText(MainActivity.textview_debug, "onEndOfSpeech");
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                                //sendMediaPlay();
                                MyNotificationListenerService.ensureOperaPlaying();
                            }
                        }, 200);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        if (Objects.equals(getErrorText(errorCode), "Insufficient permissions")) {
                            setText(MainActivity.textview_output_messages, "Please give RECORD AUDIO PERMISSION (USE MICROPHONE PERMISSION) to GOOGLE APP");
                        }
                        else {
                            setText(MainActivity.textview_debug, "onError : " + getErrorText(errorCode));
                        }
                        speechRecognizer.startListening(speechRecognizerIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                                //sendMediaPlay();
                                MyNotificationListenerService.ensureOperaPlaying();
                            }
                        }, 200);                    }
                }

                @Override
                public void onResults(Bundle results) {
                        /*setText(MainActivity.textview_output_messages, "onResults");
                        if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                            speechRecognizer.stopListening();
                        } else {
                            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                            VOICE_TEXT.STRING = matches.get(0).toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
                            MainActivity.voice_text.setText(VOICE_TEXT.STRING);
                            MainActivity.voice_text.setSelection(MainActivity.voice_text.getText().length());
                            speechRecognizer.startListening(speechRecognizerIntent);
                        }*/
                }

                @Override
                public void onPartialResults(Bundle results) {
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (PREFER_OFFLINE_STATUS.OFFLINE) {
                            ArrayList<String> unstableData = results.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");
                            VOICE_TEXT.STRING = data.get(0).toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC)) + unstableData.get(0).toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
                        } else {
                            StringBuilder text = new StringBuilder();
                            for (String result : data)
                                text.append(result);
                            VOICE_TEXT.STRING = text.toString().toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
                        }
                        MainActivity.voice_text.setText(VOICE_TEXT.STRING);
                        MainActivity.voice_text.setSelection(MainActivity.voice_text.getText().length());
                    }
                }

                @Override
                public void onEvent(int arg0, Bundle arg1) {
                    setText(MainActivity.textview_output_messages, "onEvent");
                    /*
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                                //sendMediaPlay();
                                MyNotificationListenerService.ensureOperaPlaying();
                            }
                        }, 200);
                    }
                    */
                }

                public String getErrorText(int errorCode) {
                    String message;
                    switch (errorCode) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "Audio recording error";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "Client side error";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "Insufficient permissions";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "Network error";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "Network timeout";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "No match";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RecognitionService busy";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "error from server";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "No speechRecognizer input";
                            break;
                        default:
                            message = "Didn't understand, please try again.";
                            break;
                    }
                    return message;
                }
            });
        }

        if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
            speechRecognizer.startListening(speechRecognizerIntent);
            timer = new Timer();
            timerTask = new TimerTask() {

                @Override
                public void run() {

                    if (VOICE_TEXT.STRING != null &&
                            !Objects.equals(VOICE_TEXT.STRING, "")) {

                        // =================================================
                        // ENDPOINT TEST HAS NOT FINISHED YET
                        // =================================================
                        if (GOOGLE_TRANSLATE_ENDPOINT == 0) {
                            Log.d("testGoogleTranslate", "Waiting for endpoint test...");
                            return;
                        }
                        // =================================================
                        // ENDPOINT 2 SUCCESS
                        // =================================================
                        if (GOOGLE_TRANSLATE_ENDPOINT == 1) {
                            Log.d("testGoogleTranslate", "Using GoogleTranslate1");
                            GoogleTranslate1(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
                        }
                        // =================================================
                        // ENDPOINT 2 SUCCESS
                        // =================================================
                        else if (GOOGLE_TRANSLATE_ENDPOINT == 2) {
                            Log.d("testGoogleTranslate", "Using GoogleTranslate2");
                            GoogleTranslate2(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);

                        }
                        // =================================================
                        // BOTH ENDPOINT FAILED
                        // =================================================
                        else if (GOOGLE_TRANSLATE_ENDPOINT == -1) {
                            Log.e("testGoogleTranslate", "No working Google Translate endpoint");
                        }
                    }
                }
            };
            timer.schedule(timerTask,0,2000);

        } else {
            speechRecognizer.stopListening();
            if (timerTask != null) timerTask.cancel();
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public void setText(final TextView tv, final String text){
            new Handler(Looper.getMainLooper()).post(() -> tv.setText(text));
    }

    private void testGoogleTranslateEndpoints() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean SELECT_ENDPOINT_1 = false;
            boolean SELECT_ENDPOINT_2 = false;
            HttpClient httpClient = null;
            // =========================================================
            // TEST ENDPOINT 1
            // https://clients5.google.com/translate_a/t
            // =========================================================
            try {
                String testSentence = URLEncoder.encode("Hello", "UTF-8");
                String url =
                        "https://clients5.google.com/translate_a/t" +
                                "?client=dict-chrome-ex" +
                                "&sl=en" +
                                "&tl=id" +
                                "&q=" + testSentence;
                Log.d("testGoogleTranslate", "Testing endpoint 1: " + url);
                httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/139.0.0.0 Safari/537.36"
                );
                HttpResponse response = httpClient.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                Log.d("testGoogleTranslate", "Endpoint 1 status code: " + statusLine.getStatusCode());
                if (statusLine.getStatusCode() == 200) {
                    if (response.getEntity() != null) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        response.getEntity().writeTo(output);
                        String responseString = output.toString("UTF-8");
                        output.close();
                        Log.d("testGoogleTranslate", "Endpoint 1 response: " + responseString);
                        JSONArray jsonArray = new JSONArray(responseString);
                        if (jsonArray.length() > 0) {
                            SELECT_ENDPOINT_1 = true;
                        }
                    }
                }

            } catch (Exception e) {
                Log.e("testGoogleTranslate", "Endpoint 1 FAILED",e);

            } finally {
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
            }

            // =========================================================
            // TEST ENDPOINT 2
            // https://translate.googleapis.com/translate_a/single
            // =========================================================
            try {
                String testSentence = URLEncoder.encode("Hello", "UTF-8");
                String url =
                        "https://translate.googleapis.com/translate_a/" +
                                "single?client=gtx" +
                                "&sl=en" +
                                "&tl=id" +
                                "&dt=t" +
                                "&q=" + testSentence;
                Log.d("testGoogleTranslate", "Testing endpoint 2: " + url);
                httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                Log.d("testGoogleTranslate", "Endpoint 2 status code: " + statusLine.getStatusCode());

                if (statusLine.getStatusCode() == 200) {
                    if (response.getEntity() != null) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        response.getEntity().writeTo(output);
                        String responseString = output.toString("UTF-8");
                        output.close();
                        Log.d("testGoogleTranslate", "Endpoint 2 response: " +responseString);
                        JSONArray jsonArray = new JSONArray(responseString);
                        if (jsonArray.length() > 0) {
                            SELECT_ENDPOINT_2 = true;
                        }
                    }
                }

            } catch (Exception e) {
                Log.e("testGoogleTranslate", "Endpoint 2 FAILED", e);

            } finally {
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
            }

            // =========================================================
            // If endpoint 1 success,choose endpoint 1
            // =========================================================
            if (SELECT_ENDPOINT_1) {
                GOOGLE_TRANSLATE_ENDPOINT = 1;
                Log.d("testGoogleTranslate", "SELECTED ENDPOINT = 1");
                executor.shutdown();
                return;
            }

            // =========================================================
            // CHOOSE FINAL ENDPOINT
            // =========================================================
            if (SELECT_ENDPOINT_2) {
                GOOGLE_TRANSLATE_ENDPOINT = 2;
                Log.d("testGoogleTranslate", "SELECTED ENDPOINT = 2");
            } else {
                GOOGLE_TRANSLATE_ENDPOINT = -1;
                Log.e("testGoogleTranslate", "BOTH GOOGLE TRANSLATE ENDPOINTS FAILED");
            }
            executor.shutdown();
        });
    }

    private void GoogleTranslate1(String SENTENCE, String SRC, String DST) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<String> TRANSLATION = new AtomicReference<>("");

        try {
            SENTENCE = URLEncoder.encode(SENTENCE, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String finalSENTENCE = SENTENCE;

        executor.execute(() -> {
            HttpClient httpClient = null;
            try {
                String url = "https://clients5.google.com/translate_a/t";
                String params =
                        "?client=dict-chrome-ex"
                                + "&sl=" + SRC
                                + "&tl=" + DST
                                + "&q=" + finalSENTENCE;

                httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url + params);
                httpget.setHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/139.0.0.0 Safari/537.36"
                );
                HttpResponse response = httpClient.execute(httpget);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == 200) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(byteArrayOutputStream);
                    String responseString = byteArrayOutputStream.toString("UTF-8");
                    byteArrayOutputStream.close();
                    Log.d("GoogleTranslate1", "Response: " + responseString);
                    JSONArray jsonArray = new JSONArray(responseString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (!jsonArray.isNull(i)) {
                            TRANSLATION.set(TRANSLATION.get()+ jsonArray.getString(i));
                        }
                    }
                    Log.d("GoogleTranslate1", "TRANSLATION: " + TRANSLATION.get());

                } else {
                    Log.e("GoogleTranslate1", "HTTP " + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
                    if (response.getEntity() != null) {
                        response.getEntity().getContent().close();
                    }
                    throw new IOException("http " + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
                }

            } catch (Exception e) {
                Log.e("GoogleTranslate1", "Translation error", e);

            } finally {
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
            }

            handler.post(() -> {
                TRANSLATION_TEXT.STRING = TRANSLATION.toString();
                Log.d("GoogleTranslate1", "TRANSLATION_TEXT.STRING: " + TRANSLATION_TEXT.STRING);

                if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                    if (TRANSLATION_TEXT.STRING.length() == 0) {
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setVisibility(
                                        View.INVISIBLE
                                );
                        create_overlay_translation_text
                                .overlay_translation_text_container
                                .setVisibility(
                                        View.INVISIBLE
                                );
                    } else {
                        create_overlay_translation_text
                                .overlay_translation_text_container
                                .setVisibility(
                                        View.VISIBLE
                                );
                        create_overlay_translation_text
                                .overlay_translation_text_container
                                .setBackgroundColor(
                                        Color.TRANSPARENT
                                );
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setVisibility(
                                        View.VISIBLE
                                );
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setBackgroundColor(
                                        Color.TRANSPARENT
                                );
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setTextIsSelectable(
                                        true
                                );
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setText(
                                        TRANSLATION_TEXT.STRING
                                );
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setSelection(
                                        create_overlay_translation_text
                                                .overlay_translation_text
                                                .getText()
                                                .length()
                                );
                        Spannable spannableString =
                                new SpannableStringBuilder(
                                        TRANSLATION_TEXT.STRING
                                );

                        int selectionEnd = create_overlay_translation_text.overlay_translation_text.getSelectionEnd();

                        spannableString.setSpan(
                                new ForegroundColorSpan(
                                        Color.YELLOW
                                ),
                                0,
                                selectionEnd,
                                0
                        );
                        spannableString.setSpan(
                                new BackgroundColorSpan(
                                        Color.parseColor(
                                                "#80000000"
                                        )
                                ),
                                0,
                                selectionEnd,
                                0
                        );

                        create_overlay_translation_text
                                .overlay_translation_text
                                .setText(
                                        spannableString
                                );
                        create_overlay_translation_text
                                .overlay_translation_text
                                .setSelection(
                                        create_overlay_translation_text
                                                .overlay_translation_text
                                                .getText()
                                                .length()
                                );
                    }

                } else {
                    create_overlay_translation_text
                            .overlay_translation_text
                            .setVisibility(
                                    View.INVISIBLE
                            );

                    create_overlay_translation_text
                            .overlay_translation_text_container
                            .setVisibility(
                                    View.INVISIBLE
                            );
                }
            });
        });
    }

    private String GoogleTranslate2(String SENTENCE, String SRC, String DST) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<String> TRANSLATION = new AtomicReference<>("");
        try {
            SENTENCE = URLEncoder.encode(SENTENCE, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String finalSENTENCE = SENTENCE;
        if (RECOGNIZING_STATUS.IS_RECOGNIZING && finalSENTENCE != null) {
            executor.execute(() -> {
                HttpClient httpClient;
                try {
                    String url = "https://translate.googleapis.com/translate_a/";
                    String params = "single?client=gtx&sl=" + SRC + "&tl=" + DST + "&dt=t&q=" + finalSENTENCE;
                    httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(new HttpGet(url + params));
                    ByteArrayOutputStream byteArrayOutputStream;
                    StatusLine statusLine = response.getStatusLine();
                    JSONArray jsonArray;
                    if (statusLine.getStatusCode() == 200) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        response.getEntity().writeTo(byteArrayOutputStream);
                        String stringOfByteArrayOutputStream = byteArrayOutputStream.toString();
                        try {
                            jsonArray = new JSONArray(Objects.requireNonNull(stringOfByteArrayOutputStream)).getJSONArray(0);
                            //Log.d("GoogleTranslate2", "jsonArray = " + jsonArray);
                            int length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                TRANSLATION.set(TRANSLATION + new JSONArray(Objects.requireNonNull(stringOfByteArrayOutputStream)).getJSONArray(0).getJSONArray(i).get(0).toString());
                            }
                        }
                        catch(Exception e) {
                            Log.e("GoogleTranslate2", e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        response.getEntity().getContent().close();
                        httpClient.getConnectionManager().shutdown();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                    byteArrayOutputStream.close();
                    httpClient.getConnectionManager().shutdown();
                }

                catch (Exception e) {
                    Log.e("GoogleTranslate2", e.getMessage());
                    e.printStackTrace();
                }

                handler.post(() -> {
                    TRANSLATION_TEXT.STRING = TRANSLATION.toString();
                    if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        if (TRANSLATION_TEXT.STRING.length() == 0) {
                            create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                            create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                            executor.shutdown();
                        } else {
                            create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.VISIBLE);
                            create_overlay_translation_text.overlay_translation_text_container.setBackgroundColor(Color.TRANSPARENT);
                            create_overlay_translation_text.overlay_translation_text.setVisibility(View.VISIBLE);
                            create_overlay_translation_text.overlay_translation_text.setBackgroundColor(Color.TRANSPARENT);
                            create_overlay_translation_text.overlay_translation_text.setTextIsSelectable(true);
                            create_overlay_translation_text.overlay_translation_text.setText(TRANSLATION_TEXT.STRING);
                            create_overlay_translation_text.overlay_translation_text.setSelection(create_overlay_translation_text.overlay_translation_text.getText().length());
                            Spannable spannableString = new SpannableStringBuilder(TRANSLATION_TEXT.STRING);
                            spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW),
                                    0,
                                    create_overlay_translation_text.overlay_translation_text.getSelectionEnd(),
                                    0);
                            spannableString.setSpan(new BackgroundColorSpan(Color.parseColor("#80000000")),
                                    0,
                                    create_overlay_translation_text.overlay_translation_text.getSelectionEnd(),
                                    0);
                            create_overlay_translation_text.overlay_translation_text.setText(spannableString);
                            create_overlay_translation_text.overlay_translation_text.setSelection(create_overlay_translation_text.overlay_translation_text.getText().length());
                        }
                    } else {
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                    }
                });
            });
        }
        else {
            executor.shutdown();
        }
        return TRANSLATION.toString();
    }

    private void sendMediaPlay() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        long eventTime = System.currentTimeMillis();
        KeyEvent down = new KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PLAY,
                0
        );
        KeyEvent up = new KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_MEDIA_PLAY,
                0
        );
        audioManager.dispatchMediaKeyEvent(down);
        audioManager.dispatchMediaKeyEvent(up);
    }

    private void restartSpeechRecognizerAndPlay() {
        if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
            speechRecognizer.stopListening();
        } else {
            speechRecognizer.startListening(speechRecognizerIntent);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                    //sendMediaPlay();
                    MyNotificationListenerService.ensureOperaPlaying();
                }
            }, 200);
        }
    }

    private String playbackStateToString(int state) {
        switch (state) {
            case PlaybackState.STATE_NONE:
                return "STATE_NONE";

            case PlaybackState.STATE_STOPPED:
                return "STATE_STOPPED";

            case PlaybackState.STATE_PAUSED:
                return "STATE_PAUSED";

            case PlaybackState.STATE_PLAYING:
                return "STATE_PLAYING";

            case PlaybackState.STATE_FAST_FORWARDING:
                return "STATE_FAST_FORWARDING";

            case PlaybackState.STATE_REWINDING:
                return "STATE_REWINDING";

            case PlaybackState.STATE_BUFFERING:
                return "STATE_BUFFERING";

            case PlaybackState.STATE_ERROR:
                return "STATE_ERROR";

            case PlaybackState.STATE_CONNECTING:
                return "STATE_CONNECTING";

            default:
                return "UNKNOWN(" + state + ")";
        }
    }

    private void testOperaPlaybackState() {
        try {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            if (mediaSessionManager == null) {
                Log.e("MediaSessionTest", "MediaSessionManager == null");
                return;
            }
            List<MediaController> controllers = mediaSessionManager.getActiveSessions(null);
            Log.d("MediaSessionTest", "Active sessions: " + controllers.size());

            for (MediaController controller : controllers) {
                String packageName = controller.getPackageName();
                Log.d("MediaSessionTest", "Package: " + packageName);

                if (packageName != null && packageName.toLowerCase(Locale.ROOT).contains("opera")) {
                    Log.d("MediaSessionTest", "===== OPERA MEDIA SESSION =====");
                    PlaybackState playbackState = controller.getPlaybackState();
                    if (playbackState == null) {
                        Log.d("MediaSessionTest", "Opera PlaybackState = NULL");
                    } else {
                        int state = playbackState.getState();
                        Log.d("MediaSessionTest", "Opera PlaybackState = " + playbackStateToString(state));
                        Log.d("MediaSessionTest", "Position = " + playbackState.getPosition());
                        Log.d("MediaSessionTest", "Playback speed = " + playbackState.getPlaybackSpeed());
                        Log.d("MediaSessionTest", "Actions = " + playbackState.getActions());
                        Log.d("MediaSessionTest", "Last update time = " + playbackState.getLastPositionUpdateTime());
                    }

                    MediaMetadata metadata = controller.getMetadata();
                    if (metadata != null) {
                        Log.d("MediaSessionTest", "Title = " + metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
                        Log.d("MediaSessionTest", "Artist = " + metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));
                    }
                    Log.d("MediaSessionTest", "================================");
                }
            }

        } catch (SecurityException e) {
            Log.e("MediaSessionTest", "Cannot access active MediaSessions: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e("MediaSessionTest", "MediaSession test error", e);
        }
    }

}
