package com.accessibility.testapp.ui.widget.figureView;

import android.graphics.Canvas;

/**
 * @author Aleksandr Brazhkin
 */
interface Figure {
    void update();

    void draw(Canvas canvas);

    boolean hitTest(float x, float y);

    boolean borderHitTest(float x, float y);
}
