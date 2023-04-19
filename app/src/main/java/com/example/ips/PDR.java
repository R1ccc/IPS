package com.example.ips;
import android.hardware.SensorManager;
import android.util.Log;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

/*
    Using only gyro and acc to estimate the relative position
 */
public class PDR{
    private static final float STEP_THRESHOLD = 10.0f;
    private static final long MIN_TIME_BETWEEN_STEPS_MS = 250;
    private FloorplanView floorView;
    private float[] gravity = new float[3];
    private float[] gyroFiltered = new float[3];
    private float[] accFiltered = new float[3];
    private float[] magFiltered = new float[3];
    private float[] rotationVector = new float[3];
    private long lastStepTime = 0;
    private MahonyAHRS mahonyAHRS; //Algorithm
    private float stepLength = 0.7f; // get step length estimation from data_process


    public float[] initialPosition = {0,0};
    public float[] currentPosition;
    private double positionX = 0;
    private double positionY = 0;
    private float initHeading;
    private long lastStepTimestamp = 0;
    private static final int STEP_TIME_THRESHOLD = 250;


    //a constructor that takes a FloorplanView instance as a parameter
    //to pass the initial position chosen on image to PDR to draw trajectory
    public PDR(FloorplanView floorView) {
        this.floorView = floorView;
        stepLength = Switch.data_process_top.getStepLength();
        Log.i("Estimated SL :", String.valueOf(stepLength));
    }
    public void calculateRelativePosition(float[] accData, float[] magData, float[] gyroData) {
        final float alpha = 0.8f;
        double scaleX = 10;
        double scaleY = 10;
        // Low-pass filter to separate gravity from linear acceleration
        gravity[0] = alpha * gravity[0] + (1 - alpha) * accData[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * accData[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * accData[2];
        accFiltered[0] = alpha * accData[0] + (1 - alpha) * accFiltered[0];
        accFiltered[1] = alpha * accData[1] + (1 - alpha) * accFiltered[1];
        accFiltered[2] = alpha * accData[2] + (1 - alpha) * accFiltered[2];
        gyroFiltered[0] = alpha * gyroData[0] + (1 - alpha) * gyroFiltered[0];
        gyroFiltered[1] = alpha * gyroData[1] + (1 - alpha) * gyroFiltered[1];
        gyroFiltered[2] = alpha * gyroData[2] + (1 - alpha) * gyroFiltered[2];
        magFiltered[0] = alpha * magData[0] + (1 - alpha) * magFiltered[0];
        magFiltered[1] = alpha * magData[1] + (1 - alpha) * magFiltered[1];
        magFiltered[2] = alpha * magData[2] + (1 - alpha) * magFiltered[2];
        // Calculate linear acceleration
        float[] linearAcceleration = new float[3];
        linearAcceleration[0] = accData[0] - gravity[0];
        linearAcceleration[1] = accData[1] - gravity[1];
        linearAcceleration[2] = accData[2] - gravity[2];

        //implement MahonyAHRS to correct the heading
        mahonyAHRS.update(gyroFiltered[0], gyroFiltered[1], gyroFiltered[2],
                accFiltered[0], accFiltered[1], accFiltered[2],
                magFiltered[0], magFiltered[1], magFiltered[2]);
        double stepDirection = mahonyAHRS.findHeading();
        Log.i("Orientation AHRS:", String.valueOf(stepDirection));
        float stepDirection_nonAHRS = Switch.data_collector_top.getOrientation()[0];
        Log.i("Orientation :", String.valueOf(stepDirection_nonAHRS));
        positionX = stepLength * scaleX * Math.sin(stepDirection);
        positionY = stepLength * scaleY * Math.cos(stepDirection);
        //}
    }

    public void processGyroscopeData(float[] values) {
        // Simple integration of gyroscope data to estimate rotation
        rotationVector[0] += values[0] * SensorManager.GRAVITY_EARTH;
        rotationVector[1] += values[1] * SensorManager.GRAVITY_EARTH;
        rotationVector[2] += values[2] * SensorManager.GRAVITY_EARTH;
    }

    public void init(){
        initialPosition = floorView.getInitPos();
        Log.i("Init x-pos in PDR :", String.valueOf(floorView.getInitPos()[0]));
        currentPosition = initialPosition;
        //convert initial heading to initial quaternion
        initHeading = floorView.getInitHeading();
        double x = 0.0, y = 0.0, z = 1.0; // rotation axis is (0, 0, 1) for heading
        double sinHalfAngle = Math.sin(initHeading / 2.0);
        double cosHalfAngle = Math.cos(initHeading / 2.0);
        float xScalar = (float)(x * sinHalfAngle);
        float yScalar = (float)(y * sinHalfAngle);
        float zScalar = (float)(z * sinHalfAngle);
        float wScalar = (float)(cosHalfAngle);
        float[] initialQuaternion = {wScalar, xScalar, yScalar, zScalar};
        //float[] initialQuaternion = Switch.data_collector_top.getQuaternion();
        Log.i("Init heading :", String.valueOf(Math.atan2(2.0 * (Switch.data_collector_top.getQuaternion()[0] * Switch.data_collector_top.getQuaternion()[3] +
                Switch.data_collector_top.getQuaternion()[1] * Switch.data_collector_top.getQuaternion()[2]), 1.0 - 2.0 * (Switch.data_collector_top.getQuaternion()[2] *
                Switch.data_collector_top.getQuaternion()[2] + Switch.data_collector_top.getQuaternion()[3] * Switch.data_collector_top.getQuaternion()[3]))));
        // Create a MahonyAHRS instance with the initial quaternion
        mahonyAHRS = new MahonyAHRS(2f, 1.0f, 0.0f, initialQuaternion[0], initialQuaternion[1], initialQuaternion[2], initialQuaternion[3]);
    }

    public void updatePosition() {
        // Update the current position
        currentPosition[0] += positionX;
        currentPosition[1] += positionY;
        // Notify the UI or other components to update the trajectory
        // ...
    }

    public float[] getCurrentPosition() {
        return currentPosition;
    }


}

