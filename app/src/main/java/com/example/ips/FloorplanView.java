package com.example.ips;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FloorplanView extends View {

    private Bitmap floorplan;
    private Bitmap originalFloorplan;
    private Paint positionPaint;
    private Paint referencePaint;
    private Paint trajectoryPaint;
    private List<PointF> trajectoryPoints;
    private PointF currentPosition;
    private PointF referencePosition;
    public PointF initPosition;
    private float initX = 0;
    private float initY = 0;
    private float initX1 = 0;
    private float initY1 = 0;
    private float initHeading = 0;
    private int touchCount = 0;

    public FloorplanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        originalFloorplan = BitmapFactory.decodeResource(getResources(), R.drawable.nucleusground); // Replace with the resource ID of your floorplan image
        // Set the desired width and height for the floorplan
        int desiredWidth = 1024;
        int desiredHeight = 1535;

        // Create a scaled bitmap
        floorplan = Bitmap.createScaledBitmap(originalFloorplan, desiredWidth, desiredHeight, true);

        positionPaint = new Paint();
        positionPaint.setColor(Color.BLUE);
        positionPaint.setAntiAlias(true);

        referencePaint = new Paint();
        referencePaint.setColor(Color.RED);
        referencePaint.setAntiAlias(true);

        trajectoryPaint = new Paint();
        trajectoryPaint.setColor(Color.RED);
        trajectoryPaint.setAntiAlias(true);
        trajectoryPaint.setStrokeWidth(5);

        trajectoryPoints = new ArrayList<>();

        initPosition = new PointF();
        referencePosition = new PointF();
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchCount++;

                    if (touchCount == 1) {
                        initX = event.getX();
                        initY = event.getY();
                        initPosition.set(initX, initY);
                        Log.i("Initial X:", String.valueOf(initPosition.x));
                        Log.i("Initial y:", String.valueOf(initPosition.y));
                    } else if (touchCount == 2) {
                        initX1 = event.getX();
                        initY1 = event.getY();
                        initHeading = (float) Math.atan2((initY1 - initY), (initX1 - initX));
                        initHeading = (float) Math.toDegrees(initHeading) - 90;
                        initHeading = (float) -Math.toRadians(initHeading);
                        referencePosition.set(initX1, initY1);
                        Log.i("Initial X1:", String.valueOf(initX1));
                        Log.i("Initial y1:", String.valueOf(initY1));
                    }

                    invalidate();
                    return true;
                }
                return false;
            }
        });
        currentPosition = initPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0, 100);
        // Draw the floorplan image
        canvas.drawBitmap(floorplan, 0, 0, null);

        // Draw the trajectory
        for (int i = 0; i < trajectoryPoints.size() - 1; i++) {
            PointF point1 = trajectoryPoints.get(i);
            PointF point2 = trajectoryPoints.get(i + 1);
            canvas.drawLine(point1.x, point1.y, point2.x, point2.y, trajectoryPaint);
        }

        // Draw the current position
        Log.i("CurPosition x when drawing:", String.valueOf(currentPosition.x));
        Log.i("CurPosition y when drawing:", String.valueOf(currentPosition.y));
        canvas.drawCircle(currentPosition.x, currentPosition.y, 10, positionPaint);
        canvas.drawCircle(referencePosition.x, referencePosition.y, 10, referencePaint);
    }

    public void updatePosition(float[] position) {
        // Scale and translate the position coordinates to match the floorplan image
        // Replace these values with the appropriate scale and offset for your floorplan
        double scaleX = 1;
        double scaleY = 1;
        float offsetX = 0;
        float offsetY = 0;

        //TODO determine floor
//        Switch.data_process_top.determineFloor();
//        switch (Switch.data_process_top.getCurrentFloor())
//        {
//            case 0 : originalFloorplan = BitmapFactory.decodeResource(getResources(), R.drawable.nucleusground);
//                    floorplan = Bitmap.createScaledBitmap(originalFloorplan, 1024, 960, true);
//            case 1 : originalFloorplan = BitmapFactory.decodeResource(getResources(), R.drawable.fleeming_jenkins_first);
//                    floorplan = Bitmap.createScaledBitmap(originalFloorplan, 1024, 960, true);
//        }

        currentPosition.x =  (float) (position[0] * scaleX) + offsetX;
        currentPosition.y =  ((float) (position[1] * scaleY) + offsetY);

        trajectoryPoints.add(new PointF(currentPosition.x, currentPosition.y));
        invalidate();
    }



    public float[] getInitPos(){
        float[] initPos = {initPosition.x, initPosition.y};
        return initPos;
    }

    public float getInitHeading(){
        return initHeading;
    }
}

