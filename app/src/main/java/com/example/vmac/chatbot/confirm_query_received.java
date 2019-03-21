package com.example.vmac.chatbot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.vmac.WatBot.R;

public class confirm_query_received extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_query_received);
    }

    //avoid user going back into game when it is finished. Back to home screen.
    @Override
    public void onBackPressed() {
        Intent backToHome = new Intent(this, home_screen.class);
        startActivity(backToHome);
    }
}
