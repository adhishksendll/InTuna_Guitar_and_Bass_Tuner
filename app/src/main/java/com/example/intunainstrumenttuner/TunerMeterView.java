package com.example.intunainstrumenttuner;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class TunerMeterView extends View {

    private Paint centerPaint;
    private Paint needlePaint;
    private Paint inTuneZonePaint;
    private Paint boundaryPaint; // For explicit boundaries
    private Paint textPaint; // For flat/sharp indicators

    private float needlePosition = 0.5f;
    private float animatedNeedlePosition = 0.5f;
    private final float tolerance = 0.05f; // 5% tolerance from center

    private ValueAnimator needleAnimator;

    public TunerMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TunerMeterView(Context context) {
        super(context);
        init();
    }

    private void init() {
        centerPaint = new Paint();
        centerPaint.setColor(Color.GRAY);
        centerPaint.setStrokeWidth(6);

        needlePaint = new Paint();
        needlePaint.setColor(Color.RED); // Default color
        needlePaint.setStrokeWidth(10);

        inTuneZonePaint = new Paint();
        inTuneZonePaint.setColor(Color.argb(50, 0, 255, 0)); // Light green for the in-tune zone

        boundaryPaint = new Paint();
        boundaryPaint.setColor(Color.DKGRAY);
        boundaryPaint.setStrokeWidth(4);
        boundaryPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setNeedlePosition(float pos) {
        needlePosition = Math.max(0f, Math.min(1f, pos));

        if (needleAnimator != null && needleAnimator.isRunning()) {
            needleAnimator.cancel();
        }

        needleAnimator = ValueAnimator.ofFloat(animatedNeedlePosition, needlePosition);
        needleAnimator.setDuration(300); // Increased duration for smoother animation
        needleAnimator.setInterpolator(new DecelerateInterpolator());
        needleAnimator.addUpdateListener(animation -> {
            animatedNeedlePosition = (float) animation.getAnimatedValue();
            if (Math.abs(animatedNeedlePosition - 0.5f) < tolerance) {
                needlePaint.setColor(Color.GREEN);
            } else {
                needlePaint.setColor(Color.RED);
            }
            invalidate();
        });
        needleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw the horizontal line
        canvas.drawLine(0, height / 2f, width, height / 2f, centerPaint);

        // Define the 'in-tune' zone
        float zoneWidth = width * tolerance * 2;
        float zoneStart = (width / 2f) - (zoneWidth / 2f);
        float zoneEnd = zoneStart + zoneWidth;

        // Draw the 'in-tune' zone background
        canvas.drawRect(zoneStart, 0, zoneEnd, height, inTuneZonePaint);

        // Draw the boundary lines for the 'in-tune' zone
        canvas.drawLine(zoneStart, 0, zoneStart, height, boundaryPaint);
        canvas.drawLine(zoneEnd, 0, zoneEnd, height, boundaryPaint);

        // Draw flat and sharp indicators
        canvas.drawText("b", width * 0.2f, height * 0.4f, textPaint);
        canvas.drawText("#", width * 0.8f, height * 0.4f, textPaint);

        // Draw the animated needle
        float x = width * animatedNeedlePosition;
        canvas.drawLine(x, 0, x, height, needlePaint);
    }
}
