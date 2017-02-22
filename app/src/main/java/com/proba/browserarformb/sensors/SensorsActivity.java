package com.proba.browserarformb.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.proba.browserarformb.Matrix;
import com.proba.browserarformb.model.ARData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SensorsActivity extends Activity implements SensorEventListener, LocationListener {
    private static final String TAG = "SensorsActivity";
    /* verifica daca o sarcina este in executie */
    private static final AtomicBoolean isQueueBusy = new AtomicBoolean(false);

    private static final int MIN_TIME_BETWEEN_LOCATION_UPDATES = 30*1000;
    private static final int MIN_DISTANCE_BETWEEN_LOCATION_UPDATES = 10;

    private static final float tmpForRotationMatrix[] = new float[9];
    private static final float finalRotationMatrix[] = new float[9];
    private static final float gravityNumbers[] = new float[3];
    private static final float magneticFieldNumbers[] = new float[3];


    private static final Matrix worldCoordonates = new Matrix();
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix xAxisRotation = new Matrix();
    private static final Matrix magneticNorthCompensation = new Matrix();

    private static GeomagneticField geomagneticField = null;
    private static float smooth[] = new float[3];
    private static SensorManager sensorManager = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGravity = null;
    private static Sensor sensorMagnetic = null;
    private static LocationManager locationManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        double angleX = Math.toRadians(-90);
        double angleY = Math.toRadians(-90);

        xAxisRotation.set( 1f,
                0f,
                0f,
                0f,
                (float) Math.cos(angleX),
                (float) -Math.sin(angleX),
                0f,
                (float) Math.sin(angleX),
                (float) Math.cos(angleX));

        try {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

            if (sensors.size() > 0) {
                sensorGravity = sensors.get(0);
            }

            sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

            if (sensors.size() > 0) {
                sensorMagnetic = sensors.get(0);
            }
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorMagnetic, SensorManager.SENSOR_DELAY_GAME);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_LOCATION_UPDATES, MIN_DISTANCE_BETWEEN_LOCATION_UPDATES, this);

            try {

                try {
                    Location gps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(gps!=null) {
                        onLocationChanged(gps);
                    } else if (network!=null) {
                        onLocationChanged(network);
                    } else {
                        onLocationChanged(ARData.hardFix);
                    }
                } catch (Exception ex2) {
                    Log.e(TAG, "onLocationChanged exception", ex2);
                    onLocationChanged(ARData.hardFix);
                }

                geomagneticField = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                        (float) ARData.getCurrentLocation().getLongitude(),
                        (float) ARData.getCurrentLocation().getAltitude(),
                        System.currentTimeMillis());
                angleY = Math.toRadians(-geomagneticField.getDeclination());

                synchronized (magneticNorthCompensation) {
                    magneticNorthCompensation.toIdentity();
                    magneticNorthCompensation.set( (float) Math.cos(angleY),
                            0f,
                            (float) Math.sin(angleY),
                            0f,
                            1f,
                            0f,
                            (float) -Math.sin(angleY),
                            0f,
                            (float) Math.cos(angleY));
                    magneticNorthCompensation.prod(xAxisRotation);
                }
            } catch (Exception ex) {
                Log.e(TAG, "magnetic compensation exception", ex);
            }
        } catch (Exception ex1) {
            try {
                if (sensorManager != null) {
                    sensorManager.unregisterListener(this, sensorGravity);
                    sensorManager.unregisterListener(this, sensorMagnetic);
                    sensorManager = null;
                }
                if (locationManager != null) {
                    locationManager.removeUpdates(this);
                    locationManager = null;
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            try {
                sensorManager.unregisterListener(this, sensorGravity);
                sensorManager.unregisterListener(this, sensorMagnetic);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sensorManager = null;

            try {
                locationManager.removeUpdates(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            locationManager = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSensorChanged(SensorEvent evt) {
        if (!isQueueBusy.compareAndSet(false, true)) return;

        switch (evt.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, gravityNumbers);
                gravityNumbers[0] = smooth[0];
                gravityNumbers[1] = smooth[1];
                gravityNumbers[2] = smooth[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, magneticFieldNumbers);
                magneticFieldNumbers[0] = smooth[0];
                magneticFieldNumbers[1] = smooth[1];
                magneticFieldNumbers[2] = smooth[2];
                break;
        }

        SensorManager.getRotationMatrix(tmpForRotationMatrix, null, gravityNumbers, magneticFieldNumbers);
        SensorManager.remapCoordinateSystem(tmpForRotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, finalRotationMatrix);
        worldCoordonates.set(finalRotationMatrix[0], finalRotationMatrix[1], finalRotationMatrix[2],
                finalRotationMatrix[3], finalRotationMatrix[4], finalRotationMatrix[5],
                finalRotationMatrix[6], finalRotationMatrix[7], finalRotationMatrix[8]);

        magneticCompensatedCoord.toIdentity();

        synchronized (magneticNorthCompensation) {
            magneticCompensatedCoord.prod(magneticNorthCompensation);
        }

        magneticCompensatedCoord.prod(worldCoordonates);

        magneticCompensatedCoord.invert();

        ARData.setRotationMatrix(magneticCompensatedCoord);

        isQueueBusy.set(false);
    }

    public void onLocationChanged(Location location) {
        ARData.setCurrentLocation(location);
        geomagneticField = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(),
                System.currentTimeMillis());

        double angleY = Math.toRadians(-geomagneticField.getDeclination());

        synchronized (magneticNorthCompensation) {
            magneticNorthCompensation.toIdentity();

            magneticNorthCompensation.set((float) Math.cos(angleY),
                    0f,
                    (float) Math.sin(angleY),
                    0f,
                    1f,
                    0f,
                    (float) -Math.sin(angleY),
                    0f,
                    (float) Math.cos(angleY));

            magneticNorthCompensation.prod(xAxisRotation);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
    }

    public void onProviderDisabled(String provider) {
        //Not Used
    }

    public void onProviderEnabled(String provider) {
        //Not Used
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Not Used
    }
}