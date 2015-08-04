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
    private MotionEvent mClickEvent = null;     /* 单击事件 */
    private MotionEvent mDoubleEvent = null;    /* 双击中的第一次单击 */

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
                mClickEvent = MotionEvent.obtain(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (count == 1) {
                    doScroll(ev);
                } else {
                    doScale(ev);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mClickEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onClick(ev);
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

    private void onClick(MotionEvent ev) {
        if (mDoubleEvent != null) {
            if (mClickEvent != null) {
                mClickEvent = checkClick(mClickEvent, ev);
                if (mClickEvent != null) {
                    mDoubleEvent = checkClick(mDoubleEvent, mClickEvent, 800, 100);
                    if (mDoubleEvent != null) {
                        mListener.onDoubleClick(mDoubleEvent.getX(), mDoubleEvent.getY());
                    }
                }
            }
            mClickEvent = null;
            mDoubleEvent = null;
        } else if (mClickEvent != null) {
            mClickEvent = checkClick(mClickEvent, ev);
            if (mClickEvent != null) {
                mListener.onClick(mClickEvent.getX(), mClickEvent.getY());
            }
            mDoubleEvent = mClickEvent;
            mClickEvent = null;
        }
    }

    private MotionEvent checkClick(MotionEvent pre, MotionEvent now, long time, long pos) {
        MotionEvent clickEvent = null;
        long downTime = pre.getEventTime();
        long eventTime = now.getEventTime();
        if (eventTime - downTime < time) {
            float x1 = pre.getX();
            float y1 = pre.getY();
            float x2 = now.getX();
            float y2 = now.getY();
            if (Math.abs(x1 - x2) < pos && Math.abs(y1 - y2) < pos) {
                float x = (x1 + x2) / 2;
                float y = (y1 + y2) / 2;
                clickEvent = MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
            }
        }
        return clickEvent;
    }

    private MotionEvent checkClick(MotionEvent pre, MotionEvent now) {
        return checkClick(pre, now, 500, 10);
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

        void onClick(float x, float y);

        void onDoubleClick(float x, float y);

        void onEnd();
    }
}
