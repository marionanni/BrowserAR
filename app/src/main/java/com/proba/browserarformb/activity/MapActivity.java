package com.proba.browserarformb.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.proba.browserarformb.R;
import com.proba.browserarformb.globals.Globals;
import com.proba.browserarformb.model.LocationGPS;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class MapActivity extends AppCompatActivity  implements MapEventsReceiver {

    private static final String TAG = "Cip";
    private MapView map;
    private IMapController mapController;
    private MapEventsOverlay mapEventsOverlay;
    private Marker myMarker;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        enableMaps();
        setOverlay();

        setMarkerOnDefaultPosittion();
    }

    private void enableMaps() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController = map.getController();
        mapController.setZoom(17);
    }

    private void setOverlay() {
        mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);
    }

    private void setMarkerOnDefaultPosittion() {
        LocationGPS currentLocation = ((Globals)this.getApplication()).getCurrentLocation();
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();

        GeoPoint startPoint = new GeoPoint(lat, lng);
        Log.d(TAG, String.format("lat: %.2f, lng: %.2f", lat, lng));
        drawMarker(startPoint);
    }

    public void drawMarker(GeoPoint geoPoint){
        mapController.setCenter(geoPoint);

        myMarker = new Marker(map);
        myMarker.setPosition(geoPoint);
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        myMarker.setIcon(getResources().getDrawable(R.drawable.person));
        myMarker.setTitle(getString(R.string.fake_location_marker_title));

        map.getOverlays().add(myMarker);
        map.invalidate();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        InfoWindow.closeAllInfoWindowsOn(map);
        map.getOverlays().remove(myMarker);
        map.invalidate();
        ((Globals)this.getApplication()).setCurrentLocation(geoPoint);
        drawMarker(geoPoint);

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    public void switchToAR(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
