package com.example.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class SpeechToTextService {
    public interface OnSpeechRecognizedListener {
        void onPartialResults(String text);

        void onResults(String text);
    }

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private OnSpeechRecognizedListener listener;
    private Context context;

    public SpeechToTextService(Context context, OnSpeechRecognizedListener listener) {
        this.context = context;
        this.listener = listener;
        initRecognizer();
    }

    private void initRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                java.util.ArrayList<String> matches =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    listener.onPartialResults(matches.get(0));
                }
            }

            @Override
            public void onResults(Bundle results) {
                java.util.ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    listener.onResults(matches.get(0));
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL");
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    public void startRecognition() {
        speechRecognizer.startListening(speechIntent);
    }

    public void stopRecognition() {
        // stopListening() tells Android to deliver final onResults(...)
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }
}