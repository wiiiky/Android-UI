package com.wiky.ui.layout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.wiky.ui.R;
import com.wiky.ui.utils.SimpleAnimatorListener;

import java.security.InvalidParameterException;

/**
 * Created by wiky on 7/16/15.
 */
public class SegmentPager extends ScrollableLayout {
    private final AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver();
    private FrameLayout mSegment0, mSegment1, mSegment2;
    private RecyclerView.Adapter mAdapter = null;
    private float mSegmentWidthRatio = 0.76f;
    private float mSegmentSpacingRatio = 0.04f;
    private float mSegmentRevealRatio = (1.0f - mSegmentWidthRatio) / 2.0f - mSegmentSpacingRatio;
    private float mMaxOverScrollRatio = 0.15f;
    private float mMaxOverScroll;
    private float mMaxScrollRatio = 0.8f;
    private float mMaxScroll;
    private int mMaxScrollForwardDuration = 300;
    private int mMaxScrollBackDuration = 200;
    private int mMaxScrollOverBackDuration = 200;
    private float mScrollCriticalRatio = 0.2f;
    private float mScrollCritical;
    private float mOffset = 0.0f;
    private int mCurrentPosition = 0;
    private Animator mAnimator = null;
    private Interpolator mScrollForwardInterpolator = new DecelerateInterpolator();
    private Interpolator mScrollBackInterpolator = new DecelerateInterpolator();
    private Interpolator mScrollOverBackInterpolator = new DecelerateInterpolator();
    private int mSegmentWidth;
    private int mSegmentRevealWidth;
    private int mSegmentSpacingWidth;

    public SegmentPager(Context context) {
        this(context, null);
    }

