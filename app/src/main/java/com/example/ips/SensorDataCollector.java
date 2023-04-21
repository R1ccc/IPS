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
    private Sensor RotationVector;
    LocationListener locationListener;

    private String name;
    private String vendor;
    private float res;
    private float power;
    private int version;

    private Context context ;
    private float[] lastAccelerometer = new float[3];
    private float[] lastGyroscope = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float lastLight;
    private float lastProximity;
    private int lastStep = 0;
    private float lastBarometer;
    //real time location, always refreshing
    private LOCATION LocationInfo;
    private Location LocationFullInfo;
    private String bssid;
    private long StartTime;
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
    //Top trajectory
    public TrajectoryOuterClass.Trajectory.Builder TrajectoryTop;
    //Trajectory related builder
    private TrajectoryOuterClass.Pressure_Sample.Builder PressureSampleBuilder;
    private TrajectoryOuterClass.Position_Sample.Builder PositionSampleBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder AccBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder MagBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder GyroBuilder;
    private TrajectoryOuterClass.Light_Sample.Builder LightSampleBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder ProxBuilder;
    private TrajectoryOuterClass.Motion_Sample.Builder MotionSampleBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder BaroBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder LightBuilder;
    private TrajectoryOuterClass.Sensor_Info.Builder RotationBuilder;
    //Wifi related builder
    private TrajectoryOuterClass.WiFi_Sample.Builder WifiSampleBuilder;
    private TrajectoryOuterClass.Mac_Scan.Builder MacScanBuilder;
    private TrajectoryOuterClass.AP_Data.Builder APDataBuilder;
    //GPS related builder
    private TrajectoryOuterClass.GNSS_Sample.Builder GNSSSampleBuilder;
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
        RotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new DCLocationListener();

        //initialize
        TrajectoryTop = TrajectoryOuterClass.Trajectory.newBuilder();
        PressureSampleBuilder = TrajectoryOuterClass.Pressure_Sample.newBuilder();
        LightSampleBuilder = TrajectoryOuterClass.Light_Sample.newBuilder();
        MotionSampleBuilder = TrajectoryOuterClass.Motion_Sample.newBuilder();
        PositionSampleBuilder = TrajectoryOuterClass.Position_Sample.newBuilder();
        WifiSampleBuilder = TrajectoryOuterClass.WiFi_Sample.newBuilder();
        MacScanBuilder = TrajectoryOuterClass.Mac_Scan.newBuilder();
        APDataBuilder = TrajectoryOuterClass.AP_Data.newBuilder();
        GNSSSampleBuilder = TrajectoryOuterClass.GNSS_Sample.newBuilder();
        //Sensor info builder initialization
        AccBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();
        MagBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();
        GyroBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();
        ProxBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();
        BaroBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();
        RotationBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();
        LightBuilder = TrajectoryOuterClass.Sensor_Info.newBuilder();

        //build sensor info builder
        setAccInfo();
        setMagInfo();
        setBaroInfo();
        setLightInfo();
        setGyroInfo();
        setRotationInfo();
        //add sensor info into TrajectoryTop
        TrajectoryTop.setAccelerometerInfo(AccBuilder);
        TrajectoryTop.setRotationVectorInfo(RotationBuilder);
        TrajectoryTop.setBarometerInfo(BaroBuilder);
        TrajectoryTop.setLightSensorInfo(LightBuilder);
        TrajectoryTop.setGyroscopeInfo(GyroBuilder);
        TrajectoryTop.setMagnetometerInfo(MagBuilder);
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
                //change GNSS SAMPLE BUILDER
                GNSSSampleBuilder.setAccuracy(location.getAccuracy());
                GNSSSampleBuilder.setAltitude((float) location.getAltitude());
                GNSSSampleBuilder.setLatitude((float) location.getLatitude());
                GNSSSampleBuilder.setLongitude((float) location.getLongitude());
                GNSSSampleBuilder.setSpeed(location.getSpeed());
                GNSSSampleBuilder.setProvider(location.getProvider());
                GNSSSampleBuilder.setRelativeTimestamp(System.currentTimeMillis()-StartTime);
                GNSSSampleBuilder.build();
                TrajectoryTop.addGnssData(GNSSSampleBuilder);
                Log.e("Location:", String.valueOf(LocationInfo.longtitude) + ":" + String.valueOf(LocationInfo.latitude));
            }
            else Log.e("Location","=null");
        }
    }

    public void startCollecting() {
        StartTime = System.currentTimeMillis();
        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, stepdetector, 10000);
        sensorManager.registerListener(this, gyroscope, 10000);
        sensorManager.registerListener(this, magnetometer, 10000);
        sensorManager.registerListener(this, barometer, 10000);
        //sensorManager.registerListener(this, ambientLightSensor, 10000);
        sensorManager.registerListener(this, proximitySensor, 10000);
        sensorManager.registerListener(this, RotationVector, 10000); // 100 Samples/s
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
        //When recording stops, build TrajectoryTop
        TrajectoryTop.build();
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
                    MacScanBuilder.setMac(Long.parseLong(bssid.replace(":",""),16));
                    MacScanBuilder.setRssi(level);
                    MacScanBuilder.setRelativeTimestamp(System.currentTimeMillis()-StartTime);
                    MacScanBuilder.build();
                    APDataBuilder.setMac(Long.parseLong(bssid.replace(":",""),16));
                    APDataBuilder.setSsid(ssid);
                    APDataBuilder.setFrequency(freq);
                    APDataBuilder.build();
                    //TODO When to build wifisample?
                    WifiSampleBuilder.setRelativeTimestamp(System.currentTimeMillis()-StartTime);
                    WifiSampleBuilder.addMacScans(MacScanBuilder);
                    WifiSampleBuilder.build();
                    TrajectoryTop.addWifiData(WifiSampleBuilder);
                    TrajectoryTop.addApsData(APDataBuilder);
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

        TrajectoryOuterClass.Sensor_Info newSensor;
        //update wifi info
        WIFI_update();
        sensorManager.getOrientation(rotationMatrix, orientationAngles);
        sensorManager.getQuaternionFromVector(initialQuaternion, rotationMatrix);
        //Log.i("Orientation WHEN INIT:", String.valueOf(orientationAngles[0]));
        boolean Acc_flag = false;
        boolean Gyro_flag = false;
        boolean Rot_flag = false;
        boolean Step_flag = false;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values.clone();
                lastAccelerometerTimestamp = currentTimeStamp;
                //change motion sample
                MotionSampleBuilder.setAccX(lastAccelerometer[0]);
                MotionSampleBuilder.setAccY(lastAccelerometer[1]);
                MotionSampleBuilder.setAccY(lastAccelerometer[2]);
                Acc_flag = true;
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                lastStep ++;
                lastStepTimestamp = currentTimeStamp;

                MotionSampleBuilder.setStepCount(lastStep);
                Step_flag = true;

                break;
            case Sensor.TYPE_GYROSCOPE:
                lastGyroscope = event.values.clone();
                lastGyroscopeTimestamp = currentTimeStamp;

                MotionSampleBuilder.setGyrX(lastGyroscope[0]);
                MotionSampleBuilder.setGyrY(lastGyroscope[1]);
                MotionSampleBuilder.setGyrZ(lastGyroscope[2]);
                Gyro_flag = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastMagnetometer = event.values.clone();
                lastMagnetometerTimestamp = currentTimeStamp;

                //change position ssample builder
                PositionSampleBuilder.setMagX(lastMagnetometer[0]);
                PositionSampleBuilder.setMagY(lastMagnetometer[1]);
                PositionSampleBuilder.setMagZ(lastMagnetometer[2]);
                PositionSampleBuilder.setRelativeTimestamp(System.currentTimeMillis()-StartTime);
                PositionSampleBuilder.build();
                TrajectoryTop.addPositionData(PositionSampleBuilder);
                break;
            case Sensor.TYPE_LIGHT:
                lastLight = event.values[0];
                lastLightTimestamp = currentTimeStamp;

                //change light sample builder
                LightSampleBuilder.setLight(lastLight);
                LightSampleBuilder.setRelativeTimestamp(System.currentTimeMillis()-StartTime);
                LightSampleBuilder.build();
                TrajectoryTop.addLightData(LightSampleBuilder);
                break;
            case Sensor.TYPE_PROXIMITY:
                lastProximity = event.values[0];
                lastProximityTimestamp = currentTimeStamp;

                break;
            case Sensor.TYPE_PRESSURE:
                lastBarometer = event.values[0];
                lastBarometerTimestamp = currentTimeStamp;
                PressureSampleBuilder.setPressure(lastBarometer);
                PressureSampleBuilder.setRelativeTimestamp(System.currentTimeMillis()-StartTime);
                PressureSampleBuilder.build();
                TrajectoryTop.addPressureData(PressureSampleBuilder);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:

                //change motion sample
                MotionSampleBuilder.setRotationVectorX(event.values[0]);
                MotionSampleBuilder.setRotationVectorY(event.values[1]);
                MotionSampleBuilder.setRotationVectorZ(event.values[2]);
                MotionSampleBuilder.setRotationVectorW(event.values[3]);
                Rot_flag = true;
                break;
        }
        //only when all four sensors are sampled then we set the relative time
        if (Rot_flag & Acc_flag & Gyro_flag & Step_flag){
            MotionSampleBuilder.setRelativeTimestamp(System.currentTimeMillis() - StartTime);
            MotionSampleBuilder.build();
            TrajectoryTop.addImuData(MotionSampleBuilder);
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        //Data_manager.collectData();
        //TODO where to add the position
        //pdr.updatePosition();
        //pdrView.addPosition(pdr.getCurrentPosition());
    }

    private TrajectoryOuterClass.Sensor_Info createSensorInfo(String name, String vendor, float res, float power, int version, int type){
        TrajectoryOuterClass.Sensor_Info.Builder newSensor = TrajectoryOuterClass.Sensor_Info.newBuilder();
        newSensor.setName(name);
        newSensor.setVendor(vendor);
        newSensor.setResolution(res);
        newSensor.setPower(power);
        newSensor.setVersion(version);
        newSensor.setType(type);

        return newSensor.build();
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

    private void setAccInfo(){
        name = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getName();
        vendor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getVendor();
        res = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getResolution();
        power = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getPower();
        version = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getVersion();
        AccBuilder.setName(name);
        AccBuilder.setVendor(vendor);
        AccBuilder.setResolution(res);
        AccBuilder.setPower(power);
        AccBuilder.setVersion(version);
        AccBuilder.setType(Sensor.TYPE_ACCELEROMETER);
        AccBuilder.build();
    }

    private void setMagInfo(){
        name = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).getName();
        vendor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).getVendor();
        res = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).getResolution();
        power = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).getPower();
        version = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).getVersion();
        MagBuilder.setName(name);
        MagBuilder.setVendor(vendor);
        MagBuilder.setResolution(res);
        MagBuilder.setPower(power);
        MagBuilder.setVersion(version);
        MagBuilder.setType(Sensor.TYPE_MAGNETIC_FIELD);
        MagBuilder.build();
    }

    private void setGyroInfo(){
        name = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getName();
        vendor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getVendor();
        res = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getResolution();
        power = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getPower();
        version = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getVersion();
        GyroBuilder.setName(name);
        GyroBuilder.setVendor(vendor);
        GyroBuilder.setResolution(res);
        GyroBuilder.setPower(power);
        GyroBuilder.setVersion(version);
        GyroBuilder.setType(Sensor.TYPE_GYROSCOPE);
        GyroBuilder.build();
    }

    private void setRotationInfo(){
        name = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).getName();
        vendor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).getVendor();
        res = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).getResolution();
        power = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).getPower();
        version = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).getVersion();
        RotationBuilder.setName(name);
        RotationBuilder.setVendor(vendor);
        RotationBuilder.setResolution(res);
        RotationBuilder.setPower(power);
        RotationBuilder.setVersion(version);
        RotationBuilder.setType(Sensor.TYPE_ROTATION_VECTOR);
        RotationBuilder.build();
    }

    private void setLightInfo(){
        name = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getName();
        vendor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getVendor();
        res = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getResolution();
        power = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getPower();
        version = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getVersion();
        LightBuilder.setName(name);
        LightBuilder.setVendor(vendor);
        LightBuilder.setResolution(res);
        LightBuilder.setPower(power);
        LightBuilder.setVersion(version);
        LightBuilder.setType(Sensor.TYPE_LIGHT);
        LightBuilder.build();
    }

    private void setBaroInfo(){
        name = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE).getName();
        vendor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE).getVendor();
        res = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE).getResolution();
        power = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE).getPower();
        version = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE).getVersion();
        BaroBuilder.setName(name);
        BaroBuilder.setVendor(vendor);
        BaroBuilder.setResolution(res);
        BaroBuilder.setPower(power);
        BaroBuilder.setVersion(version);
        BaroBuilder.setType(Sensor.TYPE_PRESSURE);
        BaroBuilder.build();
    }
}




