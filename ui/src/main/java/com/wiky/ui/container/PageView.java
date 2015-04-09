package com.wiky.ui.container;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.wiky.pageview.R;
import com.wiky.ui.adapter.PageAdapter;


/**
 * 上滑翻页
 * 两个切换临界点都表示从下往上的部分在整个纵向界面中所占的百分比
 */
public class PageView extends RelativeLayout implements View.OnTouchListener {
    private PageAdapter mAdapter;
    private FrameLayout mCurrentFrame;
    private FrameLayout mNextFrame;
    private FrameLayout mPrevFrame;

    /* attrs */
    private float mNextBreakPoint = 0.5f; /* 临界点 */
    private float mPrevBreakPoint = 0.5f;
    private int mResetCurrentDuration = 250;
    private int mResetPrevDuration = 250;
    private int mNextDuration = 150;
    private int mPrevDuration = 150;
    private float mRI = 1.0f;       /* 阻力系数 */

    private OnPageChangeListener mListener;

    private int mCurrentPos = 0;
    private float mDownY = 0;
    private AnimationAction mAction;

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /*
     * Initialize
     */
    private void init(Context context, AttributeSet attrs) {
        mCurrentFrame = createFrameLayout(context);
        mNextFrame = createFrameLayout(context);
        mPrevFrame = createFrameLayout(context);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mNextFrame, params);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mCurrentFrame, params);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mPrevFrame, params);

        mCurrentPos = 0;
        mAdapter = null;
        mListener = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageView);
        mNextBreakPoint = a.getFloat(R.styleable.PageView_nextBreakPoint, 0.5f);
        mPrevBreakPoint = a.getFloat(R.styleable.PageView_prevBreakPoint, 0.5f);
        mResetCurrentDuration = a.getInt(R.styleable.PageView_resetCurrentDuration, 250);
        mResetPrevDuration = a.getInt(R.styleable.PageView_resetPrevDuration, 250);
        mNextDuration = a.getInt(R.styleable.PageView_nextDuration, 150);
        mPrevDuration = a.getInt(R.styleable.PageView_prevDuration, 150);
        mRI = a.getFloat(R.styleable.PageView_ri, 1.0f);
    }

    /*
     * 创建基本的FrameLayout
     */
    private FrameLayout createFrameLayout(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setVisibility(INVISIBLE);
        return frameLayout;
    }

    public void setAdapter(PageAdapter adapter) {
        mAdapter = adapter;

        mCurrentFrame.removeAllViews();
        mNextFrame.removeAllViews();
        mPrevFrame.removeAllViews();

        mCurrentPos = 0;
        setDisplayView(mCurrentFrame, 0);
        setDisplayView(mNextFrame, 1);
        setDisplayView(mPrevFrame, -1);

        setOnTouchListener(this);
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    /*
     * 给FrameLayout设置一个View
     */
    private void setDisplayView(FrameLayout frame, int pos) {
        if (pos >= mAdapter.getCount() || pos < 0) {
            frame.setVisibility(INVISIBLE);
        } else {
            View v = mAdapter.getView(pos, frame.getChildAt(0), frame);
            if (frame.getChildAt(0) != v) {
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                frame.removeAllViews();
                frame.addView(v, params);
            }
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
                mAction = AnimationAction.NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                float fy = y - mDownY;
                if (fy < 0 && mCurrentFrame.getVisibility() == VISIBLE) {
                    mAction = AnimationAction.NEXT;
                    mCurrentFrame.setY((int) fy / mRI);
                } else {
                    mCurrentFrame.setY(0);
                    if (mPrevFrame.getVisibility() == VISIBLE) {
                        mPrevFrame.setY(fy / mRI - getHeight());
                        mAction = AnimationAction.PREV;
                    } else {
                        mAction = AnimationAction.NONE;
                    }
                }
                break;
            default:
                if (mAction == AnimationAction.NEXT) {
                    if (mCurrentFrame.getY() > -getHeight() * mNextBreakPoint) {
                        resetCurrentFrame();
                    } else {
                        forwardToNextFrame();
                    }
                } else if (mAction == AnimationAction.PREV) {
                    if (mPrevFrame.getY() < -getHeight() * (1 - mPrevBreakPoint)) {
                        resetPrevFrame();
                    } else {
                        backToPrevFrame();
                    }
                }
                break;
        }
        return true;
    }

    /*
     * 重置前一个FrameLayout的位置
     */
    private void resetPrevFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mPrevFrame, "y", -getHeight());
        ani.setDuration(mResetPrevDuration);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setOnTouchListener(PageView.this);
                if (mListener != null) {
                    mListener.onPrevCancelled(mCurrentPos);
                }
            }
        });
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 滑动距离不够，返回
     */
    private void resetCurrentFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mCurrentFrame, "y", 0);
        ani.setDuration(mResetCurrentDuration);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setOnTouchListener(PageView.this);
                if (mListener != null) {
                    mListener.onNextCancelled(mCurrentPos);
                }
            }
        });
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 进入下一页
     */
    private void forwardToNextFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mCurrentFrame, "y", -getHeight());
        ani.setDuration(mNextDuration);
        ani.setInterpolator(new DecelerateInterpolator());
        ani.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    /* 这里需要延时一小段时间后才调用刷新的方法，因为在动画结束后立即调用会引起一个动画卡顿 */
                    @Override
                    public void run() {
                        mCurrentPos++;
                        if (mListener != null) {
                            mListener.onPageChanged(mCurrentPos);
                        }
                        rollToNext();
                        setOnTouchListener(PageView.this);
                    }
                }, 50);
            }
        });
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 返回上一页
     */
    private void backToPrevFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mPrevFrame, "y", 0);
        ani.setDuration(mPrevDuration);
        ani.setInterpolator(new DecelerateInterpolator());
        ani.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentPos--;
                if (mListener != null) {
                    mListener.onPageChanged(mCurrentPos);
                }
                rollBackToPrev();
                setOnTouchListener(PageView.this);
            }
        });
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 移动到下一个
     */
    private void rollToNext() {
        FrameLayout view = mPrevFrame;
        mPrevFrame = mCurrentFrame;
        mCurrentFrame = mNextFrame;
        mNextFrame = view;

        mNextFrame.bringToFront();
        mCurrentFrame.bringToFront();
        mPrevFrame.bringToFront();
        mPrevFrame.setY(-getHeight());
        mCurrentFrame.setY(0);
        mNextFrame.setY(0);

        setDisplayView(mNextFrame, mCurrentPos + 1);
    }

    /*
     * 移动FrameLayout到上一个
     */
    private void rollBackToPrev() {
        FrameLayout view = mNextFrame;
        mNextFrame = mCurrentFrame;
        mCurrentFrame = mPrevFrame;
        mPrevFrame = view;

        mNextFrame.bringToFront();
        mCurrentFrame.bringToFront();
        mPrevFrame.bringToFront();
        mPrevFrame.setY(-getHeight());
        mCurrentFrame.setY(0);
        mNextFrame.setY(0);

        setDisplayView(mPrevFrame, mCurrentPos - 1);
    }


    private enum AnimationAction {
        NEXT,
        PREV,
        NONE,
    }

    /*
     * 回调接口
     */
    public interface OnPageChangeListener {
        void onPageChanged(int pos);         /* 翻页时调用,pos表示新页  */

        void onNextCancelled(int pos);       /* 取消翻页时调用, pos表示当前页  */

        void onPrevCancelled(int pos);       /* 取消向前翻页时候调用，pos表示当前页 */
    }

    private abstract class AnimatorEndListener implements Animator.AnimatorListener {

        @Override
        public abstract void onAnimationEnd(Animator animation);

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}