package com.example.ips;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;



public class Recording_frag extends Fragment implements SensorEventListener {
    private GraphView Graph;
    private SensorDataCollector data_collector;
    private Viewport View;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor barometer;
    private Sensor ambientLightSensor;
    private Sensor proximitySensor;
    private Sensor gravitySensor;
    private FragmentManager fm=null ;
    private FragmentTransaction transaction =null ;
    private float time = 0;

    private double lastTimestamp=0;
    private double mLastXvalue=0;
    float accelerometerValues = 0;
    final float alpha = (float) 0.8;
    private float gravity [] = new float[3];
    private long accTimestamp,magTimestamp,gyrTimestamp,barTimestamp,prxTimestamp,lightTimestamp,gravityTimestamp;
    private boolean timeRecord=false;
    // Constants for sampling
    private final int SENSOR_RATE = 100; // Hz
    private final int TARGET_RATE = 5; // Hz
    private final int SAMPLE_PERIOD_MS = 1000 / TARGET_RATE; // Milliseconds

    // Variables for sampling
    private float[] mSensorBuffer = new float[SENSOR_RATE / TARGET_RATE];
    private int mSensorBufferIndex = 0;
    //graphmenu
    String[] SensorList = { "Accelerometer", "Gyroscope",
            "Rotation", "Barometer"};
    Spinner graphmenu;
    String currentDisplaySensor;

    LineGraphSeries<DataPoint> accSeriesx = new LineGraphSeries<>();
    LineGraphSeries<DataPoint> accSeriesy = new LineGraphSeries<>();
    LineGraphSeries<DataPoint> accSeriesz = new LineGraphSeries<>();
    //UI
    private ImageView imageview1;
    private ImageView imageview2;
    private ImageView imageview3;
    private ImageView imageview4;
    private ImageView imageview5;
    private ImageView imageview6;
    private TextView xvalue;
    private TextView yvalue;
    private TextView zvalue;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_graph, container, false);
        Graph = (GraphView) view.findViewById(R.id.accGraph);
        xvalue = (TextView)view.findViewById(R.id.xValue);
        yvalue = (TextView)view.findViewById(R.id.yValue);
        zvalue = (TextView)view.findViewById(R.id.zValue);
        imageview1 = (ImageView) view.findViewById(R.id.imageView);
        imageview2 = (ImageView) view.findViewById(R.id.imageView2);
        imageview3 = (ImageView) view.findViewById(R.id.imageView3);
        imageview4 = (ImageView) view.findViewById(R.id.imageView4);
        imageview5 = (ImageView) view.findViewById(R.id.imageView5);
        imageview6 = (ImageView) view.findViewById(R.id.imageView6);

        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        data_collector = Switch.data_collector_top;
        //plot data
        setupGraph();
        // graphmenu
        graphmenu = view.findViewById(R.id.spinner2);
        spinnierInitialization();
        return view;
    }

    @Override
    public void onResume() {
        super. onResume();
        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, gyroscope, 10000);
        sensorManager.registerListener(this, magnetometer, 10000);
        sensorManager.registerListener(this, barometer, 10000);
        sensorManager.registerListener(this, ambientLightSensor, 10000);
        sensorManager.registerListener(this, proximitySensor, 10000);
        sensorManager.registerListener(this, gravitySensor, 10000);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER&currentDisplaySensor=="Accelerometer"){
            //plot graph
            float[] accData = data_collector.getLastAccelerometer();
            // Add a high pass filter (Alpha contributed) to get the real acceleration.

            gravity[0] = alpha * gravity[0] + (1 - alpha) * accData[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * accData[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * accData[2];

            float x, y, z;
            x = accData[0]- gravity[0];
            y = accData[1] - gravity[1];
            z = accData[2] - gravity[2];
            // Update the data

            DrawGraph(x,y,z,time);
            time += 1;
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE&(currentDisplaySensor=="Gyroscope")){
            float[] accData = data_collector.getLastGyroscope();

            DrawGraph(accData[0],accData[1],accData[2],time);
            time += 1;
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD&(currentDisplaySensor=="Rotation")){
            float[] accData = data_collector.getLastMagnetometer();

            DrawGraph(accData[0],accData[1],accData[2],time);
            time += 1;
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE&(currentDisplaySensor=="Barometer")){
            float accData = data_collector.getLastBarometer();
            //DrawGraph(accData,0,0,time);

            accSeriesx.appendData(new DataPoint(time, accData), true, 100);
            xvalue.setText(Math.round(accData * 10000d) / 10000d + "");
            yvalue.setText(Math.round(0 * 10000d) / 10000d + "");
            zvalue.setText(Math.round(0 * 10000d) / 10000d + "");

            time += 1;

        }
    }
    private void DrawGraph(float xValue, float yValue, float zValue, float time1 ){
        accSeriesx.appendData(new DataPoint(time1, xValue), true, 100);
        accSeriesy.appendData(new DataPoint(time1, yValue), true, 100);
        accSeriesz.appendData(new DataPoint(time1, zValue), true, 100);
        xvalue.setText(Math.round(xValue * 10000d) / 10000d + "");
        yvalue.setText(Math.round(yValue * 10000d) / 10000d + "");
        zvalue.setText(Math.round(zValue * 10000d) / 10000d + "");
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void spinnierInitialization() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, SensorList);  //默认文件？
        graphmenu.setAdapter(adapter);
        graphmenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);

                if(currentDisplaySensor!=(String) parent.getItemAtPosition(position)){
                    currentDisplaySensor=(String) parent.getItemAtPosition(position);
                    accSeriesx.resetData(new DataPoint[0]);
                    accSeriesy.resetData(new DataPoint[0]);
                    accSeriesz.resetData(new DataPoint[0]);
                    time=0;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
    private void setupGraph() {

        //   accSeriesx = new LineGraphSeries<>();
       // accSeriesx.setTitle("X-axis");
        //    accSeriesy = new LineGraphSeries<>();
       // accSeriesy.setTitle("Y-axis");
        //    accSeriesz = new LineGraphSeries<>();
       // accSeriesz.setTitle("Z-axis");
        accSeriesx.setColor(Color.parseColor("#A02422")); // red
        accSeriesy.setColor(Color.parseColor("#63AB62")); // green
        accSeriesz.setColor(Color.parseColor("#DD7500")); // orange

        Graph.addSeries(accSeriesx);
        Graph.addSeries(accSeriesy);
        Graph.addSeries(accSeriesz);

        Graph.getViewport().setXAxisBoundsManual(true);
        Graph.getViewport().setMinX(0);
        Graph.getViewport().setMaxX(100);

     //   Graph.getLegendRenderer().setVisible(true);
    }
}
