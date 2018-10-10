package com.accessibility.testapp.ui.widget.figureView;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Circle.
 *
 * @author Aleksandr Brazhkin
 */
class Circle implements Figure {

    private final FigureView view;
    private final Paint paint;

    private float cx;
    private float cy;
    private float radius;
    private float distance;
    private float innerDistance;
    private float outerDistance;

    Circle(FigureView view, Paint paint) {
        this.view = view;
        this.paint = paint;
    }

    @Override
    public void update() {
        cx = view.getViewWidth() / 2;
        cy = view.getViewHeight() / 2;
        radius = view.getFigureWidth() / 2;
        distance = radius * radius;
        float touchDistance = view.getTouchDistance();
        innerDistance = (radius - touchDistance) * (radius - touchDistance);
        outerDistance = (radius + touchDistance) * (radius + touchDistance);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    public boolean hitTest(float x, float y) {
        float xDiff = x - cx;
        float yDiff = y - cy;
        return distance > (xDiff * xDiff + yDiff * yDiff);
    }

    @Override
    public boolean borderHitTest(float x, float y) {
        float xDiff = x - cx;
        float yDiff = y - cy;
        boolean fitsOuter = outerDistance > (xDiff * xDiff + yDiff * yDiff);
        if (!fitsOuter) {
            return false;
        }
        boolean fitsInner = innerDistance > (xDiff * xDiff + yDiff * yDiff);
        return !fitsInner;
    }
}
