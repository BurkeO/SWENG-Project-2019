package com.example.vmac.chatbot;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vmac.WatBot.R;

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
                sendContactMessage();
                // switch to the new activity is disabled as stops the user from successfully sending an email
                //openConfirmQueryScreen();
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
     * Method that initiates the sending of an email using the fields from the layout.
     * The user is promtped to select the email carrier and go to the email app.
     * created: 30/03/2019 by J.Cistiakovas
     * last modified: 30/03/2019 by J.Cistiakovas - changed startACtivity() to startActivityForResult()
     */
    private void sendContactMessage(){
        Log.d(TAG, "sending an email");
        String message = mMessage.getText().toString().trim();
        String email = mContactEmail.getText().toString().trim();
        String[] TO = {""};
        TO[0] = email;
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setType("message/rfc822");
        i.setData(Uri.parse("mailto:" + email));
        //i.putExtra(Intent.EXTRA_EMAIL, TO);
        i.putExtra(Intent.EXTRA_SUBJECT, "feedback");
        i.putExtra(Intent.EXTRA_TEXT, message);

        try{
            startActivityForResult(Intent.createChooser(i,"Send mail..."),EMAIL_CODE);
            Log.d(TAG, "finished sendng email");
            //finish();
        } catch (android.content.ActivityNotFoundException e){
            Toast.makeText(this,"Failed to send an email", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EMAIL_CODE) {
            // start a new activity
            openConfirmQueryScreen();
        }
    }


    }
