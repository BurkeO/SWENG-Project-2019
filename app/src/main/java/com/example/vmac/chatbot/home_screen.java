package com.example.vmac.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.vmac.WatBot.MainActivity;
import com.example.vmac.WatBot.R;

public class home_screen extends AppCompatActivity implements View.OnClickListener{

    //Use to test if something clicked, text appears:
    // Toast.makeText(this, "this was clicked", Toast.LENGTH_SHORT).show();
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
    }

    //methods bringing user to screen depending on which button they clicked
    public void openNewGame()
    {
        Intent new_game_intent = new Intent(this, MainActivity.class);
        startActivity(new_game_intent);
    }

    public void openContactScreen()
    {
        Intent contact_intent = new Intent(this, contact.class);
        startActivity(contact_intent);
    }

    public void openSettingsScreen()
    {
        Intent settings_intent = new Intent(this,settings.class);
        startActivity(settings_intent);
    }

    @Override
    public void onClick(View v)
    {
        //switch statement checks which button was clicked on click
        switch(v.getId())
        {
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
}
