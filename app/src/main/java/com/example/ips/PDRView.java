package com.example.ips;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PDRView extends View {
    private Paint paint;
    private Path path;
    private List<float[]> trajectory;

    public PDRView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(8f);
        paint.setStyle(Paint.Style.STROKE);

        path = new Path();
        trajectory = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 1; i < trajectory.size(); i++) {
            float[] prevPosition = trajectory.get(i - 1);
            float[] currentPosition = trajectory.get(i);

            path.moveTo(prevPosition[0], prevPosition[1]);
            path.lineTo(currentPosition[0], currentPosition[1]);
        }

        canvas.drawPath(path, paint);
    }

    public void addPosition(float[] position) {
        trajectory.add(position);
        invalidate(); // Request to redraw the view
    }
}

