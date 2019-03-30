package com.example.vmac.chatbot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.vmac.WatBot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class contact extends AppCompatActivity {

    private EditText mContactEmail;
    private EditText mMessage;
    private static final String TAG = "ContactActivity";
    private static final int EMAIL_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //button enabling user to send us a message/query
        Button send_button = findViewById(R.id.send_button);

        mContactEmail = findViewById(R.id.contactEmail);
        mMessage = findViewById(R.id.contactMessage);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadContactMessage();
                // switch to the new activity is disabled as stops the user from successfully sending an email
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

    /**
     * Method that uploads the feedback into the database.
     * created: 30/03/2019 by J.Cistiakovas
     * last modified: 30/03/2019 by J.Cistiakovas
     */
    private void uploadContactMessage(){
        String message = mMessage.getText().toString().trim();
        String email = mContactEmail.getText().toString().trim();

        FirebaseAuth Auth = FirebaseAuth.getInstance();
        FirebaseDatabase Database = FirebaseDatabase.getInstance();
        DatabaseReference DatabaseRefFeedback = Database.getReference("feedback");
        DatabaseReference newFeedback = DatabaseRefFeedback.push();
        newFeedback.child("message").setValue(message);
        newFeedback.child("reply-email").setValue(email);


    }



    }
