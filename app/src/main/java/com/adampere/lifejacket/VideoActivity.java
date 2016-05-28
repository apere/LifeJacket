package com.adampere.lifejacket;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private View decorView;
    private MediaPlayer mp;
    private ImageView mImageView;
    private boolean orig_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // hide the action bar
        decorView = getWindow().getDecorView();
        hideSystemUI();

        // Play a video
//        VideoView vidSurf = (VideoView)findViewById(R.id.videoView);
//        vidSurf.setMediaController(new MediaController(this));

//        String uriPath =  "android.resource://" + getPackageName() + "/" + R.raw.woman_staring_cropped;
//        Uri uri = Uri.parse(uriPath);
//        vidSurf.setVideoURI(uri);
//        vidSurf.requestFocus();
//        vidSurf.start();

        // Display Image

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.woman_staring_cropped);
        mImageView.setOnClickListener(playAudioClickListener);
        orig_image = true;
        // Play Audio
        mp = new MediaPlayer();
        try {
            String path =  "android.resource://" + getPackageName() + "/" + R.raw.invasion;
            mp.setDataSource(VideoActivity.this, Uri.parse(path));
            mp.prepare();
            mp.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final View.OnClickListener playAudioClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
//           if( mp.isPlaying()) {
//               mp.pause();
//           } else {
//               mp.start();
//           }
            if(orig_image){
                mImageView.setImageResource(R.drawable.gray);
            } else {
                mImageView.setImageResource(R.drawable.woman_staring_cropped);
            }
            orig_image = !orig_image;

        }
    };

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }



}
