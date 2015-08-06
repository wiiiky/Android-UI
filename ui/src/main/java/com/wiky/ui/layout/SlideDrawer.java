package com.wiky.ui.layout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.wiky.ui.R;
import com.wiky.ui.utils.SimpleAnimatorListener;

/**
 * Created by wiky on 8/5/15.
 */
public class SlideDrawer extends ViewGroup {

    private View mMask;
    private float mOffset = 0.0f;
    private float mDrawerWidth = 300;
    private float mDrawerWidthRatio = -1.0f;
    private View mContent;
    private View mDrawer;
    private SlideDragHelper mSlideHelper;

    public SlideDrawer(Context context) {
        this(context, null);
    }
    public SlideDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SlideDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        mSlideHelper = new SlideDragHelper(context);

        mMask = new View(context);
        mMask.setBackgroundColor(Color.BLACK);
        addView(mMask, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideDrawer);
        mDrawerWidth = a.getDimensionPixelSize(R.styleable.SlideDrawer_drawWidth, (int) mDrawerWidth);
        mDrawerWidthRatio = a.getFloat(R.styleable.SlideDrawer_drawWidthRatio, mDrawerWidthRatio);
        a.recycle();
    }

    @Override
    public void onMeasure(int w, int h) {
        super.onMeasure(w, h);

        w = getMeasuredWidth();
        h = getMeasuredHeight();

        if (mDrawerWidthRatio > 0 && mDrawerWidthRatio <= 1.0f) {
            mDrawerWidth = w * mDrawerWidthRatio;
        }

        mContent = findViewById(R.id.sd_content);
        mDrawer = findViewById(R.id.sd_drawer);

        if (mDrawer != null) {
            mDrawer.setPivotX(0);
            mDrawer.setPivotY(h / 2.0f);
            mDrawer.bringToFront();
            measureChild(mDrawer, MeasureSpec.makeMeasureSpec((int) mDrawerWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));

            measureChild(mMask, MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
            mMask.bringToFront();
        }

        if (mContent != null) {
            mContent.setPivotX(0.0f);
            mContent.setPivotY(h / 2.0f);
            mContent.bringToFront();
            measureChild(mContent, MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        if (mDrawer != null) {
            float d = 0.8f + mOffset / mDrawerWidth * 0.2f;
            mDrawer.setScaleX(d);
            mDrawer.setScaleY(d);
            mDrawer.layout(0, 0, (int) mDrawerWidth, h);

            mMask.layout(0, 0, w, h);
            mMask.setAlpha(1.0f - mOffset / mDrawerWidth);
        }
        if (mContent != null) {
            float d = 1.0f - mOffset / mDrawerWidth * 0.2f;
            mContent.setScaleX(d);
            mContent.setScaleY(d);
            mContent.layout((int) (mOffset), 0, (int) (w + mOffset), h);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mSlideHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mSlideHelper.onTouchEvent(event);
    }

    enum SlideDrawState {
        IDLE,
        DRAGING
    }

    private class SlideDragHelper {

        private MotionEvent mDownEvent = null;
        private MotionEvent mLastEvent = null;
        private SlideDrawState mState = SlideDrawState.IDLE;
        private Scroller mScroller;
        private boolean mTryIntercept = false;
        private ValueAnimator mAnimator = null;

        public SlideDragHelper(Context context) {
            mScroller = new Scroller(context);
        }

        private void saveDownEvent(MotionEvent event) {
            mDownEvent = MotionEvent.obtain(event);
            mState = SlideDrawState.IDLE;
            mLastEvent = null;
            mTryIntercept = false;
        }

        private void saveLastEvent(MotionEvent event) {
            mLastEvent = MotionEvent.obtain(event);
        }

        private void cancel() {
            mDownEvent = null;
            mLastEvent = null;
            mState = SlideDrawState.IDLE;
        }

        private boolean checkMotionRight(MotionEvent event) {
            return event.getX() > mDownEvent.getX() && Math.abs(event.getX() - mDownEvent.getX()) > Math.abs(event.getY() - mDownEvent.getY());
        }

        private boolean checkMotionLeft(MotionEvent event) {
            return event.getX() < mDownEvent.getX() && Math.abs(event.getX() - mDownEvent.getX()) > Math.abs(event.getY() - mDownEvent.getY());
        }

        public boolean shouldInterceptTouchEvent(MotionEvent event) {
            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    saveDownEvent(event);
                    if (mOffset > 0 && mContent != null && mContent.getLeft() <= event.getX()
                            && mContent.getRight() >= event.getX() && mContent.getTop() <= event.getY()
                            && mContent.getBottom() >= event.getY()) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mState == SlideDrawState.DRAGING || mTryIntercept == true) {
                        break;
                    }
                    saveLastEvent(event);
                    if (event.getEventTime() - event.getDownTime() >= 120) {
                        if (mOffset == 0) {
                            if (checkMotionRight(event)) {
                                mState = SlideDrawState.DRAGING;
                                return true;
                            }
                        } else if (checkMotionLeft(event) || checkMotionRight(event)) {
                            mState = SlideDrawState.DRAGING;
                            return true;
                        }
                        mTryIntercept = true;
                    }
                    break;
            }
            Log.e("intercept", "" + action);
            return false;
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (mAnimator != null) {
                mAnimator.cancel();
                mAnimator = null;
            }
            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    saveDownEvent(event);
                    saveLastEvent(event);
                    mState = SlideDrawState.DRAGING;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - mLastEvent.getX();
                    onSlide(dx);
                    saveLastEvent(event);
                    break;
                default:
                    onEnd(event);
                    break;
            }
            Log.e("action", "" + action + "\tx:" + event.getX() + "\ty:" + event.getY());
            return true;
        }

        public void onClick(MotionEvent e) {
            mAnimator = createAnimator(mOffset, 0);
            mAnimator.start();
        }

        public void onSlide(float dx) {
            mOffset += dx;
            mOffset = Math.max(0, Math.min(mDrawerWidth, mOffset));
            requestLayout();
        }

        public void onEnd(MotionEvent event) {
            if (mOffset == mDrawerWidth && event.getEventTime() - event.getDownTime() < 100) {
                onClick(event);
                return;
            }
            if (mOffset > mDrawerWidth * 0.5f) {
                mAnimator = createAnimator(mOffset, mDrawerWidth);
            } else {
                mAnimator = createAnimator(mOffset, 0);
            }
            mAnimator.start();
        }

        private ValueAnimator createAnimator(float from, float to) {
            ValueAnimator animator = ValueAnimator.ofFloat(from, to);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mOffset = (float) animation.getAnimatedValue();
                    requestLayout();
                }
            });
            animator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                }
            });
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration((long) Math.abs(from - to));
            return animator;
        }

    }
}
