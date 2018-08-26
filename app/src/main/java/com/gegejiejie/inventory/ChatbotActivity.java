package com.gegejiejie.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.speech.SpeechRecognizer.ERROR_NO_MATCH;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;

public class ChatbotActivity extends AppCompatActivity implements
        OnInitListener, RecognitionListener, View.OnClickListener, AsyncListener {

    private static final int REQUEST_RECORD_PERMISSION = 100;
    public static final int PARTIAL_RESULTS_MAX = 5;
    protected static final int CHATBOT_REQUEST_CODE = 10;
    protected static final String NUM_RECORDS = "NUM_RECORDS";
    private EditText messageET;
    private ListView messagesContainer;
    private ImageView sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    public static final String EXTRA_MESSAGE = "com.gegejiejie.inventory.MESSAGE";
    private TextView txtView;
    private StateObject stateObj;
    private InventoryObject inventoryObj;
    private SpeechRecognizer stt;
    private boolean bFinished = false;
    private boolean bStart = false;
    private ChatbotActivity chatbotActivity;
    private ChatMessage chatMsg;
    private Bundle instanceState;
    private static String TAG = ChatbotActivity.class.getSimpleName();
    private AndroidTextToSpeech mTalker;
    private VoiceRecognizer mListener;
    private List<String> mPartialData;
    private boolean bFinishSpeaking = false;
    Object mSyncStartToken = new Object();
    Object mSyncStopToken = new Object();
    BluetoothConnect connectTask;
    static private SqliteDatabaseObject db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceState = savedInstanceState;
        if (savedInstanceState != null) {
            stateObj = instanceState.getParcelable(StateObject.STATE_OBJECT_TAG);
            inventoryObj = instanceState.getParcelable("INVENTORY_OBJECT");
        } else {
            stateObj = new StateObject();
        }
        setContentView(R.layout.activity_chat);
        initControls();

        inventoryObj = new InventoryObject();
        Log.e( TAG, "onCreate");
        initSpeech();
    }
    @Override
    protected void onSaveInstanceState(Bundle inst) {
        super.onSaveInstanceState(inst);
        inst.putParcelable(StateObject.STATE_OBJECT_TAG, stateObj);
        inst.putParcelable("INVENTORY_OBJECT", inventoryObj);
    }
    private void initSpeech() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions
                    (ChatbotActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_PERMISSION);
        }else {
            mListener = VoiceRecognizer.getInstance(this, this);
        }
        mTalker = AndroidTextToSpeech.getInstance(this, this.getCallingPackage(), this, mSyncStartToken, mSyncStopToken);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.e(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mListener = VoiceRecognizer.getInstance(this, this);
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }
    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (ImageView) findViewById(R.id.btnSpeak);

        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView companionLabel = (TextView) findViewById(R.id.botLbl);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        companionLabel.setText("InventoryBot");// Hard Coded
        loadDummyHistory();

        sendBtn.setOnClickListener(this);
    }
    private void initBluetoothThread() {
        if (connectTask == null) {
            connectTask = new BluetoothConnect(this);
            connectTask.execute("");
        }

    }
    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
        chatMsg = null;
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory(){

        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        adapter = new ChatAdapter(ChatbotActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (chatMsg != null) {
            displayMessage(chatMsg);
        }
        if (!bStart) {
            bStart = true;
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.e("ChatbotActivity", "onStop");
        stopListening();
        mTalker.Stop();
    }

    public void onPause() {
        super.onPause();
        Log.e("ChatbotActivity", "onPause");
        stopListening();
        //mTalker.Stop();

    }
    @Override
    public void onResume() {
        super.onResume();
        if (chatMsg != null) {
            displayMessage(chatMsg);
            Log.e("ChatbotActivity", "displayMessage on Resume");
        }
        if (bStart) {
            bStart = false;
            initSpeech();
            promptSpeech(null, true);
        }
        Log.e("ChatbotActivity", "onResume");
    }

    public void listen(View view) {
        if (bFinished) {
            return;
        }
        Log.e("ChatbotActivity", "listen");

        Intent intent = new Intent(this, ChatbotActivity.class);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (view == null)
        {
            txtView = (TextView) findViewById(R.id.txtResult);
        }
        if (stateObj.getPromptText() != null)
        {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, stateObj.getPromptText());
        }

        if (intent.resolveActivity(getPackageManager()) != null) {
            mListener.startListen(this.getPackageName(), stateObj.getPromptText());
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }
    public void promptSpeech(String promptText, boolean bIsBot)
    {
        if (promptText == null)
        {
            promptText = stateObj.getPromptTextOnState(inventoryObj, stateObj);
        }

        talk(promptText, bIsBot);
    }
    /** Called when the user taps the Send button */
    public void talk(String promptText, boolean bIsBot) {
        if (bFinished) {
            return;
        }

        Log.e("ChatbotActivity:talk", promptText);
        chatMsg = new ChatMessage(false, promptText, bIsBot ? 1 : 0);
        displayMessage(chatMsg);
        /*
        synchronized(mSyncStartToken) {
            mTalker.setText(promptText);
            mSyncStartToken.notify();
        }
        */
        mTalker.setText(promptText);
        mTalker.speak();


    }
    public boolean validateResult(String answerText) {

        boolean bValid = stateObj.storeResultOnState(inventoryObj, answerText);

        if (bValid) {
            if (stateObj.getConfirmedState() == 0) {
                stateObj.setConfirmState(1);
                Log.e(TAG, "validateResult 1");
            }else if (stateObj.getConfirmedState() == 1) {
                stateObj.setConfirmState(0);
                Log.e(TAG, "validateResult 0");
            }
        }
        else if (!bValid && stateObj.getConfirmedState()== 1){
            stateObj.setConfirmState(2);
            Log.e(TAG, "validateResult 2");
        }
        return bValid;
    }
    private void validateResultAndSetState() {
        if (stateObj.getAnswerText() == null || stateObj.getAnswerText().isEmpty()) {
            stateObj.setConfirmState(2);
            Log.e(TAG, "validateResultAndSetState 2");
        } else {
            String answerText = stateObj.getAnswerText();
            chatMsg = new ChatMessage(true, answerText, 0);
            //displayMessage(chatMsg);
            boolean bValid = validateResult(answerText);

            if (!bValid) {
                Log.e("ChatbotActivity", "Invalid input.");
            }
            if (stateObj.getState() == StateObject.FINISH_STATE_CONFIRMED && stateObj.getConfirmedState() == 0) {
                //txtView.setText(inventoryObj.toString());
                // Display the result
                chatMsg = new ChatMessage(false, "Save the data now.....", 0);
                displayMessage(chatMsg);
            }
            if (stateObj.getState() == StateObject.FINISH_STATE_CONFIRMED) {
                bFinished = true;
                stopListening();
                bStart = false;
                if (db == null) {
                    db =  SqliteDatabaseObject.getInstance(this);
                }
                db.Update(inventoryObj);
                String[] queryNumProducts = {SqliteDatabaseObject.HistoryColumns.COLUMN_NAME_DATE, SqliteDatabaseObject.HistoryColumns.COLUMN_NAME_EMPLOYEE_ID,
                        SqliteDatabaseObject.HistoryColumns.COLUMN_NAME_PRODUCT_QUANTITY};
                Cursor cursorObj = db.query(SqliteDatabaseObject.HistoryColumns.TABLE_NAME, queryNumProducts, null);
                int rowCount =  cursorObj.getCount();
                Log.e(TAG, "cursor after update" + String.valueOf(rowCount));

                Intent returnIntent = new Intent();
                returnIntent.putExtra(NUM_RECORDS, inventoryObj.products_list.size());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                return;
            }
        }
    }
    protected void Main() {

        try {

            promptSpeech(null, true);
            initBluetoothThread();

            // check that it is the SecondActivity with an OK result
            synchronized (mSyncStopToken) {
                if (!mTalker.isDone()) {
                    Log.e(TAG + " Main", "wait");
                    mSyncStopToken.wait(5000);
                }
                if (!mTalker.isDone()) {
                    Toast.makeText(this, "Text to Speech has not finished yet!", Toast
                            .LENGTH_SHORT).show();

                }
            }
            Log.e(TAG, String.valueOf(mTalker.isDone()));
            listen(txtView);



        }catch(InterruptedException ex) {
            bStart = false;
            Log.e(TAG, ex.toString());
        }
    }

    public void stopListening() {
        if (mListener != null)
            mListener.stopListening();

    }


    public void onSpeechPartialResults(List<String> results) {
        mPartialData = results;
    }


    public void onSpeechResult(String result) {
        stateObj.setAnswerText(result);
        validateResultAndSetState();
        stopListening();
        Main();


    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "error from server";
            case ERROR_SPEECH_TIMEOUT:
                return "No speech input";

            default:
                return "Didn't understand, please try again.";
        }
    }
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
    }

    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    public void onRmsChanged(float rmsdB) {
        Log.d(TAG, "onRmsChanged");
    }

    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");
    }

    public void onEndOfSpeech() {
        Log.d(TAG, "onEndofSpeech");
    }

    public void onError(int error) {
        Log.d(TAG, "error " + error);

        String errorText = getErrorText(error);
        //txtView = findViewById(R.id.txtMessage);
        //txtView.setText("error " + errorText);
        if (error == ERROR_NO_MATCH || error == ERROR_SPEECH_TIMEOUT) {
            //Check if user typed in something
            String messageText = messageET.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                return;
            }
            else {
                onClick(messageET);
            }
        }
    }

    public void onEvent(int eventType, Bundle params)
    {
        Log.d(TAG, "onEvent " + eventType);
    }
    public void onResults(Bundle results)
    {
        Log.e(TAG, "onResults");
        ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String txt = result.get(0);
        Log.e(TAG + " onResults", txt );
        //txtView = (TextView) findViewById(R.id.meLbl);

        //txtView.setText(txt);
        //This echo what has been set
        onSpeechResult(txt);

    }

    public void onPartialResults(Bundle bundle)
    {
        Log.d(TAG, "onPartialResults");

        mPartialData = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        int k_partialResults = 0;
        String txt = null;
        for (final String result : mPartialData) {

            if (result.isEmpty()) {
            } else {
                k_partialResults ++;
                txt = txt + result;
                if (k_partialResults  == PARTIAL_RESULTS_MAX) {
                    onSpeechPartialResults(mPartialData);
                }

            }
        }

    }
    private String constructPartialResultAsString() {
        String txt = null;
        int k_partialResults = 0;
        for (final String result : mPartialData) {

            if (result.isEmpty()) {
            } else {
                k_partialResults ++;
                txt = txt + result;
                if (k_partialResults  == PARTIAL_RESULTS_MAX) {
                    return txt;
                }

            }
        }
        return txt;
    }
    @Override
    public void onInit(final int status) {
        Log.e(TAG, "onInit");
        if (status == TextToSpeech.SUCCESS) {
            if (!bStart) {
                bStart = true;
                int result = mTalker.setLanguage();
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG,  "This Language is not supported");
                    Toast.makeText(this, "The language is not support", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Thread.sleep(2000);
                }catch(InterruptedException ex) {
                    Log.e(TAG, "Init is interrupted");
                    return;
                }
                Main();
            }

        } else {
            Log.e(TAG, " Initilization Failed!");
        }

    }
    @Override
    protected void onDestroy() {
        //Close the Text to Speech Library
        if(mTalker != null) {

            mTalker.destroy();
            Log.d(TAG, "TTS Destroyed");
        }
        if (connectTask != null) {
            connectTask.cancel(true);

        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        String messageText = messageET.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(122);//dummy
        chatMessage.setMessage(messageText);
        chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatMessage.setMe(true);

        messageET.setText("");

        displayMessage(chatMessage);
        stateObj.setAnswerText(messageText);
        validateResultAndSetState();
        stopListening();
        Main();

    }

    @Override
    public void processResult(String output) {
        if (messageET != null && output != null && output.length() > 0) {
            String originalText = messageET.getText().toString();
            if (originalText.isEmpty()) {
                messageET.setText(output);
            }else {
                messageET.setText(originalText + output);

            }
            onClick(messageET);
        }
    }
}