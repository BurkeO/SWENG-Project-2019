package com.example.vmac.WatBot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.assistant.v2.Assistant;
import com.ibm.watson.developer_cloud.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageInput;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant.v2.model.SessionResponse;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private ImageButton btnRecord;
    StreamPlayer streamPlayer = new StreamPlayer();
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;
    private boolean listening = false;
    private MicrophoneInputStream capture;
    private Context mContext;
    private MicrophoneHelper microphoneHelper;

    private Assistant watsonAssistant;
    private SessionResponse watsonAssistantSession;
    private SpeechToText speechService;
    private TextToSpeech textToSpeech;

    //Firebase atributes
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //firebase realtime database
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;


    //reference to a game obect
    //private TuringGame currentGame;
    private boolean isHumanGame;
    private int messageNum;
    private String myId;
    private int gameStatus;
    private static final int GAME_NOT_ACTIVE = 1;
    private static final int GAME_ACTIVE = 5;


    //UI elements
    private ProgressBar progressBar;

    /**
     *  Method to be called when activity is created
     * created:
     * last modified : 07/03/2019 by J.Cistiakovas - added a progress bar that initially appears on
     *      the screen. Added a thread that performs matchmaking and at the end hides the progress bar.
     *      Added checks to ensure that send/record buttons can only be used when game has started.
     * modified : 07/03/2019 by J.Cistiakovas - added a function call to matchmaking
     * modified : 22/02/2019 by J.Cistiakovas - added database listener
     * modified: 21/02/2019 by J.Cistiakovas - added anonymous sign in functionality
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate the layout
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        btnRecord = findViewById(R.id.btn_record);
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);
        recyclerView = findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();
        //mAdapter = new ChatAdapter(messageArrayList,myId);
        microphoneHelper = new MicrophoneHelper(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);


        gameStatus = GAME_NOT_ACTIVE;
        //initiate game parameters
        //TODO: change the initialisation
        messageNum = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                matchMaking();  //find an opponent
                initialRequest = true;
                createWatsonServices(); //create text-to-speech and voice-to-text services
                if(isHumanGame){
                    createFirebaseServices();
                }else{
                    initialRequest = false; // set it randomly, it determines who starts the conversation
                    createWatsonAssistant();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        mAdapter = new ChatAdapter(messageArrayList,myId);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        gameStatus = GAME_ACTIVE;
                    }
                });
            }
        }).start();
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        } else {
            Log.i(TAG, "Permission to record was already granted");
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Message audioMessage = (Message) messageArrayList.get(position);
                if (audioMessage != null && !audioMessage.getMessage().isEmpty()) {
                    new SayTask().execute(audioMessage.getMessage());
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                recordMessage();

            }
        }));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //only send the message if game is active
                if (checkInternetConnection() && gameStatus==GAME_ACTIVE) {
                    sendMessage();
                }
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameStatus==GAME_ACTIVE) {
                    recordMessage();
                }
            }
        });
        //TODO: find out why it is necessary to send an empty initial message?
        //sendMessage();
    }

    ;

    /**
     * Method to be called when activity is started
     * created: 15:00 22/03/2019 by J.Cistiakovas
     * last modified: -
     */
    @Override
    public void onStart() {
        super.onStart();
        //initiate sign in check
        //mAuthListener.onAuthStateChanged(mAuth);
    }

    // Speech-to-Text Record Audio permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case RECORD_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }

            case MicrophoneHelper.REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // if (!permissionToRecordAccepted ) finish();

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MicrophoneHelper.REQUEST_PERMISSION);
    }

    /**
     * Method to be called to send a message
     * created: 23/02/2019 by J.Cistiakovas
     * last modified: 23/02/2019 by J.Cistiakovas
     */
    private  void sendMessage(){
        if(isHumanGame){
            sendMessageHuman();
        }else{
            sendMessageBot();
        }
    }

    /**
     * Method that sends a message to a human play via Firebase realtime databse
     * created: 22:00 23/03/2019 by J.Cistiakovas
     * last modified: 22:00 23/03/2019 by J.Cistiakovas
     */
    private void sendMessageHuman(){
        //create a new TuringMessage object using values from the editText box
        String id = mAuth.getUid() + (new Integer(messageNum++).toString());
        Message message = new Message();
        message.setMessage(this.inputMessage.getText().toString().trim());
        message.setId(id);
        message.setSender(myId);

        //return if message is empty
        if(message.getMessage().equals("")){
            return;
        }
        //publish the message in an openchat
        mDatabaseRef.child("openchat").child(message.getId()).setValue(message);
        // add a message object to the list
        messageArrayList.add(message);
        this.inputMessage.setText("");
        // make adapter to update its view and add a new message to the screen
        //mAdapter.notifyDataSetChanged();
        scrollToMostRecentMessage();
    }

    // Sending a message to Watson Assistant Service
    private void sendMessageBot() {
        final String inputmessage = this.inputMessage.getText().toString().trim();
        if (!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            inputMessage.setSender(myId);
            messageArrayList.add(inputMessage);
        } else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            inputMessage.setSender(myId);
            this.initialRequest = false;
            Toast.makeText(getApplicationContext(), "Tap on the message for Voice", Toast.LENGTH_LONG).show();

        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (watsonAssistantSession == null) {
                        ServiceCall<SessionResponse> call = watsonAssistant.createSession(
                                new CreateSessionOptions.Builder().assistantId(mContext.getString(R.string.assistant_id)).build());
                        watsonAssistantSession = call.execute();
                    }

                    MessageInput input = new MessageInput.Builder()
                            .text(inputmessage)
                            .build();
                    MessageOptions options = new MessageOptions.Builder()
                            .assistantId(mContext.getString(R.string.assistant_id))
                            .input(input)
                            .sessionId(watsonAssistantSession.getSessionId())
                            .build();

                    //blocking statement
                    MessageResponse response = watsonAssistant.message(options).execute();
                    Log.i(TAG, "run: " + response);
                    final Message outMessage = new Message();
                    if (response != null &&
                            response.getOutput() != null &&
                            !response.getOutput().getGeneric().isEmpty() &&
                            "text".equals(response.getOutput().getGeneric().get(0).getResponseType())) {
                        outMessage.setMessage(response.getOutput().getGeneric().get(0).getText());
                        outMessage.setId("2");

                        messageArrayList.add(outMessage);

                        // speak the message
                        new SayTask().execute(outMessage.getMessage());
                        scrollToMostRecentMessage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    private class SayTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            streamPlayer.playStream(textToSpeech.synthesize(new SynthesizeOptions.Builder()
                    .text(params[0])
                    .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
                    .accept(SynthesizeOptions.Accept.AUDIO_WAV)
                    .build()).execute());
            return "Did synthesize";
        }
    }

    //Record a message via Watson Speech to Text
    private void recordMessage() {
        if (listening != true) {
            capture = microphoneHelper.getInputStream(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(getRecognizeOptions(capture), new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {
                        showError(e);
                    }
                }
            }).start();
            listening = true;
            Toast.makeText(MainActivity.this, "Listening....Click to Stop", Toast.LENGTH_LONG).show();

        } else {
            try {
                microphoneHelper.closeInputStream();
                listening = false;
                Toast.makeText(MainActivity.this, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Check Internet Connection
     *
     * @return
     */
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions(InputStream audio) {
        return new RecognizeOptions.Builder()
                .audio(audio)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

    //Watson Speech to Text Methods.
    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                showMicText(text);
            }
        }

        @Override
        public void onError(Exception e) {
            showError(e);
            enableMicButton();
        }

        @Override
        public void onDisconnected() {
            enableMicButton();
        }

    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputMessage.setText(text);
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnRecord.setEnabled(true);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    /**
     * Method that scrolls the recycler view to the most recent message
     * created: 26/02/2019 by J.Cistiakovas
     * last modified: 26/02/2019 by J.Cistiakovas
     */
    private void scrollToMostRecentMessage(){
        runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                    Log.d(TAG,"Scrolled to the most recent message!");
                }

            }
        });
    }

