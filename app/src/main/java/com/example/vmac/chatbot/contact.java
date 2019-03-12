package com.example.vmac.chatbot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.vmac.WatBot.R;

public class contact extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //button enabling user to send us a message/query
        Button send_button = findViewById(R.id.send_button);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openConfirmQueryScreen();
            }
        });
    }

    //function that takes user to a page confirming their query has been received after they send it
    public void openConfirmQueryScreen()
    {
        Intent confirm_query_received_intent = new Intent(this, confirm_query_received.class);
        startActivity(confirm_query_received_intent);
    }
}
