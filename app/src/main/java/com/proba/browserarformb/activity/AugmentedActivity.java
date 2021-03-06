package com.proba.browserarformb.activity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.proba.browserarformb.R;
import com.proba.browserarformb.model.ARData;
import com.proba.browserarformb.view.components.Marker;
import com.proba.browserarformb.view.widgets.VerticalSeekBar;

import java.text.DecimalFormat;

public class AugmentedActivity extends SensorsActivity implements View.OnTouchListener, SurfaceHolder.Callback {

    private static final String TAG = "Cip";
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    protected static PowerManager.WakeLock wakeLock = null;
    protected static VerticalSeekBar myZoomBar = null;
    protected static TextView endLabel = null;
    protected static LinearLayout zoomLayout = null;
    protected static AugmentedView augmentedView = null;

    public static final float MAX_ZOOM = 50; //in KM
    public static final float ONE_PERCENT = MAX_ZOOM/100f;
    public static final float TEN_PERCENT = 10f*ONE_PERCENT;
    public static final float TWENTY_PERCENT = 2f*TEN_PERCENT;
    public static final float EIGHTY_PERCENTY = 4f*TWENTY_PERCENT;

    public static boolean useCollisionDetection = true;
    public static boolean showRadar = true;
    public static boolean showZoomBar = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);


        addViewsDinamically();
        updateDataOnZoom();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DimScreen");
    }

    private void addViewsDinamically() {

        /**
         * @// TODO: 2/26/2017
         * put those views into activity_ar layout
         */
        addAugmentedView();
        addZoomLayoutView();
        addZoombarTextView();
        addZoomSeekBar();
    }

    private void addAugmentedView() {
        augmentedView = new AugmentedView(this);
        augmentedView.setOnTouchListener(this);
        ViewGroup.LayoutParams augLayout = new ViewGroup.LayoutParams(  ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addContentView(augmentedView,augLayout);
    }

    private void addZoomLayoutView() {
        int BACKGROUND_COLOR = Color.argb(125,55,55,55);
        zoomLayout = new LinearLayout(this);
        zoomLayout.setVisibility((showZoomBar)?LinearLayout.VISIBLE:LinearLayout.GONE);
        zoomLayout.setOrientation(LinearLayout.VERTICAL);
        zoomLayout.setPadding(5, 5, 5, 5);
        zoomLayout.setBackgroundColor(BACKGROUND_COLOR);
    }

    private void addZoombarTextView() {
        String END_TEXT = FORMAT.format(AugmentedActivity.MAX_ZOOM)+" km";

        endLabel = new TextView(this);
        endLabel.setText(END_TEXT);
        endLabel.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams zoomTextParams =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        zoomLayout.addView(endLabel, zoomTextParams);
    }

    private void addZoomSeekBar() {
        myZoomBar = new VerticalSeekBar(this);
        myZoomBar.setMax(100);
        myZoomBar.setProgress(50);
        myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
        LinearLayout.LayoutParams zoomBarParams =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
        zoomBarParams.gravity = Gravity.CENTER_HORIZONTAL;
        zoomLayout.addView(myZoomBar, zoomBarParams);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(  ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.FILL_PARENT,
                Gravity.RIGHT);
        addContentView(zoomLayout,frameLayoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
    }

    @Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER ||
                evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            augmentedView.postInvalidate();
        }
    }

    private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateDataOnZoom();
            surfaceView.invalidate();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            //Not used
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            updateDataOnZoom();
            surfaceView.invalidate();
        }
    };

    private static float calcZoomLevel(){
        int myZoomLevel = myZoomBar.getProgress();
        float out = 0;

        float percent = 0;
        if (myZoomLevel <= 25) {
            percent = myZoomLevel/25f;
            out = ONE_PERCENT*percent;
        } else if (myZoomLevel > 25 && myZoomLevel <= 50) {
            percent = (myZoomLevel-25f)/25f;
            out = ONE_PERCENT+(TEN_PERCENT*percent);
        } else if (myZoomLevel > 50 && myZoomLevel <= 75) {
            percent = (myZoomLevel-50f)/25f;
            out = TEN_PERCENT+(TWENTY_PERCENT*percent);
        } else {
            percent = (myZoomLevel-75f)/25f;
            out = TWENTY_PERCENT+(EIGHTY_PERCENTY*percent);
        }
        return out;
    }

    protected void updateDataOnZoom() {
        float zoomLevel = calcZoomLevel();
        ARData.setRadius(zoomLevel);
        ARData.setZoomLevel(FORMAT.format(zoomLevel));
        ARData.setZoomProgress(myZoomBar.getProgress());
    }

    public boolean onTouch(View view, MotionEvent me) {
        for (Marker marker : ARData.getMarkers()) {
            if (marker.handleClick(me.getX(), me.getY())) {
                if (me.getAction() == MotionEvent.ACTION_UP) {
                    markerTouched(marker);
                }
                return true;
            }
        }
        return super.onTouchEvent(me);
    }

    protected void markerTouched(Marker marker) {
//        Log.w(TAG,"markerTouched() not implemented in AugmentedActivity");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            Log.e(TAG, "surfaceCreated RuntimeException", e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

        param.setPreviewSize(352, 288);
        camera.setParameters(param);
        try {
            // tell the camera where to draw the preview.
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "surfaceCreated Exception", e);
            return;
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    private void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}