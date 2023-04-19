package com.example.ips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class Switch extends AppCompatActivity implements SensorEventListener {
    public Button record_start;
    private FragmentTransaction transaction =null ;
    private FragmentManager fm=null ;
    private Recording_frag record_frag;
    public static SensorDataCollector data_collector_top;
    public static Data_Manager data_manager_top;
    public static Data_Process data_process_top;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor barometer;
    private Sensor ambientLightSensor;
    private Sensor proximitySensor;
    private Sensor gravitySensor;
    private float[] accData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        getSupportActionBar().hide();
        fm = getSupportFragmentManager();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        data_collector_top = new SensorDataCollector(this);
        data_manager_top = new Data_Manager(data_collector_top);
        data_process_top = new Data_Process(data_manager_top);
        data_collector_top.startCollecting();
        data_manager_top.collectData();
       // data_manager_top.collectData();
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.Sensor);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.PDR:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.Sensor:
                        return true;

                    case R.id.cloud:
                        startActivity(new Intent(getApplicationContext(),SendtoCloud.class));
                        overridePendingTransition(0,0);
                        return true;
                }

                return false;
            }
        });
    }
    public void start_record(View view){
        transaction = fm.beginTransaction();
        record_frag=new Recording_frag();
        transaction.replace(R.id.fragment,record_frag);
        transaction.commit();
    }
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
    }

    public void onSensorChanged(SensorEvent event) {
        updateSensorData();
        data_manager_top.collectData();
        //floorplanView.updatePosition(pdr.getCurrentPosition());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private void updateSensorData() {
        accData = Switch.data_collector_top.getLastAccelerometer();
        Switch.data_manager_top.collectData();
//        pdr.calculateRelativePosition(accData);
//        pdr.updatePosition();
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
        //Log.i("CurPosition x:", String.valueOf(pdr.getCurrentPosition()[0]));
        //Log.i("CurPosition y:", String.valueOf(pdr.getCurrentPosition()[1]));

      //  tvSensorData.setText(Switch.data_collector_top.getBssid());
    }

    ;
}