package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button start_game_button;  //initialise button that will be pressed to start a game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_game_button = (Button) findViewById(R.id.start_game_button);
        start_game_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openActivity2();
            }
        });
    }

    protected void openActivity2()
    {
        Intent start_game_intent = new Intent(this, Activity2.class);
        startActivity(start_game_intent);
    }
}
