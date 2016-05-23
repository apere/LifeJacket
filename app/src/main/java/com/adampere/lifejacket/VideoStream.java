package com.adampere.lifejacket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class VideoStream extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);

        TextView dbgStr = (TextView)findViewById(R.id.debugViewString);
        dbgStr.setText("test");

    }
}
