package com.adampere.lifejacket;

import android.content.Intent;
import android.os.Debug;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class InitialActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.adampere.lifejacket.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        addRadioButtons(2);

    }


    /**
     * Adds number radio buttons to the radio button group in this view.
     * *** FUTURE: Update so that it adds a radio button for each camera
     * @param number the number of radio buttons to add
     */
    public void addRadioButtons(int number) {
            for (int i = 1; i <= number; i++) {
                RadioButton rdbtn = new RadioButton(this);
                rdbtn.setId((2) + i);
                rdbtn.setText("Radio " + rdbtn.getId());
                ((ViewGroup) findViewById(R.id.viewRadioGroup)).addView(rdbtn);
            }
    }

    /**
     * Called when the user presses the 'display view' button
     * This function opens up the 'video stream' view with the stream from the
     * appropriate camera/source
     * @param view
     */
    public void openVideoStream(View view){

        RadioGroup rgp = (RadioGroup)findViewById(R.id.viewRadioGroup);
        if(rgp.getCheckedRadioButtonId() != -1) {
            int radioID = rgp.getCheckedRadioButtonId();
            View rbv = rgp.findViewById(radioID);
            RadioButton rb = (RadioButton)rgp.findViewById(radioID);


            String tex = (String) rb.getText().toString();
            Log.d("radiochecked", tex);

            Intent intent =  new Intent(this, VideoStream.class);
            intent.putExtra(EXTRA_MESSAGE, tex);
            startActivity(intent);
        }

    }

}
