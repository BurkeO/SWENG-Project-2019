package com.example.vmac.chatbot;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class genericResults extends AppCompatActivity {
    private static String TAG = "genericResults";
    static final boolean WIN = true;
    static final boolean LOSE = false;

    /**
     * Reads and updates the score file.
     * created: 23/03/2019 by J.Cistiakovas
     * last modified: 23/03/2019 by J.Cistiakovas
     * TODO: change how the score is calculated/incremented
     */
    protected void changeScore(boolean gameState){
        if(gameState == WIN){
            int score = 0;
            byte[] buff = null;
            File file = new File(getFilesDir() + "/" +home_screen.SCORE_DIR);
            if (!file.exists()) {
                Log.d(TAG, "file does not exist");
                //create a new file
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(home_screen.SCORE_DIR,MODE_PRIVATE);
                    fos.write(home_screen.intToByteArray(0));
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                FileInputStream fis = getApplicationContext().openFileInput(home_screen.SCORE_DIR);
                buff =new byte[4];
                fis.read(buff);
                score = home_screen.byteArrayToInt(buff);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //write the updated score
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(home_screen.SCORE_DIR,MODE_PRIVATE);
                // increment the score by 1
                fos.write(home_screen.intToByteArray(score+1));
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }


        }
    }
}
