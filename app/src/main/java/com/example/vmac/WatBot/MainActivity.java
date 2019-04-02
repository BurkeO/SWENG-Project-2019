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
import android.os.CountDownTimer;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vmac.chatbot.home_screen;
import com.example.vmac.chatbot.results;
import com.example.vmac.chatbot.results_2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
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

    //Firebase listeners
    ValueEventListener mCurrentGameStatusListener;
    ValueEventListener mAvailableGameListener;
    ChildEventListener mChatRoomMessageListener;

    //firebase realtime database
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;


    //reference to a game obect
    //private TuringGame currentGame;
    private boolean isHumanGame;
    private boolean gameJoined;
    private String gameState;
    private int messageNum;
    private String myId;
    private String chatRoomId;
    private int gameStatus;
    private static final int GAME_NOT_ACTIVE = 1;
    private static final int GAME_ACTIVE = 5;
    private static final int GAME_STOPPED = 5;

    //UI elements
    private ProgressBar mProgressBar;
    private Button mTimerStopButton;
    private TextView mTimerTime;
    //Timer
    private CountDownTimer mCountDownTimer;
    private final long gameLength = 5 * 60000; // fix game length at 5 minutes
    private long mTimeLeft;
    private boolean mTimerRunning;

    private boolean guessedRight;

    /**
     * Method to be called when activity is created
     * created:
     * last modified : 14/03/2019 by J.Cistiakovas - added functionality for the timer.
     * modified : 07/03/2019 by J.Cistiakovas - added a progress bar that initially appears on
     *      the screen. Added a thread that performs matchmaking and at the end hides the progress bar.
     *      Added checks to ensure that send/record buttons can only be used when game has started.
     * modified : 07/03/2019 by J.Cistiakovas - added a function call to matchmaking
     * modified : 22/02/2019 by J.Cistiakovas - added database listener
     * modified: 21/02/2019 by J.Cistiakovas - added anonymous sign in functionality
     * modified: 11/03/2019 - 24/03/2019 by C.Coady - added matchmaking functionality along with some
     *                                                  tweaks to the database structure.
     *                                              - added funcitonality so all messages are pushed
     *                                              to firebase for both human and bot games.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate the layout
        setContentView(R.layout.activity_main);

        //buttons used to guess if human or bot
        Button guessButton1 = findViewById(R.id.human);
        Button guessButton2 = findViewById(R.id.bot);

        mContext = getApplicationContext();
        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        btnRecord = findViewById(R.id.btn_record);
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);
        recyclerView = findViewById(R.id.recycler_view);
        mTimerTime = findViewById(R.id.timerTime);

        messageArrayList = new ArrayList<>();
        //mAdapter = new ChatAdapter(messageArrayList,myId);
        microphoneHelper = new MicrophoneHelper(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        mProgressBar = findViewById(R.id.progressBar);

        recyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        gameStatus = GAME_NOT_ACTIVE;
        //initiate game parameters
        mTimerRunning = false;
        messageNum = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                matchmaking();  //find an opponent
                initialRequest = true;
                createWatsonServices(); //create text-to-speech and voice-to-text services
                if (isHumanGame) {
                    createFirebaseServices();
                } else {
                    initialRequest = false; // set it randomly, it determines who starts the conversation
                    createWatsonAssistant();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(MainActivity.this == null)
                            return;
                        mProgressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        mAdapter = new ChatAdapter(messageArrayList, myId);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        gameStatus = GAME_ACTIVE;



                    }
                });
                //start the timer
                //TODO: synchronise time
                startTimer();
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
                if (checkInternetConnection() && gameStatus == GAME_ACTIVE) {
                    sendMessage();
                }
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameStatus == GAME_ACTIVE) {
                    recordMessage();
                }
            }
        });
        //sendMessage();

        //timer listener - DETECTING WHEN USER GUESSES HUMAN
        guessButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHumanGame){
                    guessedRight = true;
                }else{
                    guessedRight = false;
                }
                timerStartStop();
            }
        });

        //timer listener - DETECTING WHEN USER GUESSES BOT
        guessButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isHumanGame){
                    guessedRight = true;
                }else{
                    guessedRight = false;
                }
                timerStartStop();
            }
        });
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
                    showToast("Permission to record audio denied", Toast.LENGTH_SHORT);
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
    private void sendMessage() {
        if (isHumanGame) {
            sendMessageHuman();
        } else {
            sendMessageBot();
        }
    }

    /**
     * Method that sends a message to a human play via Firebase realtime databse
     * created: 22:00 23/03/2019 by J.Cistiakovas
     * last modified: 19:00 24/03/2019 by C.Coady - updated messages to include new type
     * attribute. This denotes if the message was sent by a human or a bot.
     */
    private void sendMessageHuman() {
        //create a new TuringMessage object using values from the editText box
        String id = mAuth.getUid() + (new Integer(messageNum++).toString());
        Message message = new Message();
        message.setMessage(this.inputMessage.getText().toString().trim());
        message.setId(id);
        message.setSender(myId);
        message.setType("human");

        //return if message is empty
        if (message.getMessage().equals("")) {
            return;
        }
        //publish the message in an chatRooms
        mDatabaseRef.child("chatRooms").child(chatRoomId).child(message.getId()).setValue(message);
        // add a message object to the list
        messageArrayList.add(message);
        this.inputMessage.setText("");
        // make adapter to update its view and add a new message to the screen
        //mAdapter.notifyDataSetChanged();
        new SayTask().execute(message.getMessage());
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
            inputMessage.setType("human");
            messageArrayList.add(inputMessage);
        } else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            inputMessage.setSender(myId);
            inputMessage.setType("human");
            this.initialRequest = false;
            showToast("Tap on the message for Voice", Toast.LENGTH_LONG);

        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();
        scrollToMostRecentMessage();
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
            showToast("Listening....Click to Stop", Toast.LENGTH_LONG);

        } else {
            try {
                microphoneHelper.closeInputStream();
                listening = false;
                showToast("Stopped Listening....Click to Start", Toast.LENGTH_LONG);
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
            showToast(" No Internet Connection available ", Toast.LENGTH_LONG);
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
     * last modified: 20/03/2019 by J.Cistiakovas - fixed NullPointerException
     */
    private void scrollToMostRecentMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                if(MainActivity.this == null || mAdapter == null)
                    return;
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                    Log.d(TAG, "Scrolled to the most recent message!");
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
    private void createWatsonAssistant() {
        watsonAssistant = new Assistant("2018-11-08", new IamOptions.Builder()
                .apiKey(mContext.getString(R.string.assistant_apikey))
                .build());
        watsonAssistant.setEndPoint(mContext.getString(R.string.assistant_url));
        myId = "100";
    }

    /**
     * Method to initialise Firebase services, such as Auth and Realtime Database
     * created: 04/03/2019 by J.Cistiakovas
     * last modified: 24/03/2019 by C.Coady - removed message listener as this is handled in the
     * loadMessages method
     */
    private void createFirebaseServices() {
        //Firebase anonymous Auth
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        //listener that listens for change in the Auth state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                //check if user is already signed in
                //TODO: retrieve/reset local information from the memory, e.g. score
                if (currentUser == null) {
                    mAuth.signInAnonymously().addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mCurrentUser = firebaseAuth.getCurrentUser();
                                showToast("Hello, " + mCurrentUser.getUid(), Toast.LENGTH_LONG);
                            } else {
                                showToast("Authentication failed!", Toast.LENGTH_LONG);
                                //TODO: fail the program or do something here
                            }
                        }
                    });
                } else {
                    //user is signed in - happy days
                    mCurrentUser = currentUser;
                    showToast("Hello, " + currentUser.getUid(), Toast.LENGTH_LONG);
                    Log.d(TAG, "User already signed in. User id : " + mCurrentUser.getUid());

                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        myId = mAuth.getUid();

        //Firebase realtime database initialisation
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();
    }

    /**
     * This method starts a listener on the game state, if the game
     * is complete it will update the local gameState variable which
     * allows us to change to a guessing screen etc.
     * created: 11/03/2019 by C.Coady
     * last modified: 24/03/2019 by C.Coady
     */
    private void gameState(){
        //Get a reference to the availableGames section of the database
        final DatabaseReference currentGameRef = mDatabaseRef.child("availableGames");
        //Create a new listener to listen for changes in the game state
        mCurrentGameStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if we are currently in a game
                if(gameJoined && chatRoomId != null){
                    //Take a snapshot of the current game state from the database
                    gameState = dataSnapshot.child(chatRoomId).getValue().toString();
                    if(gameState.equals("complete")){
                        //make the user guess now
                        showToast("the game is now over", Toast.LENGTH_LONG);
                        stopTimer();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        //Attach the listener to the database reference
        currentGameRef.addValueEventListener(mCurrentGameStatusListener);
    }



    /**
     * This method handles all of the matchmaking for the game.
     * When a game starts, the user will check to see if there are any 'empty'
     * chatrooms. If there are, the user will join (this is a human to human game) and
     * a listener will be started on that chatroom to check for new messages.
     * If no chatrooms are available the user will create one and wait to see if someone
     * joins. If no one joins the user will be matched with a bot and the game will start.
     * created: 11/03/2019 by C.Coady
     * last modified: 24/03/2019 by C.Coady
     */
    private void matchmaking() {
        //initialse the firebase database
        createFirebaseServices();
        //create text-to-speech and voice-to-text services
        createWatsonServices();
        isHumanGame = true;
        //mDatabaseRef = mDatabase.getReference();
        final DatabaseReference availableGameRef = mDatabaseRef.child("availableGames");
        mAvailableGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot availableGame) {
                for (DataSnapshot game: availableGame.getChildren()) {
                    String key = game.getKey();
                    String status = game.getValue().toString();
                    if(status.equals("empty") && !gameJoined){
                        gameJoined = true;
                        chatRoomId = key;
                        showToast("Joined game " + chatRoomId, Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //error loading from the database
            }
        };
        availableGameRef.addListenerForSingleValueEvent(mAvailableGameListener);
        //start a listener to see if the game has ended
        gameState();
        //wait for a bit to read from the database
        double currentWaitTime = 0;
        double delay = 1000000000.0;
        while(!gameJoined && currentWaitTime < delay){
            currentWaitTime++;
        }
        //we have not found a game
        if(!gameJoined){
            //create a game
            createGame();
            showToast("Created game " + chatRoomId, Toast.LENGTH_LONG);
            //update game status
            gameJoined = true;
            //wait for a bit to see if anyone joins the game
            for(double playerWait = 0; playerWait < delay*2; playerWait++){}
            if(gameState.equals("empty")){
                //make this a bot game
                isHumanGame = false;
                createWatsonAssistant();
                showToast("This is a game against a bot!", Toast.LENGTH_LONG);
                Log.d(TAG, "This is a game against a bot!");
                availableGameRef.child(chatRoomId).setValue("full");

                if (Math.random() < 0.5) {
                    initialRequest = false;
                }
                else{
                    initialRequest = true;
                }
            }
        }
        else{
            //join the game and set it to full
            showToast("This is a game against a human!", Toast.LENGTH_LONG);
            Log.d(TAG, "This is a game against a human!");
            //make the game session full
            availableGameRef.child(chatRoomId).setValue("full");
        }
        //load message listener
        loadMessages();
    }

    /**
     * This method starts a listener on the chatRoom in the database.
     * When ever a message is added to the chatRoom the listener will
     * add the new message to the arrayList of messages so it can be
     * displayed on screen
     * created: 11/03/2019 by C.Coady
     * last modified: 23/03/2019 by C.Coady
     */
    private void loadMessages(){
        if(isHumanGame){
            DatabaseReference messageRef = mDatabaseRef.child("chatRooms");
            mChatRoomMessageListener = new ChildEventListener() {
                // TODO: not load previous conversation, possibly use timestamps
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //retrieve the message from the datasnapshot
                    Message newMessage = dataSnapshot.getValue(Message.class);
                    //TODO: deal with double messages, sould not be much of  a problem if we start a new chat each time
                    if (TextUtils.equals(newMessage.getSender(), mAuth.getUid())) {
                        //don't add own message
                        return;
                    }
                    messageArrayList.add(newMessage);
                    //mAdapter.notifyDataSetChanged();
                    scrollToMostRecentMessage();
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            messageRef.child(chatRoomId).addChildEventListener(mChatRoomMessageListener);
        }
    }

    /**
     * This method loops through the arrayList of messages and uploads each one
     * to the chat room corresponding to 'chatRoomId' on the database.
     * created: 23/03/2019 by C.Coady
     * last modified: 24/03/2019 by C.Coady
     */
    private void uploadMessages(){
        //get a reference to the chat rooms section of the database
        DatabaseReference chatRef = mDatabaseRef.child("chatRooms").child(chatRoomId);

        //loop through messages and upload them to the chat
        for(int i = 0; i < messageArrayList.size(); i++){
            chatRef = chatRef.push();
            Message message = (Message) messageArrayList.get(i);
            message.setId(chatRef.getKey());
            //publish the message in an chatRooms
            chatRef.setValue(message);
            chatRef = mDatabaseRef.child("chatRooms").child(chatRoomId);
        }
    }

    /**
     * This method takes creates an availableGame which players can join.
     * A random game key is generated by firebase which is used to identify
     * the game 'chatRoomId'.
     * The status of the new chatroom is set to empty while we wait for
     * other players to join the game.
     * created: 21/03/2019 by C.Coady
     * last modified: 24/03/2019 by C.Coady
     */
    private boolean createGame(){
        //get a reference to the chat rooms section of the database
        DatabaseReference chatRef = mDatabaseRef.child("availableGames");
        //create a new chatroom with a unique reference
        chatRef = chatRef.push();
        //update the chatroom id to the newly generated one
        chatRoomId = chatRef.getKey();
        //make this chatroom empty
        chatRef.setValue("empty");
        //TODO add some error checking if we fail to connect to the database
        return true;
    }

    /**
     * This method sets the gameState to 'complete' on the availableGames section
     * of the database and then uploads the messages to the database if the game
     * was against a bot those messages are only stored locally.
     * created: 21/03/2019 by C.Coady
     * last modified: 24/03/2019 by C.Coady
     */
    private boolean endGame(){
        //get a reference to the chat rooms section of the database
        DatabaseReference chatRef = mDatabaseRef.child("availableGames");
        //make this chatroom complete
        chatRef.child(chatRoomId).setValue("complete");
        //if the game is against a bot, push messages to the database
        if(!isHumanGame){
            uploadMessages();
        }
        //TODO add some error checking if we fail to connect to the database
        return true;
    }

    /**
     * Method that makes a Toast via a UI thread
     * created: 11/03/2019 by J.Cistiakovas
     * last modified: 20/03/2019 by J.Cistiakovas - fixed NullPointerException
     */
    private void showToast(final String string, final int duration) {
        runOnUiThread(new Runnable() {
            public void run() {
                if(MainActivity.this == null)
                    return;
                Toast.makeText(mContext, string, duration).show();
            }
        });
    }

    /**
     * Logic for the timer
     * created: 14/03/2019 by J.Cistiakovas
     * last modified: 14/03/2019 by J.Cistiakovas
     */
    //TODO: save the score
    private void timerStartStop(){
        if(mTimerRunning && gameStatus==GAME_ACTIVE){
            //stop timer
            stopTimer();
        }
        else {
            //not running, start the timer
            //set up the timer
            startTimer();
        }
    }

    /**
     * Method creates and starts a new timer
     * created: 14/03/2019 by J.Cistiakovas
     * last modified: 20/03/2019 by J.Cistiakovas - fixed NullPointerException
     */
    private void startTimer(){
        //set up the timer
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(MainActivity.this == null)
                    return;
                mCountDownTimer = new CountDownTimer(gameLength,1000) {
                    @Override
                    public void onTick(long l) {
                        mTimeLeft = l;
                        updateTime();
                    }

                    @Override
                    public void onFinish() {
                        stopTimer();

                    }
                };
                mCountDownTimer.start();
            }
        });
        mTimerRunning = true;
        Log.d(TAG,"New timer started!");
    }

    /**
     * Updates the textField showing the time left
     * created: 14/03/2019 by J.Cistiakovas
     * last modified: 14/03/2019 by J.Cistiakovas
     */
    private void updateTime(){
        //change the string
        int minutes = (int) mTimeLeft / 60000;
        int seconds = (int) (mTimeLeft % 60000) / 1000;
        String timeLeftText;
        timeLeftText = String.format("%02d:%02d",minutes, seconds);
        mTimerTime.setText(timeLeftText);
    }

    /**
     * Stops the timer and updates the game state
     * created: 14/03/2019 by J.Cistiakovas
     * last modified: 20/03/2019 by L.Brennan
     * modified: 14/03/2019 by J.Cistiakovas
     */
    //TODO: add actions to be done as the game is stopped/ended
    private void stopTimer() {
        //stop the game and move to other activity - results being displayed

        //change the states
        mCountDownTimer.cancel();
        gameStatus = GAME_STOPPED;
        mTimerRunning = false;
        showToast("Timer stop pressed", Toast.LENGTH_SHORT);
        Log.d(TAG,"Timer stop pressed.");

        //Takes to results screen saying if it was a bot or human
        if(isHumanGame)  //if human
        {
            Intent human_results_intent = new Intent(this, results_2.class);

            //pass the time it took the user to complete the game into results
            int minutes = (int) (mTimeLeft / 60000);
            int seconds = (int) (mTimeLeft % 60000) / 1000;
            minutes = 4 - minutes;              //get time taken by subtracting
                                               // time left from time elapsed

            seconds = 60 - seconds;
            String timeLeftText;
            timeLeftText = String.format("%02d mins : %02d seconds",minutes, seconds);
            human_results_intent.putExtra("timeTaken",timeLeftText);
            human_results_intent.putExtra("guessedRight", guessedRight);
            endGame();
            detachListeners();
            startActivity(human_results_intent);
        }
        else //if bot
        {
            Intent bot_results_intent = new Intent(this, results.class);

            //pass the time it took the user to complete the game into results
            int minutes = (int) (mTimeLeft / 60000);
            int seconds = (int) (mTimeLeft % 60000) / 1000;
            minutes = 4 - minutes;              //get time taken by subtracting
                                                // time left from time elapsed
            seconds = 60 - seconds;
            String timeLeftText;
            timeLeftText = String.format("%02d mins : %02d seconds",minutes, seconds);
            bot_results_intent.putExtra("timeTaken",timeLeftText);
            bot_results_intent.putExtra("guessedRight", guessedRight);
            endGame();
            detachListeners();
            startActivity(bot_results_intent);
        }
    }

    private void detachListeners(){

        if(mCurrentGameStatusListener != null) {
            mDatabaseRef.child("availableGames").removeEventListener(mCurrentGameStatusListener);
        }
        if(mAvailableGameListener != null) {
            mDatabaseRef.child("availableGames").removeEventListener(mAvailableGameListener);
        }
        if(mChatRoomMessageListener != null) {
            mDatabaseRef.child("chatRooms").removeEventListener(mChatRoomMessageListener);
        }
    }

}