    public SegmentPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SegmentPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context, attrs);
    }

    /* 创建分页的容器 */
    private FrameLayout createSegment(final Context context) {
        FrameLayout v = new FrameLayout(context);
        v.setVisibility(INVISIBLE);
        v.setTag(null);
        addView(v);
        return v;
    }

    private void initialize(Context context, AttributeSet attrs) {
        mSegment0 = createSegment(context);
        mSegment1 = createSegment(context);
        mSegment2 = createSegment(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SegmentPager);
        mSegmentWidthRatio = a.getFloat(R.styleable.SegmentPager_segment_width_ratio, mSegmentWidthRatio);
        mSegmentSpacingRatio = a.getFloat(R.styleable.SegmentPager_segment_spacing_ratio, mSegmentSpacingRatio);
        mSegmentRevealRatio = (1.0f - mSegmentWidthRatio) / 2.0f - mSegmentSpacingRatio;

        mScrollCriticalRatio = a.getFloat(R.styleable.SegmentPager_scroll_critical_ratio, mScrollCriticalRatio);

        mMaxScrollRatio = a.getFloat(R.styleable.SegmentPager_max_scroll_ratio, mMaxScrollRatio);
        mMaxOverScrollRatio = a.getFloat(R.styleable.SegmentPager_max_scroll_over_ratio, mMaxOverScrollRatio);

        mMaxScrollForwardDuration = a.getInt(R.styleable.SegmentPager_max_scroll_forward_duration, mMaxScrollForwardDuration);
        mMaxScrollOverBackDuration = a.getInt(R.styleable.SegmentPager_max_scroll_over_back_duration, mMaxScrollOverBackDuration);
        mMaxScrollBackDuration = a.getInt(R.styleable.SegmentPager_max_scroll_back_duration, mMaxScrollBackDuration);
        a.recycle();

        if (mSegmentWidthRatio < 0 || mSegmentSpacingRatio < 0 || mSegmentRevealRatio < 0 || mScrollCriticalRatio < 0
                || mMaxScrollRatio < 0 || mMaxOverScrollRatio < 0 || mMaxScrollForwardDuration < 0
                || mMaxScrollOverBackDuration < 0 || mMaxScrollBackDuration < 0) {
            throw new InvalidParameterException();
        }
    }

    /*
     * 设置适配器
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        resetSegments();
        fillSegments();
        requestLayout();

        mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
    }

    /*
     * 重置所有页面
     */
    private void resetSegments() {
        if (mAdapter == null) {
            mCurrentPosition = 0;
        } else {
            int total = mAdapter.getItemCount();
            if (mCurrentPosition >= total) {
                mCurrentPosition = Math.max(total - 1, 0);
            }
        }
        mSegment0.setTag(null);
        mSegment0.setTag(null);
        mSegment2.setTag(null);
        mSegment0.removeAllViews();
        mSegment1.removeAllViews();
        mSegment2.removeAllViews();
    }

    /* 在相应的界面填充内容 */
    private void fillSegments() {
        if (mAdapter == null || mAdapter.getItemCount() == 0) {
            mSegment0.setVisibility(INVISIBLE);
            mSegment1.setVisibility(INVISIBLE);
            mSegment2.setVisibility(INVISIBLE);
            mScrollable = false;
            return;
        }
        mScrollable = true;
        fillSegment(mSegment0, 0, mCurrentPosition - 1);
        fillSegment(mSegment1, 1, mCurrentPosition);
        fillSegment(mSegment2, 2, mCurrentPosition + 1);
    }

    /*
     * 在单个页面填充内容
     * @i 是页面下标，只会是0、1和2
     * @j 该页面当前显示的内容下标
     */
    private void fillSegment(FrameLayout segment, int i, int j) {
        if (j < 0 || j >= mAdapter.getItemCount()) {
            segment.setVisibility(INVISIBLE);
        } else {
            showSegment(segment, i, j);
        }
    }

    /* 显示单个页面 */
    private void showSegment(FrameLayout segment, int i, int j) {
        segment.setVisibility(VISIBLE);
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) segment.getTag();
        if (viewHolder == null) {
            /* 如果ViewHolder还没有则创建 */
            viewHolder = mAdapter.onCreateViewHolder(segment, i);
            segment.setTag(viewHolder);
            segment.addView(viewHolder.itemView);
        }

        mAdapter.bindViewHolder(viewHolder, j);
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        measureChildren(width, height);

        mMaxOverScroll = width * mMaxOverScrollRatio;
        mMaxScroll = width * mMaxScrollRatio;
        mScrollCritical = width * mScrollCriticalRatio;
        mSegmentWidth = Math.round(width * mSegmentWidthRatio);
        mSegmentRevealWidth = Math.round(width * mSegmentRevealRatio);
        mSegmentSpacingWidth = Math.round(width * mSegmentSpacingRatio);
    }

    protected void measureChildren(int width, int height) {
        int w = View.MeasureSpec.makeMeasureSpec((int) (width * mSegmentWidthRatio), View.MeasureSpec.EXACTLY);
        int h = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        mSegment0.measure(w, h);
        mSegment1.measure(w, h);
        mSegment2.measure(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int offset = Math.round(mOffset);

        mSegment0.layout(mSegmentRevealWidth - mSegmentWidth + offset, 0, mSegmentRevealWidth + offset, height);
        mSegment1.layout(mSegmentRevealWidth + mSegmentSpacingWidth + offset, 0, mSegmentRevealWidth + mSegmentSpacingWidth + mSegmentWidth + offset, height);
        mSegment2.layout(width - mSegmentRevealWidth + offset, 0, width + mSegmentWidth - mSegmentRevealWidth + offset, height);
    }


    /* 手势操作结束后执行操作
     * 如果滑动距离足够则进入下一个页面或者返回上一个页面
     * 如果在边缘或者滑动距离不够则回弹
     */
    private void afterward() {
        if ((mCurrentPosition == 0 && mOffset > 0) || mCurrentPosition == mAdapter.getItemCount() - 1 && mOffset < 0) {
            mAnimator = scrollOverBack();
        } else {
            float offset = Math.abs(mOffset);
            if (offset < mScrollCritical) {
                mAnimator = scrollBack();
            } else {
                mAnimator = scrollForward();
            }
        }

        mAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator = null;
            }
        });
        mAnimator.start();
    }

    /* 向前滚或者后退 */
    private Animator scrollForward() {
        int width = getMeasuredWidth();
        if (mOffset < 0) {
            width = -width;
        }
        FrameLayout v = mSegment0;
        if (mOffset < 0) {
            mCurrentPosition++;
            mSegment0 = mSegment1;
            mSegment1 = mSegment2;
            mSegment2 = v;
        } else {
            mCurrentPosition--;
            mSegment0 = mSegment2;
            mSegment2 = mSegment1;
            mSegment1 = v;
        }
        mOffset -= width * (mSegmentWidthRatio + mSegmentSpacingRatio);
        ValueAnimator animator = createOffsetAnimator(mOffset, 0);
        animator.setDuration(computeDuration(mOffset, mMaxScroll - mScrollCritical, mMaxScrollForwardDuration));
        animator.setInterpolator(mScrollForwardInterpolator);
        fillSegments();
        return animator;
    }

    private ValueAnimator createOffsetAnimator(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffset = (float) animation.getAnimatedValue();
                requestLayout();
            }
        });
        return animator;
    }

    private Animator scrollBack() {
        ValueAnimator animator = createOffsetAnimator(mOffset, 0);
        animator.setDuration(computeDuration(mOffset, mMaxOverScroll, mMaxScrollBackDuration));
        animator.setInterpolator(mScrollBackInterpolator);
        return animator;
    }

    private Animator scrollOverBack() {
        ValueAnimator animator = createOffsetAnimator(mOffset, 0);
        animator.setDuration(computeDuration(mOffset, mMaxOverScroll, mMaxScrollOverBackDuration));
        animator.setInterpolator(mScrollOverBackInterpolator);
        return animator;
    }


    @Override
    protected void onScrollFinish(MotionEvent e) {
        int action = e.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            afterward();
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
            /* 当用户向右滑动时候，distanceX是负数 */
        mOffset += computeScrollDistance(-distanceX);
        requestLayout();
        return true;
    }

    /* 向右为正 */
    protected float computeScrollDistance(float distance) {
        if (distance * mOffset <= 0) {
            return distance;
        }
        if ((mCurrentPosition == 0 && mOffset > 0) || mCurrentPosition == mAdapter.getItemCount() - 1 && mOffset < 0) {
            /* 左边的边界 */
            return computeScrollEdgeDistance(distance);
        }
        return computeScrollNextDistance(distance);
    }

    protected float computeScrollEdgeDistance(float distance) {
        float offset = Math.abs(mOffset);
        return distance * (1.0f - Math.min(1.0f, offset / mMaxOverScroll));
    }

    protected float computeScrollNextDistance(float distance) {
        float offset = Math.abs(mOffset);
        return distance * (1.0f - Math.min(1.0f, offset / mMaxScroll));
    }

    private class AdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mCurrentPosition >= mAdapter.getItemCount()) {
                mCurrentPosition = Math.min(0, mAdapter.getItemCount() - 1);
            }
            fillSegments();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }
}

