package com.accessibility.testapp.ui.helper.swipegesturedetector;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.accessibility.logger.Logger;
import com.accessibility.logger.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Gesture listener for swipe.
 *
 * @author Aleksandr Brazhkin
 */
public class SwipeGestureDetector {

    private static final Logger logger = LoggerFactory.getLogger(SwipeGestureDetector.class);

    private static final int MAX_SUPPORTED_POINTERS_COUNT = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SWIPE_DIRECTION_UP, SWIPE_DIRECTION_RIGHT, SWIPE_DIRECTION_DOWN, SWIPE_DIRECTION_LEFT})
    public @interface SwipeDirection {
    }

    public static final int SWIPE_DIRECTION_UP = 1;
    public static final int SWIPE_DIRECTION_RIGHT = 2;
    public static final int SWIPE_DIRECTION_DOWN = 3;
    public static final int SWIPE_DIRECTION_LEFT = 4;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker velocityTracker;

    private final ActivePointers activePointers = new ActivePointers(MAX_SUPPORTED_POINTERS_COUNT);

    private int minimumFlingVelocity;
    private int maximumFlingVelocity;

    private float tempFocusX;
    private float tempFocusY;

    private float lastFocusX;
    private float lastFocusY;

    private boolean swipeStarted;
    private boolean swipeDisallowed;
    private int fingersCountAtStart;
    private int touchSlopSquare;
    private int swipeDirection;

    private OnSwipeListener onSwipeListener;

    public void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        int touchSlop = configuration.getScaledTouchSlop();
        touchSlopSquare = touchSlop * touchSlop;
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.onSwipeListener = onSwipeListener;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        boolean handled = false;

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                // Clear vars
                swipeStarted = false;
                swipeDisallowed = false;
                fingersCountAtStart = 0;
                // Clear existing down events
                activePointers.clear();
                // Add new down event
                int downIndex = ev.getActionIndex();
                int downId = ev.getPointerId(downIndex);
                activePointers.addDownPtr(downId, ev.getX(downIndex), ev.getY(downIndex));
                // Update focus
                calcFocus(ev);
                lastFocusX = tempFocusX;
                lastFocusY = tempFocusY;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (!swipeStarted) {
                    if (activePointers.size() < MAX_SUPPORTED_POINTERS_COUNT) {
                        // Add new down event
                        int downIndex = ev.getActionIndex();
                        int downId = ev.getPointerId(downIndex);
                        activePointers.addDownPtr(downId, ev.getX(downIndex), ev.getY(downIndex));
                        // Update focus
                        calcFocus(ev);
                        lastFocusX = tempFocusX;
                        lastFocusY = tempFocusY;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                // Update focus
                calcFocus(ev);
                if (swipeStarted) {
                    if (activePointers.size() > 0) {
                        // Calc scroll
                        float deltaX = tempFocusX - lastFocusX;
                        float deltaY = tempFocusY - lastFocusY;
                        if ((Math.abs(deltaX) >= 1) || (Math.abs(deltaY) >= 1)) {
                            lastFocusX = tempFocusX;
                            lastFocusY = tempFocusY;
                            if (swipeDirection == SWIPE_DIRECTION_DOWN || swipeDirection == SWIPE_DIRECTION_UP) {
                                handled = onSwipe(deltaY);
                            } else {
                                handled = onSwipe(deltaX);
                            }
                        }
                    }
                } else if (!swipeDisallowed) {
                    boolean allPointersMoved = true;
                    boolean allPointersHaveSameDirection = true;
                    int smallestDeltaX = Integer.MAX_VALUE;
                    int smallestDeltaY = Integer.MAX_VALUE;
                    int commonDirection = 0;
                    for (int i = 0; i < activePointers.size(); i++) {
                        int index = ev.findPointerIndex(activePointers.getId(i));
                        int deltaX = (int) (ev.getX(index) - activePointers.getDownX(i));
                        int deltaY = (int) (ev.getY(index) - activePointers.getDownY(i));
                        smallestDeltaX = Math.abs(deltaX) < Math.abs(smallestDeltaX) ?
                                deltaX : smallestDeltaX;
                        smallestDeltaY = Math.abs(deltaY) < Math.abs(smallestDeltaY) ?
                                deltaY : smallestDeltaY;
                        int distance = (deltaX * deltaX) + (deltaY * deltaY);
                        if (distance > touchSlopSquare) {
                            activePointers.setPtrPos(i, ev.getX(index), ev.getY(index));
                            // Calc pointer direction
                            int direction = calcDirection(deltaX, deltaY);
                            if (commonDirection == 0) {
                                commonDirection = direction;
                            } else {
                                if (commonDirection != direction) {
                                    allPointersHaveSameDirection = false;
                                }
                            }
                        } else {
                            allPointersMoved = false;
                        }
                    }
                    if (allPointersMoved) {
                        if (allPointersHaveSameDirection) {
                            lastFocusX = tempFocusX;
                            lastFocusY = tempFocusY;
                            swipeStarted = true;
                            swipeDirection = commonDirection;
                            fingersCountAtStart = activePointers.size();
                            for (int i = 0; i < ev.getPointerCount(); i++) {
                                int id = ev.getPointerId(i);
                                int indexOfId = activePointers.indexOfId(id);
                                if (indexOfId != -1) {
                                    logger.trace("onMoveStart: i = " + i + " oldX = " + activePointers.getDownX(indexOfId)
                                            + ", newX = " + ev.getX(i) + ", oldY = " + activePointers.getDownY(indexOfId)
                                            + ", newY = " + ev.getY(i));
                                }
                            }
                            if (swipeDirection == SWIPE_DIRECTION_DOWN || swipeDirection == SWIPE_DIRECTION_UP) {
                                handled = onSwipe(smallestDeltaY);
                            } else {
                                handled = onSwipe(smallestDeltaX);
                            }
                        } else {
                            swipeDisallowed = true;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                // Delete existing down event
                int upIndex = ev.getActionIndex();
                int upId = ev.getPointerId(upIndex);
                handled = postOnFlingIfNeed(upId);
                int activeIndex = activePointers.indexOfId(upId);
                if (activeIndex >= 0) {
                    activePointers.deletePtrAtIndex(activeIndex);
                }
                // Update focus
                calcFocus(ev);
                lastFocusX = tempFocusX;
                lastFocusY = tempFocusY;
                break;
            }
            case MotionEvent.ACTION_UP: {
                int upIndex = ev.getActionIndex();
                int upId = ev.getPointerId(upIndex);
                handled = postOnFlingIfNeed(upId);
                velocityTracker.recycle();
                velocityTracker = null;
                swipeStarted = false;
                swipeDisallowed = false;
                fingersCountAtStart = 0;
                // Delete last down event
                activePointers.clear();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                velocityTracker.recycle();
                velocityTracker = null;
                swipeStarted = false;
                swipeDisallowed = false;
                fingersCountAtStart = 0;
                // Clear existing down events
                activePointers.clear();
                break;
            }
        }
        return handled;
    }

    private boolean postOnFlingIfNeed(int pointerId) {
        if (swipeDisallowed) {
            return false;
        }
        if (activePointers.size() != 1) {
            return false;
        }
        if (activePointers.getId(0) != pointerId) {
            return false;
        }
        velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity);
        if (swipeDirection == SWIPE_DIRECTION_DOWN || swipeDirection == SWIPE_DIRECTION_UP) {
            float velocityY = velocityTracker.getYVelocity(pointerId);
            if (Math.abs(velocityY) > minimumFlingVelocity) {
                return onFling(velocityY);
            } else {
                return onSwipeEnd();
            }
        } else {
            float velocityX = velocityTracker.getXVelocity(pointerId);
            if (Math.abs(velocityX) > minimumFlingVelocity) {
                return onFling(velocityX);
            } else {
                return onSwipeEnd();
            }
        }
    }

    private void calcFocus(@NonNull MotionEvent ev) {
        float sumX = 0, sumY = 0;
        for (int i = 0; i < activePointers.size(); i++) {
            int activeId = activePointers.getId(i);
            int index = ev.findPointerIndex(activeId);
            sumX += ev.getX(index);
            sumY += ev.getY(index);
        }
        tempFocusX = sumX / activePointers.size();
        tempFocusY = sumY / activePointers.size();
    }

    @SwipeDirection
    private int calcDirection(int deltaX, int deltaY) {
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) {
                return SWIPE_DIRECTION_RIGHT;
            } else {
                return SWIPE_DIRECTION_LEFT;
            }
        } else {
            if (deltaY > 0) {
                return SWIPE_DIRECTION_DOWN;
            } else {
                return SWIPE_DIRECTION_UP;
            }
        }
    }

    private boolean onSwipe(float distance) {
        logger.trace("onSwipe, distance = " + distance + ", direction = " + swipeDirection + ", fingersCount = " + fingersCountAtStart);
        if (onSwipeListener != null) {
            return onSwipeListener.onSwipe(distance, swipeDirection, fingersCountAtStart);
        }
        return false;
    }

    private boolean onFling(float velocity) {
        logger.trace("onFling, velocity = " + velocity + ", direction = " + swipeDirection + ", fingersCount = " + fingersCountAtStart);
        if (onSwipeListener != null) {
            return onSwipeListener.onFling(velocity, swipeDirection, fingersCountAtStart);
        }
        return false;
    }

    private boolean onSwipeEnd() {
        logger.trace("onSwipeEnd, direction = " + swipeDirection + ", fingersCount = " + fingersCountAtStart);
        if (onSwipeListener != null) {
            return onSwipeListener.onSwipeEnd(swipeDirection, fingersCountAtStart);
        }
        return false;
    }

    public interface OnSwipeListener {
        boolean onSwipe(float distance, @SwipeDirection int direction, int fingersCount);

        boolean onSwipeEnd(@SwipeDirection int direction, int fingersCount);

        boolean onFling(float velocity, @SwipeDirection int direction, int fingersCount);
    }

}
