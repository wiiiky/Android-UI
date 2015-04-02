package com.wiky.ui.layout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.wiky.pageview.R;
import com.wiky.ui.adapter.PageAdapter;


/**
 * 上滑翻页
 */
public class PageView extends RelativeLayout implements View.OnTouchListener {
    private PageAdapter mAdapter;
    private FrameLayout mCurrentFrame;
    private FrameLayout mNextFrame;
    private FrameLayout mPrevFrame;
    private float mBreakThroughPoint = 0.5f; /* 临界点 */

    private int mResetDuration = 250;
    private int mNextDuration = 150;

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
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mCurrentFrame = createFrameLayout(context);
        mNextFrame = createFrameLayout(context);
        mPrevFrame = createFrameLayout(context);

        addView(mNextFrame, params);
        addView(mCurrentFrame, params);
        addView(mPrevFrame, params);

        mCurrentPos = 0;
        mAdapter = null;
        mListener = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageView);
        mBreakThroughPoint = a.getFloat(R.styleable.PageView_breakThroughPoint, 0.5f);
        mResetDuration = a.getInt(R.styleable.PageView_resetDuration, 250);
        mNextDuration = a.getInt(R.styleable.PageView_nextDuration, 150);
    }

    private FrameLayout createFrameLayout(Context context){
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setTag(null);
        frameLayout.setVisibility(INVISIBLE);
        return frameLayout;
    }

    public void setAdapter(PageAdapter adapter) {
        mAdapter = adapter;

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
                mAction = AnimationAction.NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                float fy = y - mDownY;
                if (fy < 0 && mCurrentFrame.getVisibility() == VISIBLE) {
                    mAction = AnimationAction.NEXT;
                    mCurrentFrame.setY((int) fy / 1.3f);
                } else {
                    mCurrentFrame.setY(0);
                    if (mPrevFrame.getVisibility() == VISIBLE) {
                        mPrevFrame.setY(fy / 1.3f - getHeight());
                        mAction = AnimationAction.PREV;
                    } else {
                        mAction = AnimationAction.NONE;
                    }
                }
                break;
            default:
                if (mAction == AnimationAction.NEXT) {
                    if (mCurrentFrame.getY() > -getHeight() * mBreakThroughPoint) {
                        resetCurrentFrame();
                    } else {
                        forwardToNextFrame();
                    }
                } else if (mAction == AnimationAction.PREV) {
                    if (mPrevFrame.getY() < -getHeight() * (1 - mBreakThroughPoint)) {
                        resetPrevFrame();
                    } else {
                        backToPrevFrame();
                    }
                }
                break;
        }
        return true;
    }

    private void resetPrevFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mPrevFrame, "y", -getHeight());
        ani.setDuration(mResetDuration);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setOnTouchListener(PageView.this);
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
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 滑动距离不够，返回
     */
    private void resetCurrentFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mCurrentFrame, "y", 0);
        ani.setDuration(mResetDuration);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setOnTouchListener(PageView.this);
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
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 进入下一页
     */
    private void forwardToNextFrame() {
        ObjectAnimator ani = ObjectAnimator.ofFloat(mCurrentFrame, "y", -getHeight());
        ani.setDuration(mNextDuration);
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

                setOnTouchListener(PageView.this);

                setDisplayView(mCurrentFrame, mCurrentPos);
                setDisplayView(mNextFrame, mCurrentPos + 1);
                setDisplayView(mPrevFrame, mCurrentPos - 1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
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
        ani.setDuration(mNextDuration);
        ani.setInterpolator(new AccelerateInterpolator());
        ani.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentPos--;
                if (mListener != null) {
                    mListener.onPageChanged(mCurrentPos);
                }

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

                setOnTouchListener(PageView.this);

                setDisplayView(mCurrentFrame, mCurrentPos);
                setDisplayView(mNextFrame, mCurrentPos + 1);
                setDisplayView(mPrevFrame, mCurrentPos - 1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        setOnTouchListener(null);
        ani.start();
    }

    /*
     * 返回另外一个FrameLayout
     */
    private FrameLayout getOtherFrameLayout(FrameLayout frameLayout) {
        if (frameLayout == mCurrentFrame) {
            return mNextFrame;
        }
        return mCurrentFrame;
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
        public void onPageChanged(int pos);         /* 翻页时调用,pos表示新页  */

        public void onCancelled(int pos);           /* 取消翻页时调用, pos表示当前页  */
    }
}