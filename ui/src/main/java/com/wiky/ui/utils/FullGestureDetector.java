package com.wiky.ui.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by wiky on 4/8/15.
 */
public abstract class FullGestureDetector implements ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener {

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private TouchEventType mEventType = TouchEventType.OTHER;

    public FullGestureDetector(Context context) {
        mGestureDetector = new GestureDetector(context, this);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result;

        mScaleGestureDetector.onTouchEvent(event);

        // result is always true here, so I need another way to check for a detected scaling gesture
        boolean isScaling = result = mScaleGestureDetector.isInProgress();
        if (!isScaling) {
            // if no scaling is performed check for other gestures (fling, long tab, etc.)
            result = mGestureDetector.onTouchEvent(event);
        }

        // some irrelevant checks...

        return result;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mEventType = TouchEventType.OTHER;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        endScroll();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        endScroll();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mEventType = TouchEventType.SCROLL;
        onScrolling(e1, e2, distanceX, distanceY);
        return false;
    }

    private void endScroll() {
        if (mEventType == TouchEventType.SCROLL) {
            mEventType = TouchEventType.OTHER;
            onScrollFinish();
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        endScroll();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        endScroll();
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        onScaling(detector);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        endScroll();
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        endScroll();
        onScaleFinish(detector);
    }

    public abstract void onScrolling(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    public abstract void onScrollFinish();

    public abstract void onScaling(ScaleGestureDetector detector);

    public abstract void onScaleFinish(ScaleGestureDetector detector);

    private enum TouchEventType {
        SCROLL,
        OTHER,
    }
}