/*    public void onPause() {

        super.onPause();
    }*/


    /**
     * Method to initialise all the objects required for Watson Services to work,
     * but assistant s not initialised
     * created: -
     * last modified: 07/03/2019 by J.Cistiakovas
     */
    private void createWatsonServices() {
        textToSpeech = new TextToSpeech();
        textToSpeech.setIamCredentials(new IamOptions.Builder()
                .apiKey(mContext.getString(R.string.TTS_apikey))
                .build());
        textToSpeech.setEndPoint(mContext.getString(R.string.TTS_url));

        speechService = new SpeechToText();
        speechService.setIamCredentials(new IamOptions.Builder()
                .apiKey(mContext.getString(R.string.STT_apikey))
                .build());
        speechService.setEndPoint(mContext.getString(R.string.STT_url));
    }

    /**
     * Method to initialise Watson assistant
     * created: -
     * last modified: 07/03/2019 by J.Cistiakovas
     */
    private void createWatsonAssistant(){
        watsonAssistant = new Assistant("2018-11-08", new IamOptions.Builder()
                .apiKey(mContext.getString(R.string.assistant_apikey))
                .build());
        watsonAssistant.setEndPoint(mContext.getString(R.string.assistant_url));
        myId = "100";
    }

    /**
     * Method to initialise Firebase services, such as Auth and Realtime Database
     * created: 04/03/2019 by J.Cistiakovas
     * last modified: 07/03/2019 by J.Cistiakovas
     */
    private void createFirebaseServices(){
        //Firebase anonymous Auth
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        //listener that listens for change in the Auth state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                //check if user is already signed in
                //TODO: retrieve/reset local information from the memory, e.g. score
                if(currentUser == null){
                    mAuth.signInAnonymously().addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                mCurrentUser = firebaseAuth.getCurrentUser();
                                Toast.makeText(mContext,"Hello, " + mCurrentUser.getUid(), Toast.LENGTH_LONG).show();
                            } else{
                                Toast.makeText(mContext,"Authentication failed!", Toast.LENGTH_LONG).show();
                                //TODO: fail the program or do something here
                            }
                        }
                    });
                } else {
                    //user is signed in - happy days
                    mCurrentUser = currentUser;
                    Toast.makeText(mContext,"Hello, " + currentUser.getUid(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "User already signed in. User id : " + mCurrentUser.getUid());

                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        myId = mAuth.getUid();

        //Firebase realtime database initialisation
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();
        mDatabaseRef.child("openchat").addChildEventListener(new ChildEventListener() {
            // TODO: not load previous conversation, possibly use timestamps
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(initialRequest){
                    initialRequest = false;
                    return;
                }
                //retrieve the message from the datasnapshot
                Message newMessage = dataSnapshot.getValue(Message.class);
                //TODO: deal with double messages, sould not be much of  a problem if we start a new chat each time
                if(TextUtils.equals(newMessage.getSender(),mAuth.getUid())){
                    //don't add own message
                    return;
                }
                messageArrayList.add(newMessage );
                //mAdapter.notifyDataSetChanged();
                scrollToMostRecentMessage();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method that deals with matchmaking and assigning opponent to the user
     * created:
     * last modified:
     */
    private void matchMaking(){

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(Math.random() > 0.1) {
            isHumanGame = false;
            Log.d(TAG, "This is a game against bot!");
        }else{
            isHumanGame = true;
            Log.d(TAG, "This is a game against human!");
        }

    }
}



