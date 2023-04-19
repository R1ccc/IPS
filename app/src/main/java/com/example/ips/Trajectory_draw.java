package com.example.ips;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Locale;

public class Trajectory_draw extends Fragment implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor barometer;
    private Sensor ambientLightSensor;
    private Sensor proximitySensor;
    private Sensor gravitySensor;
    private Sensor stepDetector;//step detector
    public FloorplanView floorplanView;
    private Recording_frag record_frag;
    private FragmentManager fm=null ;
    private FragmentTransaction transaction =null ;
    TrajectoryOuterClass.Trajectory.Builder trajectory = TrajectoryOuterClass.Trajectory.newBuilder(); // Total Data Packet
    private PDR pdr;
    private float[] accData;
    private float[] magData;
    private float[] gyroData;
    private int stepDetect = 0;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trajectory_drawing, container, false);
        //View view = inflater.inflate(R.layout.activity_main, container, false);
        //setContentView(R.layout.record_start);     //set layout file
        //fm = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        //data_collector = new SensorDataCollector(this);
        //data_manager = new Data_Manager(data_collector);
        floorplanView = view.findViewById(R.id.floorplan_view);
        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        pdr = new PDR(floorplanView);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //test for traj
        byte[] bytes = trajectory.build().toByteArray();

        return view;
    }

    @Override
    public void onResume(){
        super. onResume();
        //sensorManager.registerListener(this, accelerometer, 60000); // 10000-100 samples/sec; 60000 - 20 samples/sec
        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, gyroscope, 10000);
        sensorManager.registerListener(this, magnetometer, 10000);
        sensorManager.registerListener(this, barometer, 10000);
        sensorManager.registerListener(this, ambientLightSensor, 10000);
        sensorManager.registerListener(this, proximitySensor, 10000);
        sensorManager.registerListener(this, gravitySensor,10000);
        sensorManager.registerListener(this, stepDetector, 10000);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){//when a step is detected, calculated new position
            stepDetect++;
            Log.i("Step:", String.valueOf(stepDetect));
            updateSensorData();//get current accelerometer data for postion calculatioin
            if (pdr.initialPosition[0] == 0 & pdr.initialPosition[1] == 0) {
                pdr.init();
            }
            pdr.calculateRelativePosition(accData, magData, gyroData);//calculate relative postion versus last pos
            pdr.updatePosition();//get absolute pos
            if (pdr.getCurrentPosition() != null) {
                Log.i("CurPosition x:", String.valueOf(pdr.getCurrentPosition()[0]));
                Log.i("CurPosition y:", String.valueOf(pdr.getCurrentPosition()[1]));
            }
            floorplanView.updatePosition(pdr.getCurrentPosition());//draw new pos
        }
    };

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void updateSensorData() {
        accData = Switch.data_collector_top.getLastAccelerometer();
        magData = Switch.data_collector_top.getLastMagnetometer();
        gyroData = Switch.data_collector_top.getLastGyroscope();
//        float[] gyroData = data_collector.getLastGyroscope();
//        float[] magData = data_collector.getLastMagnetometer();
//        float lightData = data_collector.getLastLight();
//        float proximityData = data_collector.getLastProximity();
//        float barometerData = data_collector.getLastBarometer();

        String sensorDataText = String.format(Locale.getDefault(),
                "Accelerometer: x=%.2f, y=%.2f, z=%.2f\n",accData[0], accData[1], accData[2]);
//        +
//                        "Gyroscope: x=%.2f, y=%.2f, z=%.2f\n" +
//                        "Magnetometer: x=%.2f, y=%.2f, z=%.2f\n" +
//                        "Light: %.2f\n" +
//                        "Proximity: %.2f\n" +
//                        "Barometer: %.2f",
//                accData[0], accData[1], accData[2],
//                gyroData[0], gyroData[1], gyroData[2],
//                magData[0], magData[1], magData[2],
//                lightData, proximityData, barometerData);
        //String acc_x = getaccData[0];

    }
}
