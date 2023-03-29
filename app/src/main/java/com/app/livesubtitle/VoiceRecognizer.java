package com.app.livesubtitle;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.View;
import android.widget.TextView;
//import android.widget.Toast;

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

    @Override
    public void onCreate() {
        super.onCreate();
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
            //speechRecognizerIntent.putExtra("android.speech.extra.SEGMENTED_SESSION", true);
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
                }

                @Override
                public void onBeginningOfSpeech() {
                    setText(MainActivity.textview_debug, "onBeginningOfSpeech");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    //setText(MainActivity.textview_debug, "onRmsChanged: " + rmsdB);
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    setText(MainActivity.textview_debug, "onBufferReceived: " + Arrays.toString(buffer));
                }

                @Override
                public void onEndOfSpeech() {
                    setText(MainActivity.textview_debug, "onEndOfSpeech");
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        speechRecognizer.stopListening();
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
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
                    }
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
                    if (VOICE_TEXT.STRING != null) {
                        if (!Objects.equals(VOICE_TEXT.STRING, "")) {
                            //translate(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
                            GoogleTranslate(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
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

    /*public void translate(String t, String src, String dst) {
        GoogleTranslateAPITranslator translate = new GoogleTranslateAPITranslator();
        translate.setOnTranslationCompleteListener(new GoogleTranslateAPITranslator.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {}

            @Override
            public void onCompleted(String translation) {
                TRANSLATION_TEXT.STRING = translation;
                if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                    if (TRANSLATION_TEXT.STRING.length() == 0) {
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
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
            }

            @Override
            public void onError(Exception e) {
                //Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                setText(MainActivity.textview_output_messages, e.getMessage());
            }
        });
        translate.execute(t, src, dst);
    }*/

    /*public void gtranslate(String t, String src, String dst) {
        GoogleClient5Translator translate = new GoogleClient5Translator();
        translate.setOnTranslationCompleteListener(new GoogleClient5Translator.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {}

            @Override
            public void onCompleted(String translation) {
                if (RECOGNIZING_STATUS.IS_RECOGNIZING) {
                    if (TRANSLATION_TEXT.STRING.length() == 0) {
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
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


            }

            @Override
            public void onError(Exception e) {
                //toast("Unknown error");
                //setText(textview_output_messages, e.getMessage());
            }
        });
        translate.execute(t, src, dst);
    }*/

    /*private void toast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }*/

    public void setText(final TextView tv, final String text){
        new Handler(Looper.getMainLooper()).post(() -> tv.setText(text));
    }

    private String GoogleTranslate(String SENTENCE, String SRC, String DST) {
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
                }

                catch (Exception e) {
                    Log.e("GoogleTranslate", e.getMessage());
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

}
