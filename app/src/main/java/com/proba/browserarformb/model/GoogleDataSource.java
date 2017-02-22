package com.proba.browserarformb.model;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.proba.browserarformb.IconMarker;
import com.proba.browserarformb.Marker;
import com.proba.browserarformb.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GoogleDataSource extends NetworkDataSource {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
            + "&radius=50000&key=AIzaSyB674D7dkyfWNIUn5trLC5HsR1SP7U_kB8&location=";
    private static final String TAG = "Cip";

    private static Bitmap icon = null;
    private static Resources mResources;

    public GoogleDataSource(Resources res) {
        mResources = res;
    }


    @Override
    public String createRequestURL(double lat, double lng) {
        return (BASE_URL + lat + "," + lng);
    }

    @Override
    public List<Marker> parse(JSONObject jsonObject) {
        if (jsonObject==null) return null;

        List<Marker> markers=new ArrayList<Marker>();

        try {
            if(jsonObject.has("results")) {
                JSONArray resultsArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject tmpJson = resultsArray.getJSONObject(i);
                    Marker marker = googlePlacesJSONToIconMarker(tmpJson);
                    if (marker != null) {
                        markers.add(marker);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "google places json exception", e);
        }
        return markers;
    }

    private Marker googlePlacesJSONToIconMarker(JSONObject jsonObject) {
        Marker marker = null;
        if (jsonObject.has("name") && jsonObject.has("geometry") ) {
            try {
                String name = jsonObject.getString("name");
                LocationGPS locationGPS = getLocation(jsonObject);
                icon = loadIcon(jsonObject.getString("icon"));
                double lat = locationGPS.getLatitude();
                double lng = locationGPS.getLongitude();
                marker = new IconMarker(name, lat, lng, 0, Color.WHITE, icon);
            } catch (JSONException e) {
                Log.e(TAG, "google places object exception", e);
            }
        }
        return marker;
    }

    private LocationGPS getLocation(JSONObject jsonObject) throws JSONException{

        LocationGPS locationGPS = new LocationGPS();
        if(jsonObject.getJSONObject("geometry").has("location")){
            JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");
            locationGPS = new LocationGPS(lat, lng);
        }

        return locationGPS;
    }


    private Bitmap loadIcon(String icon){
        Bitmap bitmapIcon = null;

        if(icon.endsWith(".png")){
            String pngIconFile = "icons/" + (new File(icon).getName());
            bitmapIcon = getBitmapFromAssets(pngIconFile);
        }
        return bitmapIcon;
    }

    private Bitmap getBitmapFromAssets(String fileName){
        Bitmap bitmap = BitmapFactory.decodeResource(mResources, R.drawable.wikipedia);

        try {
            AssetManager assetManager = mResources.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "getBitmapFromAssets", e);
        }

        return bitmap;
    }

}