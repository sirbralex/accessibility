package com.accessibility.testapp.ui.widget.figureView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.accessibility.testapp.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Aleksandr Brazhkin
 */
public class FigureView extends View {

    public FigureView(Context context) {
        this(context, null);
    }

    public FigureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FigureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_CIRCLE, TYPE_TRIANGLE, TYPE_SQUARE})
    public @interface Type {
    }

    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_TRIANGLE = 2;
    public static final int TYPE_SQUARE = 3;

    private TouchListener touchListener;
    private int downPtrId = -1;
    private boolean figureInTouch = false;
    private Figure figure;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private int figureWidth = 0;
    private float figureScale = 1f;
    private float touchDistance;

    private final Paint paint = new Paint();

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FigureView,
                0, 0);

        try {
            touchDistance = a.getDimensionPixelSize(R.styleable.FigureView_touch_distance, 0);
        } finally {
            a.recycle();
        }

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        setFigureType(TYPE_CIRCLE);
    }

    int getViewWidth() {
        return viewWidth;
    }

    int getViewHeight() {
        return viewHeight;
    }

    int getFigureWidth() {
        return figureWidth;
    }

    public float getTouchDistance() {
        return touchDistance;
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
        if (touchListener != null) {
            touchListener.onFigureTouchStateChanged(figureInTouch);
        }
    }

    public void setFigureType(@Type int figureType) {
        switch (figureType) {
            case TYPE_CIRCLE: {
                figure = new Circle(this, paint);
                break;
            }
            case TYPE_TRIANGLE: {
                figure = new Triangle(this, paint);
                break;
            }
            case TYPE_SQUARE: {
                figure = new Square(this, paint);
                break;
            }
        }
        figure.update();
        invalidate();
    }

    public void setScale(@FloatRange(from = 0.0, to = 1.0) float scale) {
        figureScale = scale;
        updateFigureWidth();
        figure.update();
        invalidate();
    }

    private void updateFigureWidth() {
        int availableWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        int availableHeight = viewHeight - getPaddingTop() - getPaddingBottom();
        figureWidth = (int) (Math.min(availableWidth, availableHeight) * figureScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        figure.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        updateFigureWidth();
        figure.update();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                int index = ev.getActionIndex();
                downPtrId = ev.getPointerId(index);
                float x = ev.getX(index);
                float y = ev.getY(index);
                onFigureTouchStateChanged(figure.borderHitTest(x, y));
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                downPtrId = -1;
                onFigureTouchStateChanged(false);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (downPtrId != -1) {
                    int index = ev.findPointerIndex(downPtrId);
                    float x = ev.getX(index);
                    float y = ev.getY(index);
                    onFigureTouchStateChanged(figure.borderHitTest(x, y));
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (ev.getPointerId(ev.getActionIndex()) == downPtrId) {
                    downPtrId = -1;
                    onFigureTouchStateChanged(false);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                downPtrId = -1;
                onFigureTouchStateChanged(false);
                break;
            }
        }
        return true;
    }

    private void onFigureTouchStateChanged(boolean inTouch) {
        if (figureInTouch == inTouch) {
            return;
        }
        figureInTouch = inTouch;
        if (touchListener != null) {
            touchListener.onFigureTouchStateChanged(figureInTouch);
        }
    }

    public interface TouchListener {
        void onFigureTouchStateChanged(boolean inTouch);
    }
}
