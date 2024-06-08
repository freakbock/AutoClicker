package com.example.autoclicker.Draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private List<PointF> clickPoints = new ArrayList<>();
    private Paint clickPaint;
    private Path swipePath;
    private Paint swipePaint;

    public OverlayView(Context context) {
        super(context);
        initPaints();
    }

    private void initPaints() {
        clickPaint = new Paint();
        clickPaint.setColor(0xFFFF0000);
        clickPaint.setStyle(Paint.Style.FILL);

        swipePaint = new Paint();
        swipePaint.setColor(0xFF0000FF);
        swipePaint.setStyle(Paint.Style.STROKE);
        swipePaint.setStrokeWidth(5);
    }

    public void addClick(float x, float y) {
        clickPoints.add(new PointF(x, y));
        invalidate(); // Перерисовка view
    }

    public void setSwipePath(Path path) {
        swipePath = path;
        invalidate(); // Перерисовка view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PointF point : clickPoints) {
            canvas.drawCircle(point.x, point.y, 25, clickPaint);
        }
        if (swipePath != null) {
            canvas.drawPath(swipePath, swipePaint);
        }
    }

    public void clear() {
        clickPoints.clear();
        swipePath = null;
        invalidate();
    }
}

