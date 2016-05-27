package com.adampere.lifejacket;

import android.app.ActionBar;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

public class VideoStream extends AppCompatActivity implements CameraDialog.CameraDialogParent{
    private static final boolean DEBUG = true;	// TODO set false when production
    private static final String TAG = "VideoStream";

    // for thread pool
    private static final int CORE_POOL_SIZE = 1;		// initial/minimum threads
    private static final int MAX_POOL_SIZE = 4;			// maximum threads
    private static final int KEEP_ALIVE_TIME = 10;		// time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SurfaceView mUVCCameraView;
    // for open&start / stop&close camera preview
    private ImageButton mCameraButton;
    private Surface mPreviewSurface;
    private boolean isActive, isPreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);

        // hide the title bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        // hide the action bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // recieve information from last activity
        Intent intent = getIntent();
        String message = intent.getStringExtra(InitialActivity.EXTRA_MESSAGE);


        // debugging string
        TextView dbgStr = (TextView)findViewById(R.id.debugViewString);
        dbgStr.setText(message);


        // setup webcams
        mCameraButton = (ImageButton)findViewById(R.id.camera_button);
        mCameraButton.setOnClickListener(mOnClickListener);

        mUVCCameraView = (SurfaceView)findViewById(R.id.camera_surface_view);
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);


        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);



    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume:");
        mUSBMonitor.register();
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");
        mUSBMonitor.unregister();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            isActive = isPreview = false;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        mCameraButton = null;
        super.onDestroy();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mUVCCamera == null) {
                // XXX calling CameraDialog.showDialog is necessary at only first time(only when app has no permission).
                CameraDialog.showDialog(VideoStream.this);
            } else {
                synchronized (mSync) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                    isActive = isPreview = false;
                }
            }
        }
    };

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:");
            Toast.makeText(VideoStream.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            synchronized (mSync) {
                if (mUVCCamera != null)
                    mUVCCamera.destroy();
                isActive = isPreview = false;
            }
            EXECUTER.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        mUVCCamera = new UVCCamera();
                        mUVCCamera.open(ctrlBlock);
                        if (DEBUG) Log.i(TAG, "supportedSize:" + mUVCCamera.getSupportedSize());
                        try {
                            mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final IllegalArgumentException e) {
                            try {
                                // fallback to YUV mode
                                mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                            } catch (final IllegalArgumentException e1) {
                                mUVCCamera.destroy();
                                mUVCCamera = null;
                            }
                        }
                        if ((mUVCCamera != null) && (mPreviewSurface != null)) {
                            isActive = true;
                            mUVCCamera.setPreviewDisplay(mPreviewSurface);
                            mUVCCamera.startPreview();
                            isPreview = true;
                        }
                    }
                }
            });
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            // XXX you should check whether the comming device equal to camera device that currently using
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.close();
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                    isActive = isPreview = false;
                }
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:");
            Toast.makeText(VideoStream.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
        }
    };

    /**
     * to access from CameraDialog
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceCreated:");
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;
            if (DEBUG) Log.v(TAG, "surfaceChanged:");
            mPreviewSurface = holder.getSurface();
            synchronized (mSync) {
                if (isActive && !isPreview) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
            }
            mPreviewSurface = null;
        }
    };

}
