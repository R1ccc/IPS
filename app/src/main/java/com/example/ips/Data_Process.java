package com.example.ips;
/*
    Use the data from data collector to do processing
 */

import static android.hardware.SensorManager.getAltitude;

import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Data_Process extends MainActivity{
    private Data_Manager data_manager;

    private float pressure;

    //constant used for calculation
    private static final float ALPHA = 0.8f;  // 用于低通滤波器的系数
    private static final double GRAVITY = 9.81; // 重力加速度
    private static final float PEAK_THRESHOLD = 9.5f; // 峰值检测阈值
    private static final float STEP_LENGTH_FACTOR = 0.7f; // 步长因子，可以根据实际需求
    private static final float FLOOR_CHANGE_THRESHOLD_MAG = 15.0f; // 磁场强度楼层变化阈值
    private static final float FLOOR_CHANGE_THRESHOLD_PRESSURE = 2.0f; // 气压楼层变化阈值，单位：百帕（hPa）
    private static final double K = 0.45; // Adjust this constant based on your specific use case
    private static final double N = 1.0; // Adjust this constant based on your specific use case
    //processed data
    private List<float[]> trajectory = new ArrayList<>();
    private float stepLengthEstimation;
    private int currentFloor;
    private float[] lastPosition = new float[]{0, 0}; // 上一个位置
    private float[] curPosition = new float[]{0, 0};  // 当前位置

    public Data_Process(Data_Manager data_manager){
        this.data_manager = data_manager;
        pressure = data_manager.sensorDataCollector.getLastBarometer();
    }

    //data processing--Trajectory calculation
    private void calculateTrajectory(List<float[]> accelerometerDataList) {

        float[] filteredAccData = new float[3];
        float[] velocity = new float[3];
        float[] position = new float[3];

        for (float[] accData : accelerometerDataList) {
            // 对加速度计数据进行低通滤波
            filteredAccData[0] = ALPHA * filteredAccData[0] + (1 - ALPHA) * accData[0];
            filteredAccData[1] = ALPHA * filteredAccData[1] + (1 - ALPHA) * accData[1];
            filteredAccData[2] = ALPHA * filteredAccData[2] + (1 - ALPHA) * accData[2];

            // 去除重力影响
            float[] linearAcc = new float[3];
            linearAcc[0] = accData[0] - filteredAccData[0];
            linearAcc[1] = accData[1] - filteredAccData[1];
            linearAcc[2] = accData[2] - filteredAccData[2];

            // 对加速度数据进行积分以得到速度
            float deltaTime = 1; // 假设恒定的时间间隔，您需要根据实际情况计算
            velocity[0] += linearAcc[0] * deltaTime;
            velocity[1] += linearAcc[1] * deltaTime;
            velocity[2] += linearAcc[2] * deltaTime;

            // 对速度数据进行积分以得到位置
            position[0] += velocity[0] * deltaTime;
            position[1] += velocity[1] * deltaTime;
            position[2] += velocity[2] * deltaTime;

            // 将计算出的位置添加到轨迹中
            trajectory.add(position.clone());
        }
        return;
    }

    //data processing--step length estimation calculation
    public float getStepLength() {
        double height = 1.7; // Replace with the height of the individual in meters
        //List<float[]> accelerometerData = data_manager_top.getAccelerometerDataList(); // Replace with your method to collect accelerometer data
        double verticalDisplacement = calculateVerticalDisplacement(data_manager.getAccelerometerDataList()); // Replace with your method to calculate vertical displacement
        double stepLength = estimateStepLength(height, verticalDisplacement);
        return (float) stepLength;
    }

    private static double calculateVerticalDisplacement(List<float[]> accelerometerData) {
        if (accelerometerData.isEmpty()) {
            throw new IllegalArgumentException("Accelerometer data cannot be empty.");
        }

        double maxVerticalAcceleration = Double.NEGATIVE_INFINITY;
        double minVerticalAcceleration = Double.POSITIVE_INFINITY;

        for (float[] acceleration : accelerometerData) {
            float verticalAcceleration = acceleration[2]; // Assuming Z-axis is the vertical component
            maxVerticalAcceleration = Math.max(maxVerticalAcceleration, verticalAcceleration);
            minVerticalAcceleration = Math.min(minVerticalAcceleration, verticalAcceleration);
        }

        double verticalAccelerationDifference = maxVerticalAcceleration - minVerticalAcceleration;

        // Assume vertical displacement is proportional to the difference in vertical acceleration
        // You may need to adjust the proportionality constant depending on your specific use case
        double proportionalityConstant = 0.1;
        return proportionalityConstant * verticalAccelerationDifference;
    }

    private static double estimateStepLength(double height, double verticalDisplacement) {
        return K * Math.pow(height * verticalDisplacement, N);
    }

    //data processing--floor change estimation calculation
    private long previousTime = System.currentTimeMillis();
    private List<Float> altitudeHistory = new ArrayList<>();        // collected altitude values in history
    public void determineFloor() {
        // Firstly add the values to the value filter. This is used
        // to smooth them. 20 values are being smoothed.
        final float alpha = 0.9f;

            pressure = alpha * data_manager.sensorDataCollector.getLastBarometer() + (1 - alpha) * pressure;

        // get the current time and check that half of a second passed since the last time the sensor was updated.
        long currTime = System.currentTimeMillis();
        if (previousTime + 500 < currTime){
            // Get the averaged value fromthe filter list
//            float millibars_of_pressure = calculateAverage(pressureFilter);
            // Use Google provided formula for finding the altitude from pressure values
            float altitude = getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);

            // Collect 30 values in the altitude history list, which is equivalent to 15 seconds of capturing
            altitudeHistory.add(altitude);
            if (altitudeHistory.size() > 30){
                altitudeHistory.remove(0);
            }

            // first is the value at the moment, last is value 15 seconds before now.
            float first = altitude;
            float last = altitudeHistory.get(0);

            // Check that if the value difference is 2m, then determine if user went up or down.
            // Call the listener method.
            if (first - last > 2) {
                currentFloor++;
                // remove all of the values because floor was updated!
                altitudeHistory.clear();
            }else if (first - last < -2) {
                currentFloor--;
                altitudeHistory.clear();
            }
            // update the previous time
            previousTime = currTime;
            //update pressure
            pressure = data_manager.sensorDataCollector.getLastBarometer();
        }
    }


    public List<float[]> getTrajectoryDataList() {
        return trajectory;
    }

    public float getEstimatedStepLength(){
        return stepLengthEstimation;
    }

    public int getCurrentFloor(){
        return currentFloor;
    }
}
