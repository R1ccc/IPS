package com.example.ips;

import android.location.Location;
import android.net.wifi.ScanResult;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data_Manager extends AppCompatActivity {

    public SensorDataCollector sensorDataCollector;

    private List<float[]> accelerometerDataList = new ArrayList<>();
    private List<Long> accelerometerTimestampList = new ArrayList<>();

    private List<float[]> gyroscopeDataList = new ArrayList<>();
    private List<Long> gyroscopeTimestampList = new ArrayList<>();

    private List<float[]> magnetometerDataList = new ArrayList<>();
    private List<Long> magnetometerTimestampList = new ArrayList<>();

    private List<Float> lightDataList = new ArrayList<>();
    private List<Long> lightTimestampList = new ArrayList<>();

    private List<Float> proximityDataList = new ArrayList<>();
    private List<Long> proximityTimestampList = new ArrayList<>();

    private List<Float> barometerDataList = new ArrayList<>();
    private List<Long> barometerTimestampList = new ArrayList<>();

    private List<LOCATION> locationDataList = new ArrayList<>();
    //add all wifi scaned in DataCollector to a list;
    private List<HashMap> wifiScanResultsList = new ArrayList<>();
    private List<Long> wifiScanTimestampList = new ArrayList<>();

    public Data_Manager(SensorDataCollector sensorDataCollector) {
        this.sensorDataCollector = sensorDataCollector;
    }

    public void startCollecting() {
        sensorDataCollector.startCollecting();
    }

    public void stopCollecting() {
        sensorDataCollector.stopCollecting();
    }

    public void collectData() {
        long currentTime = System.currentTimeMillis();

        float[] accelerometerData = sensorDataCollector.getLastAccelerometer();
        if (accelerometerData != null) {
            accelerometerDataList.add(accelerometerData);
            accelerometerTimestampList.add(sensorDataCollector.getLastAccelerometerTimestamp());
        }

        float[] gyroscopeData = sensorDataCollector.getLastGyroscope();
        if (gyroscopeData != null) {
            gyroscopeDataList.add(gyroscopeData);
            gyroscopeTimestampList.add(sensorDataCollector.getLastGyroscopeTimestamp());
        }

        float[] magnetometerData = sensorDataCollector.getLastMagnetometer();
        if (magnetometerData != null) {
            magnetometerDataList.add(magnetometerData);
            magnetometerTimestampList.add(sensorDataCollector.getLastMagnetometerTimestamp());
        }

        float lightData = sensorDataCollector.getLastLight();
        if (lightData != 0) {
            lightDataList.add(lightData);
            lightTimestampList.add(sensorDataCollector.getLastLightTimestamp());
        }

        float proximityData = sensorDataCollector.getLastProximity();
        if (proximityData != 0) {
            proximityDataList.add(proximityData);
            proximityTimestampList.add(sensorDataCollector.getLastProximityTimestamp());
        }

        float barometerData = sensorDataCollector.getLastBarometer();
        if (barometerData != 0) {
            barometerDataList.add(barometerData);
            barometerTimestampList.add(sensorDataCollector.getLastBarometerTimestamp());
        }

        LOCATION locationData = sensorDataCollector.getLocationInfo();
        if (locationData != null) {
            locationDataList.add(locationData);
        }

        HashMap wifiScanResults = sensorDataCollector.WifiInfo;
        if (!wifiScanResults.isEmpty()) {
            wifiScanResultsList.add(wifiScanResults);
            wifiScanTimestampList.add(currentTime);
        }
    }

    public List<float[]> getAccelerometerDataList() {
        return accelerometerDataList;
    }

    public List<Long> getAccelerometerTimestampList() {
        return accelerometerTimestampList;
    }

    public List<float[]> getGyroscopeDataList() {
        return gyroscopeDataList;
    }

    public List<Long> getGyroscopeTimestampList() {
        return gyroscopeTimestampList;
    }

    public List<float[]> getMagnetometerDataList() {
        return magnetometerDataList;
    }

    public List<Long> getMagnetometerTimestampList() {
        return magnetometerTimestampList;
    }

    public List<Float> getLightDataList() {
        return lightDataList;
    }

    public List<Long> getLightTimestampList() {
        return lightTimestampList;
    }
    public List<Float> getProximityDataList() {
        return proximityDataList;
    }

    public List<Long> getProximityTimestampList() {
        return proximityTimestampList;
    }

    public List<LOCATION> getLocationDataList() {
        return locationDataList;
    }

    public List<HashMap> getWifiScanResultsList() {
        return wifiScanResultsList;
    }

    public List<Long> getWifiScanTimestampList() {
        return wifiScanTimestampList;
    }
}

