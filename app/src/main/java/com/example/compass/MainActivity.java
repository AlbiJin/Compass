package com.example.compass;


import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.SensorEvent;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Soruces:
 *  https://github.com/feeeei/CircleSeekbar
 *  https://medium.com/cs-random-thoughts-on-tech/android-force-hide-system-keyboard-while-retaining-edittexts-focus-9d3fd8dbed32
 *  https://stackoverflow.com/questions/2874743/android-volume-buttons-used-in-my-application
 *  https://www.youtube.com/watch?v=OPsVr44uCb8&t=12s
 *
 * Tasks:
 * Seekbar
 * - Create a customized circular seekbar (CircleSeekBar.java)
 * - Set a photo of watch screen surrounded with letters as a background
 * - Value form the seekbar, 1-100 will be sent back to the MainActivity.java
 * - Method setValToLetter, convert number values to letters
 *
 * Enter the selected letter and control a text cursor back and fort by volume control buttons with a Method 'dispatchKeyEvent'
 *
 * Use gyroscope to detect flicking gesture to delete the letter in the EditText
 *
 */


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CircleSeekBar mSeekbar;
    private EditText mTextView ;
    private TextView mTextHide;
    private Gyroscope gyroscope;
    private long lastUpdate = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSeekbar = (CircleSeekBar) findViewById(R.id.seekbar);
        mTextView = (EditText) findViewById(R.id.textview);
        mTextHide = (TextView) findViewById(R.id.texthide);
        mTextHide.setText("a"); // initial letter
        gyroscope = new Gyroscope(this);


        // hide keyboard and disable touch, but keep cursor on
        setKeyboard();

        // Create seekbar for a letter selecting
        mSeekbar.setOnSeekBarChangeListener(new CircleSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(CircleSeekBar seekbar, int curValue) {
                mTextHide.setText("a"); // initial letter
                mTextHide.setText(setValToLetter(curValue));
            }
        });


        // Gyroscope for flicking detection, with controlled time window at 400ms
        // delete a letter in an EditText
        gyroscope.setListener(new Gyroscope.Listener() {

            @Override
            public void onRotation(float rx, float ry, float rz) {
                float rx_threshold = 4.0f;
                long curTime = System.currentTimeMillis();
                long diffTime ;


                if (Math.abs(rx) > rx_threshold) {

                    diffTime = (curTime - lastUpdate);

                    if ((diffTime) > 450) {
                        int cursorPosition = mTextView.getSelectionStart();
                        if (cursorPosition > 0) {
                            mTextView.setText(mTextView.getText().delete(cursorPosition - 1, cursorPosition));
                            mTextView.setSelection(cursorPosition - 1);
                        }
                    }
                    lastUpdate = curTime;
                }

            }
        });


    }

    /**
     * User volume control buttons for:
     * - volume up button: to enter a letter or Uppercase a letter before entering the letter (long press)
     * - volume down button: to move cursor backward or forward (long press)
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {

                    // long press: enter an uppercase letter
                    if (event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout()) {
                        mTextHide.setText(mTextHide.getText().toString().toUpperCase());
                        if (mTextHide.getText().toString() == " ") {
                            mTextHide.setText(".");
                        }
                        //om; mTextView.append(mTextHide.getText().toString());
                        int start = Math.max(mTextView.getSelectionStart(), 0);
                        int end = Math.max(mTextView.getSelectionEnd(), 0);
                        mTextView.getText().replace(Math.min(start, end), Math.max(start, end),
                                mTextHide.getText().toString(), 0, mTextHide.getText().toString().length());

                        mTextHide.setText(mTextHide.getText().toString().toLowerCase());

                    // short press: enter a lowercase letter
                    } else {
                        //om; mTextView.append(mTextHide.getText().toString());
                        int start = Math.max(mTextView.getSelectionStart(), 0);
                        int end = Math.max(mTextView.getSelectionEnd(), 0);
                        mTextView.getText().replace(Math.min(start, end), Math.max(start, end),
                                mTextHide.getText().toString(), 0, mTextHide.getText().toString().length());
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {

                    // long press: move a cursor forward
                    if (event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout()) {
                        mTextView.setSelection(mTextView.getSelectionStart()+1);

                        // short press: move a cursor backward
                    } else {
                        mTextView.setSelection(mTextView.getSelectionStart()-1);
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    /**
     * Convert value from seekbar, 1-100, into a letter
     */
    private String setValToLetter(int curValue){
        float numLetter = 27f;
        float curSize = 100f/numLetter;
        int idxLetters = 0;
        String[] letters = new String[] { "a ", "b", "c","d", "e", "f","g","h","i","j","k","l"
                ,"m","n","o","p","q","r","s","t","u","v","w","x","y","z"," "," " };
        idxLetters =  Math.round(curValue / curSize);
        if (idxLetters > 27) idxLetters=27;
        return letters[idxLetters];
    }

    /**
     * Hide keyboard and disable touch, but keep the cursor visible
     */
    private void setKeyboard(){
        mTextView.requestFocus();
        mTextView.setShowSoftInputOnFocus(false);
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //om;mTextView.setSelection(mTextView.length());
                mTextView.setEnabled(false);
                mTextView.setEnabled(true);

                return false;
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        gyroscope.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscope.register();

    }

}
