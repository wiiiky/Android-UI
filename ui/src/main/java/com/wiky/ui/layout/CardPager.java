package com.wiky.ui.layout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.wiky.pageview.R;
import com.wiky.ui.adapter.CardAdapter;

/**
 * Created by wiky on 6/15/15.
 */
public class CardPager extends ViewGroup implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private Context mContext;
    private CardAdapter mAdapter;
    private int mPosition = 0;
    private int mOffset = 0;
    private CardView mCard1, mCard2, mCard3;

    private float mCardHeightRatio = 0.7f;
    private float mCardWidthRatio = 0.84f;
    private float mCardSpacingRatio = 0.04f;
    private float mCardShowRatio = (1.0f - mCardWidthRatio - mCardSpacingRatio * 2.0f) / 2.0f;
    private float mCardOverRatio = 0.15f;

    private boolean mPositionChanged = false;
    private int mWidth = 0;
    private int mHeight = 0;
    private MotionEvent mEvent;
    private GestureDetector mGestureDetector;
    private MotionEvent mLastEvent;

    public CardPager(Context context) throws Exception {
        this(context, null);
    }

    public CardPager(Context context, AttributeSet attrs) throws Exception {
        this(context, attrs, 0);
    }

    public CardPager(Context context, AttributeSet attrs, int defStyleAttr) throws Exception {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardPager);
        mCardHeightRatio = a.getFloat(R.styleable.CardPager_card_height_ratio, 0.7f);
        mCardWidthRatio = a.getFloat(R.styleable.CardPager_card_width_ratio, 0.84f);
        mCardSpacingRatio = a.getFloat(R.styleable.CardPager_card_spacing_ratio, 0.04f);
        mCardOverRatio = a.getFloat(R.styleable.CardPager_card_over_ratio, 0.15f);
        a.recycle();

        if (mCardHeightRatio <= 0.0f || mCardHeightRatio > 1.0f) {
            throw new Exception("Invalid card_height_ratio");
        }
        if (mCardWidthRatio <= 0.0f || mCardWidthRatio > 1.0f) {
            throw new Exception("Invalid card_width_ratio");
        }
        if (mCardSpacingRatio < 0.0f || mCardSpacingRatio >= 1.0f) {
            throw new Exception("Invalid card_spacing_ratio");
        }
        mCardShowRatio = (1.0f - mCardWidthRatio - mCardSpacingRatio * 2.0f) / 2.0f;
        if (mCardShowRatio < 0.0f || mCardShowRatio >= 1.0f) {
            throw new Exception("Invalid");
        }

        initialize(context);
    }

    private CardView createCardView(Context ctx) {
        CardView v = new CardView(ctx);
        v.setRadius(20);
        v.setCardElevation(20);
        addView(v);
        return v;
    }

    private void initialize(Context ctx) {
        mAdapter = null;
        mContext = ctx;
        mCard1 = createCardView(ctx);
        mCard2 = createCardView(ctx);
        mCard3 = createCardView(ctx);

        mGestureDetector = new GestureDetector(ctx, this);
        setOnTouchListener(this);
    }

    public void setAdapter(CardAdapter adapter) {
        mAdapter = adapter;
        mCard1.removeAllViews();
        mCard2.removeAllViews();
        mCard3.removeAllViews();
        mCard1.setTag(null);
        mCard2.setTag(null);
        mCard3.setTag(null);
        mPosition = 0;
        mOffset = 0;
        mPositionChanged = true;

        measure(getMeasuredWidthAndState(), getMeasuredHeightAndState());
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        measureChildren();

        if (mAdapter != null && mCard1.getTag() == null) {
            CardAdapter.ViewHolder viewHolder = mAdapter.createViewHolder(mCard1, 0);
            mCard1.addView(viewHolder.mView);
            mCard1.setTag(viewHolder);
            viewHolder = mAdapter.createViewHolder(mCard2, 1);
            mCard2.addView(viewHolder.mView);
            mCard2.setTag(viewHolder);
            viewHolder = mAdapter.createViewHolder(mCard3, 2);
            mCard3.addView(viewHolder.mView);
            mCard3.setTag(viewHolder);

            measureChildren();
        }
    }

    private void measureChildren() {
        mCard1.measure(MeasureSpec.makeMeasureSpec((int) (mWidth * mCardWidthRatio), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (mHeight * mCardHeightRatio), MeasureSpec.EXACTLY));
        mCard2.measure(MeasureSpec.makeMeasureSpec((int) (mWidth * mCardWidthRatio), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (mHeight * mCardHeightRatio), MeasureSpec.EXACTLY));
        mCard3.measure(MeasureSpec.makeMeasureSpec((int) (mWidth * mCardWidthRatio), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (mHeight * mCardHeightRatio), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layout(l, t, r, b);
        }

        int width = r - l;
        int height = b - t;

        int top = (int) (height * (1.0f - mCardHeightRatio) / 2.0f);
        int bot = (int) (top + height * mCardHeightRatio);
        mCard1.layout((int) (width * (mCardShowRatio - mCardWidthRatio)) + mOffset, top, (int) (width * mCardShowRatio) + mOffset, bot);
        mCard2.layout((int) (width * (mCardShowRatio + mCardSpacingRatio)) + mOffset, top, (int) (width * (mCardShowRatio + mCardSpacingRatio + mCardWidthRatio)) + mOffset, bot);
        mCard3.layout((int) (width * (1.0f - mCardShowRatio)) + mOffset, top, (int) (width * (1.0f - mCardShowRatio + mCardWidthRatio)) + mOffset, bot);
        if (mPosition - 1 < 0 || mPosition - 1 >= mAdapter.size()) {
            mCard1.setVisibility(INVISIBLE);
        } else {
            mCard1.setVisibility(VISIBLE);
            if (mPositionChanged) {
                mAdapter.showViewHolder(mCard1, (CardAdapter.ViewHolder) mCard1.getTag(), mPosition - 1);
            }
        }
        if (mPosition < 0 || mPosition >= mAdapter.size()) {
            mCard2.setVisibility(INVISIBLE);
        } else {
            mCard2.setVisibility(VISIBLE);
            if (mPositionChanged) {
                mAdapter.showViewHolder(mCard2, (CardAdapter.ViewHolder) mCard2.getTag(), mPosition);
            }
        }
        if (mPosition + 1 < 0 || mPosition + 1 >= mAdapter.size()) {
            mCard3.setVisibility(INVISIBLE);
        } else {
            mCard3.setVisibility(VISIBLE);
            if (mPositionChanged) {
                mAdapter.showViewHolder(mCard3, (CardAdapter.ViewHolder) mCard3.getTag(), mPosition + 1);
            }
        }
        mPositionChanged = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int act = event.getAction() & MotionEvent.ACTION_MASK;
        if (act == MotionEvent.ACTION_DOWN) {
            mLastEvent = MotionEvent.obtain(event);
        } else if (act == MotionEvent.ACTION_MOVE) {
            float dx = event.getRawX() - mLastEvent.getRawX();
            mOffset += getScrollDistance(dx);
            requestLayout();
            mLastEvent = MotionEvent.obtain(event);
        } else {
            if (Math.abs(mOffset) <= (mWidth * mCardOverRatio + 0.05) || (mOffset > 0 && mPosition == 0) || (mOffset < 0 && mPosition >= mAdapter.size() - 1)) {
                back();
            } else {
                forward();
            }
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /*
     * 滑动距离不够，返回
     */
    private void back() {
        ValueAnimator animator = createOffsetAnimator(mOffset, 0, 150);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    private void forward() {
        mPositionChanged = true;
        int offset = mOffset;
        int width = mWidth;
        if (offset < 0) {
            width = -width;
        }
        CardView v = mCard2;
        if (offset < 0) {
            mPosition++;
            mCard2 = mCard3;
            mCard3 = mCard1;
            mCard1 = v;
        } else {
            mPosition--;
            mCard2 = mCard1;
            mCard1 = mCard3;
            mCard3 = v;
        }
        mOffset -= width * (mCardWidthRatio + mCardSpacingRatio);
        ValueAnimator animator = createOffsetAnimator(mOffset, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private ValueAnimator createOffsetAnimator(int from, int to) {
        return createOffsetAnimator(from, to, 200);
    }

    private ValueAnimator createOffsetAnimator(int from, int to, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer offset = (Integer) animation.getAnimatedValue();
                mOffset = offset;
                requestLayout();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                CardPager.this.setOnTouchListener(null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                CardPager.this.setOnTouchListener(CardPager.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animator;
    }

    private float getScrollDistance(float dx) {
        int offset = Math.abs(mOffset);
        float width = mWidth * mCardWidthRatio * 0.5f;
        if (offset >= width && dx >= 0) {
            return 0;
        } else if (mPosition == 0 && mOffset >= mWidth * mCardOverRatio && dx >= 0) {
            return 0;
        } else if (mPosition >= mAdapter.size() - 1 && mOffset <= -mWidth * mCardOverRatio && dx <= 0) {
            return 0;
        }
        if ((mPosition <= 0 && dx >= 0) || (mPosition >= mAdapter.size() - 1 && dx <= 0)) {
            return (float) ((1.0f - Math.pow(offset / width, 0.1)) * dx);
        }
        return (float) ((1.0f - Math.pow(offset / width, 0.4)) * dx);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mCard2.getVisibility() != VISIBLE) {
            return false;
        }
        float x = e.getX();
        float y = e.getY();
        Rect rect = new Rect();
        mCard2.getHitRect(rect);
        if (rect.contains((int) x, (int) y)) {
            mAdapter.onItemClick(mCard2, (CardAdapter.ViewHolder) mCard2.getTag(), mPosition);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mCard2.getVisibility() != VISIBLE) {
            return;
        }
        float x = e.getX();
        float y = e.getY();
        Rect rect = new Rect();
        mCard2.getHitRect(rect);
        if (rect.contains((int) x, (int) y)) {
            mAdapter.onLongPress(mCard2, (CardAdapter.ViewHolder) mCard2.getTag(), mPosition);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
