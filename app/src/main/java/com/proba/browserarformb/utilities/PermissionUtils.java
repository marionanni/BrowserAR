package com.proba.browserarformb.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * fixed verson of http://droidmentor.com/multiple-permissions-in-one-go/
 * @// TODO: 3/4/2017
 * refactor
 */
public class PermissionUtils {

    private static final String TAG = "Cip";
    private Context mContext;
    private Activity mActivity;

    PermissionResultCallback callerActivity;

    ArrayList<String> mPermissionList = new ArrayList<>();
    ArrayList<String> listPermissionsNeeded = new ArrayList<>();
    String mAlertDialog = "";
    int mReqCode;

    public PermissionUtils(Activity activity) {
        this.mContext = activity.getApplicationContext();
        this.mActivity = activity;

        callerActivity = (PermissionResultCallback) activity;
    }

    public void checkPermission(ArrayList<String> permissionList, String dialogContent, int reqCode) {
        this.mPermissionList = permissionList;
        this.mAlertDialog = dialogContent;
        this.mReqCode = reqCode;

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions(permissionList, reqCode)) {
                callerActivity.PermissionGranted(reqCode);
            }
        } else {
            callerActivity.PermissionGranted(reqCode);
        }
    }


    private boolean checkAndRequestPermissions(ArrayList<String> permissions, int request_code) {

        if (permissions.size() > 0) {
            listPermissionsNeeded = new ArrayList<>();

            for (int i = 0; i < permissions.size(); i++) {
                int hasPermission = ContextCompat.checkSelfPermission(mActivity, permissions.get(i));

                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permissions.get(i));
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(mActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), request_code);
                return false;
            }
        }

        return true;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    Map<String, Integer> perms = new HashMap<>();

                    for (int i = 0; i < permissions.length; i++) {
                        perms.put(permissions[i], grantResults[i]);
                    }

                    final ArrayList<String> pending_permissions = new ArrayList<>();

                    for (int i = 0; i < listPermissionsNeeded.size(); i++) {
                        if (perms.get(listPermissionsNeeded.get(i)) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, listPermissionsNeeded.get(i)))
                                pending_permissions.add(listPermissionsNeeded.get(i));
                            else {
                                Log.i(TAG, "Go to settings and enable permissions");
                                callerActivity.NeverAskAgain(mReqCode);
                                return;
                            }
                        }

                    }

                    if (pending_permissions.size() > 0) {
                        showMessageOKCancel(mAlertDialog,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                checkPermission(mPermissionList, mAlertDialog, mReqCode);
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                Log.i(TAG, "Permissions not fully given");
                                                if (mPermissionList.size() == pending_permissions.size()) {
                                                    callerActivity.PermissionDenied(mReqCode);
                                                } else {
                                                    callerActivity.PartialPermissionGranted(mReqCode, pending_permissions);
                                                }
                                                break;
                                        }


                                    }
                                });

                    } else {
                        callerActivity.PermissionGranted(mReqCode);
                    }
                }
                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

}
