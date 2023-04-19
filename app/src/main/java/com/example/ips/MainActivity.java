package com.example.ips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends PermissionManager{
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
  /*  public static SensorDataCollector data_collector_top;
    public static Data_Manager data_manager_top;
    public static Data_Process data_process_top;*/
    private PDR pdr;
    private PDRView pdrView;
   // public Button record_start;
    private Button btnTrajectory;
    private TextView acc;
    //private View view = new View(this);
    private FragmentManager fm=null ;
    private FragmentTransaction transaction =null ;
    private Trajectory_draw trajectory_draw;
    @Override
    public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            //View view = inflater.inflate(R.layout.activity_main, container, false);
            setContentView(R.layout.activity_main);     //set layout file
        getSupportActionBar().hide();
        fm = getSupportFragmentManager();
        btnTrajectory = findViewById(R.id.btnTrajectory);
           // record_start = findViewById(R.id.record_start);
        /*    data_collector_top = new SensorDataCollector(this, data_manager_top);
            data_manager_top = new Data_Manager(data_collector_top);
            data_process_top = new Data_Process(data_manager_top);*/
//            record_start.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //Intent intent = new Intent(this, RecordStart.class);
//                    startActivity(new Intent(getApplicationContext(),RecordStart.class));
//                }
//            });
        // data_manager_top.collectData();
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.PDR);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.PDR:

                        return true;

                    case R.id.Sensor:
                        startActivity(new Intent(getApplicationContext(),Switch.class));
                        overridePendingTransition(0,0);
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

  /*  public void start_record(View view){
        startActivity(new Intent(getApplicationContext(),RecordStart.class));
    }*/

    //@Override
       public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            pdr.processAccelerometerData(event.values);
//        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            pdr.processGyroscopeData(event.values);
//        }
//
//        // Calculate the user's position and update trajectory
//        pdr.updatePosition();
//
//        // Display the trajectory on a map or custom view
//        pdrView.addPosition(pdr.getCurrentPosition());
    }
    public void onClick(View v) {
        transaction = fm.beginTransaction();
        trajectory_draw =new Trajectory_draw();
        transaction.replace(R.id.fragment,trajectory_draw);
        transaction.commit();
    }
    //@Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }


    @Override
    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
    };

    @Override
    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    };
}
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//
//    public void startRecording(){
//    }
//}