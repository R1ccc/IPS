package com.example.ips;
import org.apache.commons.math3.complex.Quaternion;
public class MahonyAHRS {
    private float q0, q1, q2, q3;
    private float beta;
    private static final float DEFAULT_SAMPLE_FREQUENCY = 100.0f;
    private static final float DEFAULT_KP = 1.0f;
    private static final float DEFAULT_KI = 0.0f;

    private float sampleFrequency;
    private float kp;
    private float ki;
    private float[] integralError = new float[3];
    private Quaternion quaternion;

    public MahonyAHRS(float sampleFrequency, float kp, float ki, float q0, float q1, float q2, float q3) {
        this.sampleFrequency = sampleFrequency;
        this.kp = kp;
        this.ki = ki;
        this.quaternion = new Quaternion(q0, q1, q2, q3);
    }

    public void update(float gx, float gy, float gz, float ax, float ay, float az, float mx, float my, float mz) {
        float norm;
        float hx, hy, bx, bz;
        float vx, vy, vz, wx, wy, wz;
        float ex, ey, ez;

        q0 = (float) quaternion.getQ0();
        q1 = (float) quaternion.getQ1();
        q2 = (float) quaternion.getQ2();
        q3 = (float) quaternion.getQ3();

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalization)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalize accelerometer measurement
            norm = (float) Math.sqrt(ax * ax + ay * ay + az * az);
            ax /= norm;
            ay /= norm;
            az /= norm;

            // Estimated direction of gravity
            vx = 2.0f * (q1 * q3 - q0 * q2);
            vy = 2.0f * (q0 * q1 + q2 * q3);
            vz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3;

            // Error is cross product between estimated direction and measured direction of gravity
            ex = (ay * vz - az * vy) * kp;
            ey = (az * vx - ax * vz) * kp;
            ez = (ax * vy - ay * vx) * kp;

            // Compute and apply the integral
            // feedback term (proportional to the integral of the error)
            if (ki > 0.0f) {
                integralError[0] += ex * ki * (1.0f / sampleFrequency); // integral error scaled by Ki
                integralError[1] += ey * ki * (1.0f / sampleFrequency);
                integralError[2] += ez * ki * (1.0f / sampleFrequency);

                gx += integralError[0]; // apply integral feedback
                gy += integralError[1];
                gz += integralError[2];
            } else {
                integralError[0] = 0.0f; // prevent integral windup
                integralError[1] = 0.0f;
                integralError[2] = 0.0f;
            }

            // Apply proportional feedback
            gx += ex;
            gy += ey;
            gz += ez;
        }

        // Integrate the rate of change of quaternion
        gx *= (0.5f * (1.0f / sampleFrequency)); // pre-multiply common factors
        gy *= (0.5f * (1.0f / sampleFrequency));
        gz *= (0.5f * (1.0f / sampleFrequency));

        Quaternion qDot = new Quaternion(q0, q1, q2, q3).multiply(new Quaternion(0, gx, gy, gz)); // compute quaternion derivative

        // Update quaternion
        q0 += qDot.getQ0();
        q1 += qDot.getQ1();
        q2 += qDot.getQ2();
        q3 += qDot.getQ3();

        // Normalize quaternion
        norm = (float) Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 /= norm;
        q1 /= norm;
        q2 /= norm;
        q3 /= norm;

        // Set the quaternion
        quaternion = new Quaternion(q0, q1, q2, q3);
    }

    //Finds the user's heading relative to their start location
    public double findHeading(){
        double heading = Math.atan2(2.0 * (q0 * q3 + q1 * q2), 1.0 - 2.0 * (q2 * q2 + q3 * q3));

        float headingDegrees = (float) Math.toDegrees(heading);
        return heading;
    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    // Add additional helper methods if necessary
}

