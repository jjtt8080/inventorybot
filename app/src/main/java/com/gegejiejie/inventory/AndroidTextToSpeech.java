package com.gegejiejie.inventory;
import java.util.Locale;
import java.util.UUID;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.support.annotation.Nullable;
import android.util.Log;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

public class AndroidTextToSpeech  {
    private TextToSpeech tts;
    private String txtText;
    //private TextView txtView;
    private Context mContext;
    private static AndroidTextToSpeech instance;
    private String logTag = AndroidTextToSpeech.class.getSimpleName();
    boolean bFinishSpeaking = false;
    private boolean bDestroyed = true;
    private static final String TAG = AndroidTextToSpeech.class.getSimpleName();
    private ChatbotActivity speechDelegate;
    private Object mSyncStart, mSyncStop;
    private boolean IsDestroyed() {return bDestroyed;}
    private void initSpeech(final Context context,  OnInitListener list) {
        tts = new TextToSpeech(context, list);
        tts.setOnUtteranceProgressListener(mProgressListener);
        bDestroyed = false;
    }
    private AndroidTextToSpeech(final Context context,  OnInitListener list, Object syncObjStart, Object syncObjStop) {
        mSyncStart = syncObjStart;
        mSyncStop = syncObjStop;
        initSpeech(context, list);

    }
    private AndroidTextToSpeech(final Context context, final String callingPackage, OnInitListener list, Object syncObjStart, Object syncObjStop) {
        mSyncStart = syncObjStart;
        mSyncStop = syncObjStop;
        initSpeech(context, list);

    }
    public static AndroidTextToSpeech init(final Context context, final String callPackage, OnInitListener list, Object syncObjStart, Object syncObjStop) {
        if (instance == null) {
            instance = new AndroidTextToSpeech(context, callPackage, list, syncObjStart, syncObjStop);
        }
        if (instance.IsDestroyed() == true) {
            instance.initSpeech(context, list);
        }
        return instance;
    }
    public static AndroidTextToSpeech getInstance(Context context, final String callingPackage, OnInitListener list, Object syncObjStart, Object syncObjStop) {
        return init(context, callingPackage, list, syncObjStart, syncObjStop);
    }



    public void destroy() {
        if (!bDestroyed && tts != null) {
            tts.stop();
            tts.shutdown();
            bDestroyed = true;
        }
    }

    public void Stop() {
        // Don't forget to shutdown tts!
        Log.e(TAG, "Stop");
        if (tts != null) {
            destroy();
        }
    }

    public void setText(final String txt) {
        txtText = txt;
    }
    public int setLanguage() {
        int result = tts.setLanguage(Locale.US);
        return result;
    }
    public void speak() {

        Log.e(logTag + " speakOut", txtText);
        final String utteranceId = UUID.randomUUID().toString();
            //txtView.setText(txtText);
        bFinishSpeaking = false;
        tts.speak(txtText, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

    }
    public boolean isDone() {
        synchronized(mSyncStop) {
            return bFinishSpeaking;
        }
    }


    private UtteranceProgressListener mProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            Log.e(TAG + " utteranceProgress", "onStart");

        }

        @Override
        public void onError(String utteranceId) {
            Log.e(TAG + " utteranceProgress", "onError");
            bFinishSpeaking = true;
        }

        @Override
        public void onDone(String utteranceId) {
            Log.e(TAG + " utteranceProgress", "onDone");
            synchronized(mSyncStop) {
                bFinishSpeaking = true;
                mSyncStop.notify();
                Log.e(TAG + " utteranceProgress", "finishNotify");
            }

        }
    };




}
