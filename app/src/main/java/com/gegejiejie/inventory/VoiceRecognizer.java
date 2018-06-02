package com.gegejiejie.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VoiceRecognizer
{
    private static final String TAG = VoiceRecognizer.class.getSimpleName();
    private static final int REQUEST_RECORD_PERMISSION = 100;

    private SpeechRecognizer sr;

    private TextView txtView;
    private ImageView speakButton;
    private StateObject stateObj;
    private Intent recognizer_intent;
    private Intent input_intent;
    boolean keep = true;
    boolean bStartListen = false;

    private static VoiceRecognizer instance;

    private Context mContext;
    private long mLastActionTimestamp;
    private ArrayList<String> mPartialData;
    private static final long mStopListeningDelayInMs = 10000;
    private static final long mTransitionMinimumDelay = 1200;
    private RecognitionListener mListener;
    private VoiceRecognizer(final Context context, RecognitionListener list)
    {
        //speakButton = (ImageView) findViewById(R.id.btnSpeak);
        //speakButton.setOnClickListener(this))
        mPartialData = new ArrayList<String>();
        mListener = list;
        initSpeechRecognizer(context, list);


    }
    private void initSpeechRecognizer(final Context context, RecognitionListener list) {
        if (context == null)
            throw new IllegalArgumentException("context must be defined!");

        mContext = context;

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (sr != null) {
                try {
                    sr.destroy();
                } catch (final Throwable exc) {
                    Log.e(TAG,
                            "Non-Fatal error while destroying speech. " + exc.getMessage());
                } finally {
                    sr = null;
                }
            }

            sr = SpeechRecognizer.createSpeechRecognizer(context);
            sr.setRecognitionListener(list);
            //initDelayedStopListening(context);

        } else {
            sr = null;
        }

        mPartialData.clear();
        //mUnstableData = null;
    }
    public static VoiceRecognizer getInstance(final Context context, RecognitionListener list) {
        if (instance == null) {
            instance = new VoiceRecognizer(context, list);
        }
        return instance;
    }

    public void startListen(String callPackageName, String promptText) {
        Log.e(TAG,  "startListen");
        if (bStartListen) {
            return;
        }

        recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, callPackageName);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        //recognizer_intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promptText);
        try {
            sr.startListening(recognizer_intent);
        } catch(final SecurityException ex) {
            throw ex;
        }

        bStartListen = true;
        updateLastActionTimestamp();

    }
    private String constructPartialResultAsString() {
        String txt = null;
        int k_partialResults = 0;
        for (final String result : mPartialData) {

            if (result.isEmpty()) {
            } else {
                k_partialResults ++;
                txt = txt + result;
                if (k_partialResults  == ChatbotActivity.PARTIAL_RESULTS_MAX) {
                    return txt;
                }

            }
        }
        return txt;
    }
    public String returnPartialResultsAndRecreateSpeechRecognizer() {
        bStartListen = false;
        String partialResult = constructPartialResultAsString();
        initSpeechRecognizer(mContext, mListener);
        return partialResult;
    }

    public boolean isListening() {
        if (sr == null) return false;
        return bStartListen;
    }

    /**
     * Stops voice recognition listening.
     * This method does nothing if voice listening is not active
     */
    public void stopListening() {
        Log.e(TAG,  "stopListening");
        if (!bStartListen) return;

        if (throttleAction()) {
            Log.e(TAG, "Calm down! Throttling stop to prevent disaster!");
            try {
                Thread.sleep(mTransitionMinimumDelay);
            }catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        sr.stopListening();
        bStartListen = false;
        updateLastActionTimestamp();
        returnPartialResultsAndRecreateSpeechRecognizer();
    }

    private void updateLastActionTimestamp() {
        mLastActionTimestamp = new Date().getTime();
    }
    private boolean throttleAction() {
        return (new Date().getTime() <= (mLastActionTimestamp + mTransitionMinimumDelay));
    }

}

