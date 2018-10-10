package com.accessibility.testapp.ui.widget.figureView;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Triangle.
 *
 * @author Aleksandr Brazhkin
 */
class Triangle implements Figure {

    /**
     * Tan for triangle sides.
     */
    private static final float TG = 2;


    private final FigureView view;
    private final Paint paint;

    private float left;
    private float top;
    private float right;
    private float bottom;
    private float center;
    private float touchDistance;

    Triangle(FigureView view, Paint paint) {
        this.view = view;
        this.paint = paint;
    }

    @Override
    public void update() {
        left = (view.getViewWidth() - view.getFigureWidth()) / 2;
        top = (view.getViewHeight() - view.getFigureWidth()) / 2;
        right = left + view.getFigureWidth();
        bottom = top + view.getFigureWidth();
        center = (left + right) / 2;
        float distance = view.getTouchDistance();
        touchDistance = (float) Math.sqrt(distance * distance + TG * TG * distance * distance);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLine(left, bottom, center, top, paint);
        canvas.drawLine(center, top, right, bottom, paint);
        canvas.drawLine(right, bottom, left, bottom, paint);
    }

    @Override
    public boolean hitTest(float x, float y) {
        float sideY = top + Math.abs(TG * (x - center));
        return y < bottom && y > sideY;
    }

    @Override
    public boolean borderHitTest(float x, float y) {
        float outerSideY = top + Math.abs(TG * (x - center)) - touchDistance;
        boolean fitsOuter = y < bottom + touchDistance && y > outerSideY;
        if (!fitsOuter) {
            return false;
        }
        float innerSideY = top + Math.abs(TG * (x - center)) + touchDistance;
        boolean fitsInner = y < bottom - touchDistance && y > innerSideY;
        return !fitsInner;
    }
}
