package com.wiky.ui.layout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.wiky.ui.adapter.PageAdapter;


/**
 * 上滑翻页
 */
public class PageView extends RelativeLayout implements View.OnTouchListener {
    private PageAdapter mAdapter;
    private FrameLayout mFrame1;
    private FrameLayout mFrame2;

    private OnPageChangeListener mListener;

    private int mCurrentPos = 0;
    private float mDownY = 0;

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /*
     * Initialize
     */
    private void init(Context context) {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mFrame1 = new FrameLayout(context);
        mFrame1.setTag(null);
        mFrame1.setVisibility(GONE);

        mFrame2 = new FrameLayout(context);
        mFrame2.setTag(null);
        mFrame2.setVisibility(GONE);

        addView(mFrame2, params);
        addView(mFrame1, params);

        mCurrentPos = 0;
        mAdapter = null;
        mListener = null;
    }

    public void setAdapter(PageAdapter adapter) {
        mAdapter = adapter;
        mFrame1.setVisibility(GONE);
        mFrame2.setVisibility(GONE);

        mCurrentPos = 0;
        setView(mFrame1, 0);
        setView(mFrame2, 1);

        mFrame1.setOnTouchListener(this);
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    /*
     * 给FrameLayout设置一个View
     */
    private void setView(FrameLayout frame, int pos) {
        if (pos >= mAdapter.getCount()) {
            frame.setVisibility(GONE);
        } else {
            View v = mAdapter.getView(pos, (View) frame.getTag(), frame);
            if (frame.getChildAt(0) != v) {
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                frame.removeAllViews();
                frame.addView(v, params);
            }
            frame.setTag(v);
            frame.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int act = event.getAction();
        float y = event.getRawY();

        switch (act & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (y - mDownY < 0) {
                    v.setY((int) (y - mDownY) / 1.3f);
                }
                break;
            default:
                if (v.getY() > -500) {
                    resetView(v);
                } else {
                    nextView(v);
                }
                break;
        }
        return true;
    }

    /*
     * 滑动距离不够，返回
     */
    private void resetView(final View v) {
        ObjectAnimator ani = ObjectAnimator.ofFloat(v, "y", 0);
        ani.setDuration(250);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setOnTouchListener(PageView.this);
                if (mListener != null) {
                    mListener.onCancelled(mCurrentPos);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        v.setOnTouchListener(null);
        ani.start();
    }

    /*
     * 进入下一页
     */
    private void nextView(final View v) {
        ObjectAnimator ani = ObjectAnimator.ofFloat(v, "y", -getHeight());
        ani.setDuration(100);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentPos++;
                if (mListener != null) {
                    mListener.onPageChanged(mCurrentPos);
                }

                if (mCurrentPos >= mAdapter.getCount()) {
                    /* finish */
                    mFrame1.setVisibility(GONE);
                    mFrame2.setVisibility(GONE);
                    return;
                }
                FrameLayout upper, lower = (FrameLayout) v;
                if (v == mFrame1) {
                    upper = mFrame2;
                } else {
                    upper = mFrame1;
                }
                upper.setOnTouchListener(PageView.this);
                upper.bringToFront();
                lower.setY(0);

                setView(upper, mCurrentPos);
                setView(lower, mCurrentPos + 1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        v.setOnTouchListener(null);
        ani.start();
    }


    /*
     * 回调接口
     */
    public interface OnPageChangeListener {
        public void onPageChanged(int pos);         /* 翻页时调用,pos表示新页  */

        public void onCancelled(int pos);           /* 取消翻页时调用, pos表示当前页  */
    }
}