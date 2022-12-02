package com.app.livesubtitle;

//import static com.app.livesubtitle.MainActivity.textview_debug;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
//import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceRecognizer extends Service {

    public VoiceRecognizer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private SpeechRecognizer speechRecognizer = null;
    public static Intent speechRecognizerIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        int h;
        if (Objects.equals(LANGUAGE.SRC, "ja") || Objects.equals(LANGUAGE.SRC, "zh-Hans") || Objects.equals(LANGUAGE.SRC, "zh-Hant")) {
            h = 122;
        }
        else {
            h = 109;
        }
        MainActivity.voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

        String src_dialect = LANGUAGE.SRC_DIALECT;

        Timer timer = new Timer();
        if(speechRecognizer != null) speechRecognizer.destroy();
        //setText(textview_debug, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));

        String string_recognizing = "recognizing=" + RECOGNIZING_STATUS.RECOGNIZING;
        setText(MainActivity.textview_recognizing, string_recognizing);
        String string_overlaying = "Overlaying=" + OVERLAYING_STATUS.OVERLAYING;
        setText(MainActivity.textview_overlaying, string_overlaying);

        if(SpeechRecognizer.isRecognitionAvailable(this)) {

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
            //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle arg0) {
                    //setText(textview_debug, "onReadyForSpeech");
                }

                @Override
                public void onBeginningOfSpeech() {
                    //setText(textview_debug, "onBeginningOfSpeech");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    //setText(textview_debug, "onRmsChanged: " + rmsdB);
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    //setText(textview_debug, "onBufferReceived: " + buffer);
                }

                @Override
                public void onEndOfSpeech() {
                    Handler handler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            //setText(textview_debug, "onEndOfSpeech");
                            if (!RECOGNIZING_STATUS.RECOGNIZING) {
                                speechRecognizer.stopListening();
                            } else {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }
                    };
                    handler.sendEmptyMessage(1);
                }

                @Override
                public void onError(int errorCode) {
                    Handler handler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            //String errorMessage = getErrorText(errorCode);
                            //setText(textview_debug, "FAILED " + errorMessage);
                            if (!RECOGNIZING_STATUS.RECOGNIZING) {
                                speechRecognizer.stopListening();
                            } else {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }
                    };
                    handler.sendEmptyMessage(1);
                }

                @Override
                public void onResults(Bundle results) {
                    /*Handler handler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            //setText(textview_debug, "onResults");
                            if (!RECOGNIZING_STATUS.RECOGNIZING) {
                                speechRecognizer.stopListening();
                            } else {
                                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                                VOICE_TEXT.STRING = matches.get(0).toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
                                setText(MainActivity.voice_text, VOICE_TEXT.STRING);
                                MainActivity.voice_text.setSelection(MainActivity.voice_text.getText().length());
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }
                    };
                    handler.sendEmptyMessage(1);*/
                }

                @Override
                public void onPartialResults(Bundle results) {
                    Handler handler = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            //setText(textview_debug, "onResults");
                            if (!RECOGNIZING_STATUS.RECOGNIZING) {
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
                    };
                    handler.sendEmptyMessage(1);
                }

                @Override
                public void onEvent(int arg0, Bundle arg1) {
                    //setText(textview_debug, "onEvent");
                }

                /*public String getErrorText(int errorCode) {
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
                }*/
            });
        }

        if (RECOGNIZING_STATUS.RECOGNIZING) {
            speechRecognizer.startListening(speechRecognizerIntent);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (VOICE_TEXT.STRING != null) {
                        translate(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
                    }
                }
            },0,800);
        } else {
            speechRecognizer.stopListening();
            stopSelf();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
    }

    public void translate(String t, String src, String dst) {
        translate_api translate = new translate_api();
        translate.setOnTranslationCompleteListener(new translate_api.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {}

            @Override
            public void onCompleted(String translation) {
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        TRANSLATION_TEXT.STRING = translation;
                        if (RECOGNIZING_STATUS.RECOGNIZING) {
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
                };
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onError(Exception e) {
                //Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                //setText(textview_debug, e.getMessage());
            }
        });
        translate.execute(t, src, dst);
    }

    /*public void gtranslate(String t, String src, String dst) {
        google_translate translate = new google_translate();
        translate.setOnTranslationCompleteListener(new google_translate.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {}

            @Override
            public void onCompleted(String translation) {
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        TRANSLATION_TEXT.STRING = translation;
                        if (RECOGNIZING_STATUS.RECOGNIZING) {
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
                };
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onError(Exception e) {
                //toast("Unknown error");
                //setText(textview_debug, e.getMessage());
            }
        });
        translate.execute(t, src, dst);
    }*/

    /*private void toast(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    public void setText(final TextView tv, final String text){
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Any UI task, example
                tv.setText(text);
            }
        };
        handler.sendEmptyMessage(1);
    }

}
