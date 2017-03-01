package com.proba.browserarformb.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.proba.browserarformb.R;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CAMERA = 101;
    private static final int REQUEST_PERMISSION_STORAGE = 102;
    private static final int REQUEST_PERMISSION_LOCATION = 103;
    private Thread mThread;
    private boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        backgroundThread();
        checkForPermissions();
    }

    private void backgroundThread() {
        mThread = new Thread(new Runnable(){
            public static final String TAG = "Cip";

            public void run() {
                try {
                    //init stuff & psg geonames from assets
                    //AssetManager mgr = getAssets();
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException", e);
                }
                finish();
                startActivity(new Intent(SplashActivity.this, MapActivity.class));
            }
        });
    }

    private void checkForPermissions() {
        /**
         * @// TODO: 3/1/2017
         * refactor permissions as single class
         * bug: cred ca .... one true -> all true
         */
        checkForCameraPermission();
        checkForLocationPermission();
        checkForStoragePermission();
        startApp();
    }

    private void checkForCameraPermission() {
        if(!isCameraPermissionAllowed()){
            if(showCameraPermissionRationale()){
                showRationaleCameraExplanation();
            } else {
                requestCameraPermission();
            }
        } else {
            permissionGranted = true;
        }
    }


    public boolean isCameraPermissionAllowed() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
    }



    private boolean showCameraPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
    }


    private void showRationaleCameraExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Permission Needed")
                .setMessage("Without this permission the application can't provide the desired functionality")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestCameraPermission();
                    }
                });
        builder.create().show();
    }



    private void checkForLocationPermission() {
        if(!isLocationPermissionAllowed()){
            if(showLocationPermissionRationale()){
                showRationaleCameraExplanation();
            } else {
                requestLocationPermission();
            }
        } else {
            permissionGranted = true;
        }
    }

    private boolean showLocationPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean isLocationPermissionAllowed() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
    }


    private void checkForStoragePermission() {
        if(!isStoragePermissionAllowed()){
            if(showStoragePermissionRationale()){
                showRationaleCameraExplanation();
            } else {
                requestStoragePermission();
            }
        } else {
            permissionGranted = true;
        }
    }

    private boolean showStoragePermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean isStoragePermissionAllowed() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CAMERA);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_STORAGE:
            case REQUEST_PERMISSION_LOCATION:
            case REQUEST_PERMISSION_CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                }
                break;
        }
        startApp();
    }

    private void startApp() {
        if(permissionGranted == true){
            mThread.start();
        }
    }

    private void debugLog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
