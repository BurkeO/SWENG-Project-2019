package com.example.vmac.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vmac.WatBot.MainActivity;
import com.example.vmac.WatBot.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class home_screen extends AppCompatActivity implements View.OnClickListener {

    //Use to test if something clicked, text appears:
    // Toast.makeText(this, "this was clicked", Toast.LENGTH_SHORT).show();
    private static String TAG = "home_screen";
    static final String SCORE_DIR = "score";
    private int mScore;
    private TextView mScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //create the buttons on home screen
        Button button1 = findViewById(R.id.new_game_button);
        Button button2 = findViewById(R.id.contact_button);
        Button button3 = findViewById(R.id.settings_button);

        //set onClickListener for all buttons
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        mScoreTextView = findViewById(R.id.scoreHomeScreen);
        mScore = getScore();
        mScoreTextView.setText(new Integer(mScore).toString());
    }

    //methods bringing user to screen depending on which button they clicked
    public void openNewGame() {
        Intent new_game_intent = new Intent(this, MainActivity.class);
        startActivity(new_game_intent);
    }

    public void openContactScreen() {
        Intent contact_intent = new Intent(this, contact.class);
        startActivity(contact_intent);
    }

    public void openSettingsScreen() {
        Intent settings_intent = new Intent(this, settings.class);
        startActivity(settings_intent);
    }

    @Override
    public void onClick(View v) {
        //switch statement checks which button was clicked on click
        switch (v.getId()) {
            case R.id.new_game_button:
                openNewGame();
                break;
            case R.id.contact_button:
                openContactScreen();
                break;
            case R.id.settings_button:
                openSettingsScreen();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update the score on resume
        mScore = getScore();
        mScoreTextView.setText(new Integer(mScore).toString());
    }

    /**
     * Reads and returns a score from the local file specified by path SCORE_DIR
     * created: 23/03/2019 by J.Cistiakovas
     * last modified: 23/03/2019 by J.Cistiakovas
     */
    private int getScore() {
        int score = -1;
        File file = new File(getFilesDir() + "/" + SCORE_DIR);
        if (!file.exists()) {
            Log.d(TAG, "Score file does not exist");
            //create a new file
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(SCORE_DIR,MODE_PRIVATE);
                fos.write(intToByteArray(0));   // default value of zero
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            FileInputStream fis = openFileInput(SCORE_DIR);
            byte[] buff =new byte[4];
            fis.read(buff);
            score = byteArrayToInt(buff);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return score;
    }


    /**
     * Converts a 32 bit integer into a 4 byte array
     * created: 23/03/2019 by J.Cistiakovas
     * last modified: 23/03/2019 by J.Cistiakovas
     */
    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value
        };
    }

    /**
     * Converts a 4 byte array into a 32 bit integer. A complemeent to intToByteArray function
     * created: 23/03/2019 by J.Cistiakovas
     * last modified: 23/03/2019 by J.Cistiakovas
     */
    public static int byteArrayToInt(byte[] b)
    {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }
}
