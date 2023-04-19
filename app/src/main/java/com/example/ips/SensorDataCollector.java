package com.example.ips;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.ips.Data_Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorDataCollector implements SensorEventListener, LocationListener {

    public SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, magnetometer, barometer, stepdetector;
    private Sensor lightSensor, proximitySensor;
    private LocationManager locationManager;
    private WifiManager wifiManager;
    LocationListener locationListener;

    private Context context ;
    private float[] lastAccelerometer = new float[3];
    private float[] lastGyroscope = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float lastLight;
    private float lastProximity;
    private int lastStep = 0;
    private float lastBarometer;
    //private float lastAltitude;
    //real time location, always refreshing
    private LOCATION LocationInfo;
    private Location LocationFullInfo;
    private String bssid;
    //Store all scaned wifi information BSSID, SSID, LEVEL, frequency
    HashMap<String, WIFI> WifiInfo = new HashMap<>();
    //WIFI and Location INFO PAIR, refresh with WIFI scanning
    HashMap<LOCATION, WIFI> Wifi_Location;
    List<ScanResult> wifiScanResults;
    private long lastAccelerometerTimestamp = 0;
    private long lastStepTimestamp = 0;
    private long lastGyroscopeTimestamp = 0;
    private long lastMagnetometerTimestamp = 0;
    private long lastLightTimestamp = 0;
    private long lastProximityTimestamp = 0;
    private long lastBarometerTimestamp = 0;
    private long lastAltitudeTimestamp = 0;
    private long lastWifiScanTimestamp = 0;
    private long lastLocationTimestamp = 0;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private float[] initialQuaternion = new float[4];
    private PDR pdr;
    //private Data_Manager Data_manager;
    //constant
    private static final int WIFI_UPDATE_INTERVAL = 10000;//1s update interval for WiFi
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;


    public SensorDataCollector(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        stepdetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new DCLocationListener();
        //this.Data_manager = Data_manager;
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(context, "Please enable location services.", Toast.LENGTH_SHORT).show();
        }
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(context, "Please enable location services.", Toast.LENGTH_SHORT).show();
        }
        //Enable WiFi if disabled
        if(wifiManager.getWifiState()==wifiManager.WIFI_STATE_DISABLED){
            Toast.makeText(context, "WiFi disabled, enabling...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
    }


    //DataCollection LocationListener gets provider, accuracy, altitude, time, longitude, latitude and speed.
    class DCLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(@NonNull Location location){
            if(location != null){
                String provider = location.getProvider();
                float lon = (float) location.getLongitude();
                float lat = (float) location.getLatitude();
                //Log.i("Longitude", String.valueOf(lon));
                //Log.i("Latitude", String.valueOf(lat));
                LocationInfo = new LOCATION(lon, lat);
                LocationFullInfo = location;
                lastLocationTimestamp = System.currentTimeMillis();
                Log.e("Location:", String.valueOf(LocationInfo.longtitude) + ":" + String.valueOf(LocationInfo.latitude));
            }
            else Log.e("Location","=null");
        }
    }

    public void startCollecting() {
        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, stepdetector, 10000);
        sensorManager.registerListener(this, gyroscope, 10000);
        sensorManager.registerListener(this, magnetometer, 10000);
        sensorManager.registerListener(this, barometer, 10000);
        //sensorManager.registerListener(this, ambientLightSensor, 10000);
        sensorManager.registerListener(this, proximitySensor, 10000);
        //sensorManager.registerListener(this,gravitySensor,10000);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            Log.e("SensorDataCollector", "Location permission not granted");
        }
        //update wifi info
        WIFI_update();
    }

    public void stopCollecting() {
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        wifiManager.disconnect();
    }


    //Timestamps for WiFi data aggregation
    private long lastTimestamp = System.currentTimeMillis();
    private void WIFI_update(){
        long currentTimestamp = System.currentTimeMillis();
        lastWifiScanTimestamp = currentTimestamp;
        if(currentTimestamp-lastTimestamp > WIFI_UPDATE_INTERVAL) {
            //Check that permissions have been given or request permission
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                wifiScanResults = wifiManager.getScanResults(); //Get WiFi scan results
                WIFI curWifiData;
                for (ScanResult scanResult : wifiScanResults) {
                    int level = scanResult.level;
                    String bssid = scanResult.BSSID;
                    String ssid = scanResult.SSID;
                    long freq = scanResult.frequency;
                    curWifiData = new WIFI(level, bssid, ssid, freq);
                    this.bssid = bssid;
                    WifiInfo.put(bssid, curWifiData);//do we need to check ---- NO, automatically update old bssid-data
                    if (LocationInfo != null) {
                        Wifi_Location.put(LocationInfo, WifiInfo.get(bssid));
                    }
                }
            }
            lastTimestamp = currentTimestamp;
        }
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTimeStamp = System.currentTimeMillis();
        //update wifi info
        WIFI_update();
        sensorManager.getOrientation(rotationMatrix, orientationAngles);
        sensorManager.getQuaternionFromVector(initialQuaternion, rotationMatrix);
        //Log.i("Orientation WHEN INIT:", String.valueOf(orientationAngles[0]));
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values.clone();
                lastAccelerometerTimestamp = currentTimeStamp;
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                lastStep ++;
                lastStepTimestamp = currentTimeStamp;
                break;
            case Sensor.TYPE_GYROSCOPE:
                lastGyroscope = event.values.clone();
                lastGyroscopeTimestamp = currentTimeStamp;
                //pdr.processGyroscopeData(event.values);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastMagnetometer = event.values.clone();
                lastMagnetometerTimestamp = currentTimeStamp;
                break;
            case Sensor.TYPE_LIGHT:
                lastLight = event.values[0];
                lastLightTimestamp = currentTimeStamp;
                break;
            case Sensor.TYPE_PROXIMITY:
                lastProximity = event.values[0];
                lastProximityTimestamp = currentTimeStamp;
                break;
            case Sensor.TYPE_PRESSURE:
                lastBarometer = event.values[0];
                lastBarometerTimestamp = currentTimeStamp;
                break;
        }


        SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        //Data_manager.collectData();
        //TODO where to add the position
        //pdr.updatePosition();
        //pdrView.addPosition(pdr.getCurrentPosition());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onLocationChanged(Location location) {
        // Location data obtained here
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Not used
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Not used
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not used
    }

    public float[] getLastAccelerometer() {
        return lastAccelerometer;
    }

    public long getLastAccelerometerTimestamp() {
        return lastAccelerometerTimestamp;
    }

    public List<ScanResult> getLastWifiScanResult() {
        return wifiScanResults;
    }

    public long getLastWifiScanTimestamp() {
        return lastWifiScanTimestamp;
    }

    public Location getLastLocation() {
        return LocationFullInfo;
    }

    public long getLastLocationTimestamp() {
        return lastLocationTimestamp;
    }

    public int getLastStep() {
        return lastStep;
    }

    public long getLastStepTimestamp() {
        return lastStepTimestamp;
    }

    public float getLastBarometer() {
        return lastBarometer;
    }

    public long getLastBarometerTimestamp() {
        return lastBarometerTimestamp;
    }

    public float[] getLastGyroscope() {
        return lastGyroscope;
    }

    public long getLastGyroscopeTimestamp() {
        return lastGyroscopeTimestamp;
    }

    public String getBssid(){
        return bssid;
    }

    public float[] getLastMagnetometer() {
        return lastMagnetometer;
    }

    public long getLastMagnetometerTimestamp() {
        return lastMagnetometerTimestamp;
    }

    public float getLastLight() {
        return lastLight;
    }

    public LOCATION getLocationInfo() {
        return LocationInfo;
    }

    public long getLastLightTimestamp() {
        return lastLightTimestamp;
    }

    public float[] getQuaternion(){
        return initialQuaternion;
    }

    public float[] getRotationMatrix(){
        return rotationMatrix;
    }

    public float[] getOrientation(){
        return orientationAngles;
    }

    public float getLastProximity() {
        return lastProximity;
    }

    public long getLastProximityTimestamp() {
        return lastProximityTimestamp;
    }

}




