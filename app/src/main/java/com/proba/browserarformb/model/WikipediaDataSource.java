package com.proba.browserarformb.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.proba.browserarformb.view.components.IconMarker;
import com.proba.browserarformb.view.components.Marker;
import com.proba.browserarformb.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WikipediaDataSource extends NetworkDataSource {
    /**
     * @// TODO: 3/1/2017
     * get username from manifest file
     */
    private static final String BASE_URL = "http://api.geonames.org/findNearbyJSON?formatted=true"
            + "&username=oshop.guest&radius=300";
    private static final String TAG = "Cip";

    private static Bitmap icon = null;

    public WikipediaDataSource(Resources res) {
        if (res==null) throw new NullPointerException();

        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res==null) throw new NullPointerException();

        icon= BitmapFactory.decodeResource(res, R.drawable.wikipedia);
    }

    @Override
    public String createRequestURL(double lat, double lng) {
        String theURL = BASE_URL + "&lat=" + lat + "&lng=" + lng;
        Log.d(TAG, theURL);
        return theURL;
    }

    @Override
    public List<Marker> parse(JSONObject jsonObject) {
        if (jsonObject==null) return null;

        JSONObject jo = null;
        JSONArray geonamesArray = null;
        List<Marker> markers=new ArrayList<Marker>();

        try {
            if(jsonObject.has("geonames")) {
                geonamesArray = jsonObject.getJSONArray("geonames");
            }
            if (geonamesArray == null) return markers;

            for (int i = 0; i < geonamesArray.length(); i++) {
                jo = geonamesArray.getJSONObject(i);
                Marker marker = processJSONObject(jo);
                if(marker!=null) markers.add(marker);
            }
        } catch (JSONException e) {
            Log.e(TAG, "geoanames json exception", e);
        }
        return markers;
    }

    private Marker processJSONObject(JSONObject jsonObject) {
        Marker marker = null;
        if (jsonObject.has("toponymName") && jsonObject.has("lat") && jsonObject.has("lng") ) {
            try {
                String name = jsonObject.getString("toponymName");
                double lat = jsonObject.getDouble("lat");
                double lng = jsonObject.getDouble("lng");
                marker = new IconMarker(name, lat, lng, 0, Color.WHITE, icon);
                Log.d(TAG, "name: " + name + ", " + lat + ", " + lng );
            } catch (JSONException e) {
                Log.e(TAG, "geoanames object exception", e);
            }
        }
        return marker;
    }
}