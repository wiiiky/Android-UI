package com.wiky.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wiky on 7/21/15.
 */
public abstract class ScrollableLayout extends ViewGroup implements View.OnTouchListener, GestureDetector.OnGestureListener {
    protected boolean mScrollable = true;
    private GestureDetector mGestureDetector;

    public ScrollableLayout(Context context) {
        this(context, null);
    }


    public ScrollableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context);
    }

    private void initialize(Context context) {
        setOnTouchListener(this);

        mGestureDetector = new GestureDetector(context, this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int act = (event.getAction() & MotionEvent.ACTION_MASK);
        if (act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_CANCEL) {
            onScrollFinish(event);
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return mScrollable;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    protected abstract void onScrollFinish(MotionEvent e);

    @Override
    public abstract boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /* 根据总距离和当前位移计算动画时间 */
    protected int computeDuration(float offset, float total, int duration) {
        return (int) (Math.abs(offset) / total * duration);
    }


    public void disable() {
        mScrollable = false;
    }

    public void enable() {
        mScrollable = true;
    }
}
