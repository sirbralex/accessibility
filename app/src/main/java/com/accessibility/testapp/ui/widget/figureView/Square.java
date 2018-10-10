package com.accessibility.testapp.ui.widget.figureView;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Square.
 *
 * @author Aleksandr Brazhkin
 */
class Square implements Figure {

    private final FigureView view;
    private final Paint paint;

    private float left;
    private float top;
    private float right;
    private float bottom;
    private float touchDistance;

    Square(FigureView view, Paint paint) {
        this.view = view;
        this.paint = paint;
    }

    @Override
    public void update() {
        left = (view.getViewWidth() - view.getFigureWidth()) / 2;
        top = (view.getViewHeight() - view.getFigureWidth()) / 2;
        right = left + view.getFigureWidth();
        bottom = top + view.getFigureWidth();
        touchDistance = view.getTouchDistance();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(left, top, right, bottom, paint);
    }

    @Override
    public boolean hitTest(float x, float y) {
        return x > left && x < right && y > top && y < bottom;
    }

    @Override
    public boolean borderHitTest(float x, float y) {
        boolean fitsOuter = x > left - touchDistance && x < right + touchDistance
                && y > top - touchDistance && y < bottom + touchDistance;
        if (!fitsOuter) {
            return false;
        }
        boolean fitsInner = x > left + touchDistance && x < right - touchDistance
                && y > top + touchDistance && y < bottom - touchDistance;
        return !fitsInner;
    }
}
