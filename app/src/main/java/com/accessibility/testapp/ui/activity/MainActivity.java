package com.accessibility.testapp.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.accessibility.logger.Logger;
import com.accessibility.logger.LoggerFactory;
import com.accessibility.testapp.R;
import com.accessibility.testapp.ui.fragmnet.StartFragment;
import com.accessibility.testapp.ui.fragmnet.grid.RtPermissionsDelegate;
import com.accessibility.testapp.ui.helper.swipegesturedetector.SwipeGestureDetector;

/**
 * @author Aleksandr Brazhkin
 */
public class MainActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private final SwipeGestureDetector swipeGestureDetector = new SwipeGestureDetector();

    //region Views
    private ViewGroup mainContainer;
    private ViewGroup fragmentContainer;
    //endregion
    private RtPermissionsDelegate rtPermissionsDelegate = new RtPermissionsDelegate(this);
    private boolean navigationBlocked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init
        swipeGestureDetector.setOnSwipeListener(onSwipeListener);
        swipeGestureDetector.init(this);
        // Init UI
        mainContainer = findViewById(R.id.mainContainer);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        addStartFragment();
    }

    public RtPermissionsDelegate getRtPermissionsDelegate() {
        return rtPermissionsDelegate;
    }

    private void addStartFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = StartFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        rtPermissionsDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (swipeGestureDetector.onTouchEvent(ev)) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
            super.dispatchTouchEvent(ev);
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public void onBackPressed() {
        // nop
    }

    private void onCloseFragmentSwipeFinished(Fragment fragment) {
        logger.trace("onCloseFragmentSwipeFinished, fragment: " + fragment);
        View view = fragment.getView();
        if (view == null) {
            throw new IllegalStateException();
        }
        if (view.getTranslationX() > mainContainer.getMeasuredWidth() / 2) {
            if (view.getTranslationX() < mainContainer.getMeasuredWidth()) {
                view.animate()
                        .translationX(mainContainer.getMeasuredWidth())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                onFragmentShouldBeRemoved(fragment);
                            }
                        })
                        .start();
            } else {
                onFragmentShouldBeRemoved(fragment);
            }
        } else {
            view.animate()
                    .translationX(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            navigationBlocked = false;
                        }
                    })
                    .start();
        }
    }

    private void onFragmentShouldBeRemoved(Fragment fragment) {
        logger.trace("onFragmentShouldBeRemoved, fragment: " + fragment);
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();
        fragmentContainer.post(() -> navigationBlocked = false);
    }

    private void onCloseActivitySwipeFinished() {
        if (mainContainer.getTranslationY() > mainContainer.getMeasuredHeight() / 2) {
            if (mainContainer.getTranslationY() < mainContainer.getMeasuredHeight()) {
                mainContainer
                        .animate()
                        .translationY(mainContainer.getMeasuredHeight())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                onActivityShouldBeFinished();
                            }
                        })
                        .start();
            } else {
                onActivityShouldBeFinished();
            }
        } else {
            mainContainer.animate().translationY(0).start();
        }
    }

    private void onActivityShouldBeFinished() {
        finish();
    }

    private final SwipeGestureDetector.OnSwipeListener onSwipeListener = new SwipeGestureDetector.OnSwipeListener() {
        @Override
        public boolean onSwipe(float distance, int direction, int fingersCount) {
            if (navigationBlocked) {
                return false;
            }
            if (fingersCount == 3 && direction == SwipeGestureDetector.SWIPE_DIRECTION_DOWN) {
                mainContainer.setTranslationY(Math.max(0, mainContainer.getTranslationY() + distance));
                return true;
            } else if (fingersCount == 2 && direction == SwipeGestureDetector.SWIPE_DIRECTION_RIGHT) {
                int fragmentsCount = fragmentContainer.getChildCount();
                if (fragmentsCount > 1) {
                    View topView = fragmentContainer.getChildAt(fragmentsCount - 1);
                    topView.setTranslationX(Math.max(0, topView.getTranslationX() + distance));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onSwipeEnd(int direction, int fingersCount) {
            if (navigationBlocked) {
                return false;
            }
            if (fingersCount == 3 && direction == SwipeGestureDetector.SWIPE_DIRECTION_DOWN) {
                onCloseActivitySwipeFinished();
            } else if (fingersCount == 2 && direction == SwipeGestureDetector.SWIPE_DIRECTION_RIGHT) {
                navigationBlocked = true;
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                onCloseFragmentSwipeFinished(fragment);
            }
            return false;
        }

        @Override
        public boolean onFling(float velocity, int direction, int fingersCount) {
            if (navigationBlocked) {
                return false;
            }
            if (fingersCount == 3 && direction == SwipeGestureDetector.SWIPE_DIRECTION_DOWN) {
                new FlingAnimation(mainContainer, DynamicAnimation.TRANSLATION_Y)
                        .setStartVelocity(velocity)
                        .setFriction(1f)
                        .setMinValue(0)
                        .setMaxValue(mainContainer.getMeasuredHeight())
                        .addEndListener((animation, canceled, value, velocity1) ->
                                onCloseActivitySwipeFinished())
                        .start();
                return true;
            } else if (fingersCount == 2 && direction == SwipeGestureDetector.SWIPE_DIRECTION_RIGHT) {
                int fragmentsCount = getSupportFragmentManager().getFragments().size();
                if (fragmentsCount > 1) {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    navigationBlocked = true;
                    new FlingAnimation(fragment.getView(), DynamicAnimation.TRANSLATION_X)
                            .setStartVelocity(velocity)
                            .setFriction(1f)
                            .setMinValue(0)
                            .setMaxValue(fragmentContainer.getMeasuredWidth())
                            .addEndListener((animation, canceled, value, velocity1) ->
                                    onCloseFragmentSwipeFinished(fragment))
                            .start();
                    return true;
                }
            }
            return false;
        }
    };
}
