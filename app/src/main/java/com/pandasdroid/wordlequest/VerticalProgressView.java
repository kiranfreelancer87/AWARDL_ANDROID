package com.pandasdroid.wordlequest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

public class VerticalProgressView extends View {

    private int progress;
    private int max;
    private Paint progressPaint;
    private Paint backgroundPaint;

    public VerticalProgressView(Context context) {
        super(context);
        init(null);
    }

    public VerticalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VerticalProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        progressPaint = new Paint();
        backgroundPaint = new Paint();

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalProgressView);
            int progressColor = typedArray.getColor(R.styleable.VerticalProgressView_progressColor, ContextCompat.getColor(getContext(), R.color.green));
            int backgroundColor = typedArray.getColor(R.styleable.VerticalProgressView_backgroundColor, ContextCompat.getColor(getContext(), R.color.light_gray));
            progress = typedArray.getInt(R.styleable.VerticalProgressView_progress, 0);
            max = typedArray.getInt(R.styleable.VerticalProgressView_max, 100);
            typedArray.recycle();

            progressPaint.setColor(progressColor);
            backgroundPaint.setColor(backgroundColor);
        } else {
            progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.light_gray));
            backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.green));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        float progressHeight = (float) progress / max * height;
        float backgroundHeight = height - progressHeight;

        canvas.drawRect(0, 0, width, backgroundHeight, backgroundPaint);
        canvas.drawRect(0, backgroundHeight, width, height, progressPaint);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
    }
}
