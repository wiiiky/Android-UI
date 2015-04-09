package com.wiky.ui.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

/**
 * Created by wiky on 4/9/15.
 */
public class GoodGestureDetector {

    private OnGestureListener mListener = null;
    private Context mContext = null;
    private float mLastScrollX;
    private float mLastScrollY;
    private float mLastSpan;

    public GoodGestureDetector(@NonNull Context context, @NonNull OnGestureListener listener) {
        mContext = context;
        mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = true;
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        int count = ev.getPointerCount();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handled = mListener.onStart(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (count == 1) {
                    doScroll(ev);
                } else {
                    doScale(ev);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mListener.onEnd();
                break;
        }

        if (!(action == MotionEvent.ACTION_MOVE && count > 1)) {
            mLastSpan = 0;
        }
        if (!(action == MotionEvent.ACTION_MOVE && count == 1)) {
            mLastScrollX = -1;
            mLastScrollY = -1;
        }
        return handled;
    }

    private void doScroll(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        if (mLastScrollX != -1) {
            float dx = mLastScrollX - x;
            float dy = mLastScrollY - y;
            mListener.onScroll(dx, dy);
        }
        mLastScrollX = x;
        mLastScrollY = y;
    }

    private void doScale(MotionEvent ev) {
        float x1 = ev.getX(0);
        float y1 = ev.getY(0);
        float x2 = ev.getX(1);
        float y2 = ev.getY(1);
        float spanX = x1 - x2;
        float spanY = y1 - y2;
        float span = (float) Math.sqrt(spanX * spanX + spanY * spanY);
        if (mLastSpan != 0) {
            float diff = Math.abs(1 - span / mLastSpan);
            float factor = 1.0f;
            if (span > mLastSpan) {
                factor += diff;
            } else {
                factor -= diff;
            }
            mListener.onScale((x1 + x2) / 2.0f, (y1 + y2) / 2.0f, factor);
        }
        mLastSpan = span;
    }

    public interface OnGestureListener {
        boolean onStart(MotionEvent e);

        void onScroll(float dx, float dy);

        void onScale(float cx, float cy, float factor);

        void onEnd();
    }
}
