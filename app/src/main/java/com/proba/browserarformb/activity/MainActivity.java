package com.proba.browserarformb.activity;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.proba.browserarformb.R;
import com.proba.browserarformb.globals.Globals;
import com.proba.browserarformb.model.ARData;
import com.proba.browserarformb.model.GoogleDataSource;
import com.proba.browserarformb.model.LocationGPS;
import com.proba.browserarformb.model.NetworkDataSource;
import com.proba.browserarformb.view.components.Marker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AugmentedActivity {
    private static final String TAG = "MainActivity";
    private static final String locale = "en";
    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, queue);
    private static final Map<String,NetworkDataSource> sources = new ConcurrentHashMap<String,NetworkDataSource>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        LocalDataSource localData = new LocalDataSource(this.getResources());
//        ARData.addMarkers(localData.getMarkers());

//        NetworkDataSource twitter = new TwitterDataSource(this.getResources());
//        sources.put("twitter",twitter);
//        NetworkDataSource geonames = new WikipediaDataSource(this.getResources());
//        sources.put("wiki",geonames);
        NetworkDataSource googlePlaces = new GoogleDataSource(this.getResources());
        sources.put("googlePlaces",googlePlaces);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDataBasedOnLocation();
    }

    public void updateDataBasedOnLocation(){
        //        Location last = ARData.getCurrentLocation();
        LocationGPS currentLocation = ((Globals)this.getApplication()).getCurrentLocation();
        updateData(currentLocation.getLatitude(),currentLocation.getLongitude(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected() item="+item);
        switch (item.getItemId()) {
            case R.id.showRadar:
                showRadar = !showRadar;
                item.setTitle(((showRadar)? "Hide" : "Show")+" Radar");
                break;
            case R.id.showZoomBar:
                showZoomBar = !showZoomBar;
                item.setTitle(((showZoomBar)? "Hide" : "Show")+" Zoom Bar");
                zoomLayout.setVisibility((showZoomBar)? LinearLayout.VISIBLE:LinearLayout.GONE);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        updateDataBasedOnLocation();
    }

    @Override
    protected void markerTouched(Marker marker) {
        Toast t = Toast.makeText(getApplicationContext(), marker.getName(), Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    @Override
    protected void updateDataOnZoom() {
        super.updateDataOnZoom();
        updateDataBasedOnLocation();
    }

    private void updateData(final double lat, final double lon, final double alt) {
        try {
            exeService.execute(
                    new Runnable() {

                        public void run() {
                            for (NetworkDataSource source : sources.values())
                                download(source, lat, lon);
                        }
                    }
            );
        } catch (RejectedExecutionException rej) {
            Log.w(TAG, "Not running new download Runnable, queue is full.");
        } catch (Exception e) {
            Log.e(TAG, "Exception running download Runnable.",e);
        }
    }

    private static boolean download(NetworkDataSource source, double lat, double lon) {
        if (source==null) return false;

        String url = null;
        try {
            url = source.createRequestURL(lat, lon);
        } catch (NullPointerException e) {
            Log.e(TAG, "exception", e);
        }

        List<Marker> markers = null;
        try {
            markers = source.parse(url);
        } catch (NullPointerException e) {
            return false;
        }

        ARData.addMarkers(markers);
        return true;
    }

    public void switchToMap(View view) {
        startActivity(new Intent(this, MapActivity.class));
    }
}