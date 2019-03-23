package com.example.vmac.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.vmac.WatBot.R;

//RESULT SCREEN IF THEY USER PLAYED A BOT
public class results extends genericResults {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);


        //get the time taken from the main activity class
        Intent in = getIntent();
        String timeTaken;
        timeTaken = in.getStringExtra("timeTaken");
        //Toast.makeText(this, "Time elapsed:" + timeTaken, Toast.LENGTH_SHORT).show();

        //display the time taken to the screen - using the layout screen
        TextView timeToDisplay = (TextView) findViewById(R.id.timeBotText);
        timeToDisplay.setText(timeTaken);

        changeScore(genericResults.WIN);
    }

    //avoid user going back into game when it is finished. Back to home screen.
    @Override
    public void onBackPressed() {
        Intent backToHome = new Intent(this, home_screen.class);
        startActivity(backToHome);
    }

}
