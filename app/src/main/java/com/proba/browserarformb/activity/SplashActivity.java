package com.proba.browserarformb.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.proba.browserarformb.R;
import com.proba.browserarformb.globals.Globals;
import com.proba.browserarformb.model.LocationGPS;
import com.proba.browserarformb.utilities.PermissionResultCallback;
import com.proba.browserarformb.utilities.PermissionUtils;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback, PermissionResultCallback {

    public static final String TAG = "Cip";
    private Thread mThread;
    private PermissionUtils permissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initBackgroundThread();
        checkForPermissions();
    }

    private void checkForPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeeded.add(Manifest.permission.CAMERA);
        permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permissionUtils = new PermissionUtils(this);
        permissionUtils.checkPermission(permissionsNeeded, getString(R.string.permission_warning), 1);
    }

    private void initBackgroundThread() {
        mThread = new Thread(new Runnable(){
            public void run() {
                try {
                    initStuff();
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException", e);
                }
                finish();
                startActivity(new Intent(SplashActivity.this, MapActivity.class));
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void initStuff() {
        //init stuff & psg geonames, json's from assets
        double lat = Double.parseDouble(getString(R.string.start_lat));
        double lng = Double.parseDouble(getString(R.string.start_lng));
        ((Globals)this.getApplication()).setCurrentLocation(new LocationGPS(lat, lng));
    }

    @Override
    public void PermissionGranted(int requestCode) {
        debugLog("All Permissions Granted");
        mThread.start();
    }

    @Override
    public void PartialPermissionGranted(int requestCode, ArrayList<String> grantedPermissions) {
        debugLog("Permissions Partially Granted");
    }

    @Override
    public void PermissionDenied(int requestCode) {
        debugLog("Permission Denied");
    }

    @Override
    public void NeverAskAgain(int requestCode) {
        debugLog("Permission: NeverAskAgain ");
    }

    private void debugLog(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.i(TAG, message);
    }
}
