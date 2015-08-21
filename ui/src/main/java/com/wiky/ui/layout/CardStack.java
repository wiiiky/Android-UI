package com.wiky.ui.layout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.wiky.ui.R;
import com.wiky.ui.cardview.CardView;
import com.wiky.ui.utils.SimpleAnimatorListener;


/**
 * Created by wiky on 8/20/15.
 */
public class CardStack extends ViewGroup implements GestureDetector.OnGestureListener {

    private CardView mCard0;
    private CardView mCard1;
    private CardView mCard2;
    private CardView mCard3;

    private float mCardWidth;
    private float mCardHeight;

    private int mCardPaddingLeft = 50;
    private int mCardPaddingRight = 50;
    private int mCardPaddingTop = 40;
    private int mCardPaddingBottom = 40;

    private CardStackAdapter mAdapter = null;
    private int mPosition = 0;
    private int mCardPadding = 20;
    private GestureDetector mGestureDetector;
    private float mOffset = 0;
    private float mMaxOffset;
    private float mCriticalOffset;
    private float mCardScale2 = 0.95f;
    private float mCardScale3 = 0.9f;
    private float mCardScale4 = 0.85f;
    private float mCardAlpha2 = 0.7f;
    private float mCardAlpha3 = 0.3f;
    private float mCardAlpha4 = 0.0f;

    private boolean mVertical = false;
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            /* 一次只会有一个item更新 */
            if (invalidateCard(mCard0, positionStart) || invalidateCard(mCard1, positionStart) || invalidateCard(mCard2, positionStart) || invalidateCard(mCard3, positionStart)) {
                requestLayout();
            }
        }

        private boolean invalidateCard(CardView cardView, int pos) {
            ViewHolder viewHolder = (ViewHolder) cardView.getTag();
            if (viewHolder != null && viewHolder.pos == pos) {
                viewHolder.pos = -10;
                return true;
            }
            return false;
        }
    };
    private Animator mAnimator = null;
    private float mAnimating = 0.0f;
    private OnCardStackListener mListener = null;

    public CardStack(Context context) {
        this(context, null);
    }

    public CardStack(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardStack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        mCard0 = createCardView(context);
        mCard1 = createCardView(context);
        mCard2 = createCardView(context);
        mCard3 = createCardView(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardStack);
        mCardPaddingLeft = a.getDimensionPixelSize(R.styleable.CardStack_card_padding_left, mCardPaddingLeft);
        mCardPaddingRight = a.getDimensionPixelSize(R.styleable.CardStack_card_padding_right, mCardPaddingRight);
        mCardPaddingTop = a.getDimensionPixelSize(R.styleable.CardStack_card_padding_top, mCardPaddingTop);
        mCardPaddingBottom = a.getDimensionPixelSize(R.styleable.CardStack_card_padding_bottom, mCardPaddingBottom);
        mCardPadding = a.getDimensionPixelSize(R.styleable.CardStack_card_padding, mCardPadding);
        mVertical = a.getInt(R.styleable.CardStack_card_layout_orientation, 0) > 0;
        a.recycle();

        mGestureDetector = new GestureDetector(context, this);
    }

    private CardView createCardView(Context context) {
        CardView cardView = new CardView(context);
        cardView.setRadius(1);
        addView(cardView);
        return cardView;
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);

        w = getMeasuredWidth();
        h = getMeasuredHeight();

        mCardWidth = w - mCardPaddingLeft - mCardPaddingRight;
        mCardHeight = h - mCardPaddingTop - mCardPaddingBottom;

        measureChildren(MeasureSpec.makeMeasureSpec((int) mCardWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) mCardHeight, MeasureSpec.EXACTLY));

        if (!mVertical) {
            float halfHeight = mCardHeight / 2.0f;
            mCard0.setPivotX(mCardWidth);
            mCard0.setPivotY(halfHeight);
            mCard1.setPivotX(mCardWidth);
            mCard1.setPivotY(halfHeight);
            mCard2.setPivotX(mCardWidth);
            mCard2.setPivotY(halfHeight);
            mCard3.setPivotX(mCardWidth);
            mCard3.setPivotY(halfHeight);
        } else {
            float halfWidth = mCardWidth / 2.0f;
            mCard0.setPivotX(halfWidth);
            mCard0.setPivotY(0);
            mCard1.setPivotX(halfWidth);
            mCard1.setPivotY(0);
            mCard2.setPivotX(halfWidth);
            mCard2.setPivotY(0);
            mCard3.setPivotX(halfWidth);
            mCard3.setPivotY(0);
        }

        mMaxOffset = mCardPaddingLeft + mCardWidth + 20;
        mCriticalOffset = mCardWidth / 3.0f;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mCard0.setVisibility(INVISIBLE);
        mCard1.setVisibility(INVISIBLE);
        mCard2.setVisibility(INVISIBLE);
        mCard3.setVisibility(INVISIBLE);
        int count = getItemCount();
        if (count <= 0) {
            return;
        }

        callOnScroll();
        if (mOffset >= 0 || mAnimating > 0) {
            nextLayout(count);
        } else {
            lastLayout(count);
        }
    }

    private void updateCardLayout(CardView card, float left, float top, float right, float bottom, float scale, float alpha, int pos) {
        card.layout((int) left, (int) top, (int) right, (int) bottom);
        card.setScaleX(scale);
        card.setScaleY(scale);
        card.setAlpha(alpha);
        card.setVisibility(VISIBLE);

        updateCard(card, pos);
    }

    public void setPosition(int pos) {
        if (pos >= 0 && pos < getItemCount()) {
            mPosition = pos;
            requestLayout();
        }
    }

    public void prepareAnimation() {
        if (mAnimating > 0) {
            return;
        }
        mAnimating = 1.0f;
        requestLayout();
    }

    public void startAnimation() {
        if (mAnimating != 1.0) {
            return;
        }
        mOffset = 0;
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, -0.2f, 0.1f, 0.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimating = (float) animation.getAnimatedValue();
                requestLayout();
            }
        });
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(250);
        animator.start();
    }

    private void lastLayout(int count) {
        if (!mVertical) {
            horizontalLastLayout(count);
        } else {
            verticalLastLayout(count);
        }
        mCard1.bringToFront();
        mCard0.bringToFront();
        mCard3.bringToFront();
    }

    private void verticalLastLayout(int count) {
        float dOffset = Math.max(0, mMaxOffset + mOffset);
        float ratio = Math.min(dOffset / mCardWidth, 1.0f);
        int offset = (int) -dOffset;
        float scale = 1.0f;
        float alpha = 1.0f;

        updateCardLayout(mCard3, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition - 1);

        if (mPosition >= count) {
            return;
        }

        offset = (int) (mCardPadding - mCardPadding * ratio);
        scale = mCardScale2 + ratio * (1 - mCardScale2);
        alpha = mCardAlpha2 + ratio * (1 - mCardAlpha2);
        updateCardLayout(mCard0, mCardPaddingLeft, mCardPaddingTop - offset, mCardPaddingLeft + mCardWidth, mCardPaddingTop + mCardHeight - offset, scale, alpha, mPosition);

        if (mPosition + 1 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 2 - mCardPadding * ratio);
        scale = mCardScale3 + ratio * (mCardScale2 - mCardScale3);
        alpha = mCardAlpha3 + ratio * (mCardAlpha2 - mCardAlpha3);
        updateCardLayout(mCard1, mCardPaddingLeft, mCardPaddingTop - offset, mCardPaddingLeft + mCardWidth, mCardPaddingTop + mCardHeight - offset, scale, alpha, mPosition + 1);

        if (mPosition + 2 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 3 - mCardPadding * ratio);
        scale = mCardScale4 + ratio * (mCardScale3 - mCardScale4);
        alpha = mCardAlpha4 + ratio * (mCardAlpha3 - mCardAlpha4);
        updateCardLayout(mCard2, mCardPaddingLeft, mCardPaddingTop - offset, mCardPaddingLeft + mCardWidth, mCardPaddingTop + mCardHeight - offset, scale, alpha, mPosition + 2);
    }

    private void horizontalLastLayout(int count) {
        float dOffset = Math.max(0, mMaxOffset + mOffset);
        float ratio = Math.min(dOffset / mCardWidth, 1.0f);
        int offset = (int) -dOffset;
        float scale = 1.0f;
        float alpha = 1.0f;

        updateCardLayout(mCard3, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition - 1);

        if (mPosition >= count) {
            return;
        }

        offset = (int) (mCardPadding - mCardPadding * ratio);
        scale = mCardScale2 + ratio * (1 - mCardScale2);
        alpha = mCardAlpha2 + ratio * (1 - mCardAlpha2);
        updateCardLayout(mCard0, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition);

        if (mPosition + 1 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 2 - mCardPadding * ratio);
        scale = mCardScale3 + ratio * (mCardScale2 - mCardScale3);
        alpha = mCardAlpha3 + ratio * (mCardAlpha2 - mCardAlpha3);
        updateCardLayout(mCard1, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition + 1);

        if (mPosition + 2 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 3 - mCardPadding * ratio);
        scale = mCardScale4 + ratio * (mCardScale3 - mCardScale4);
        alpha = mCardAlpha4 + ratio * (mCardAlpha3 - mCardAlpha4);
        updateCardLayout(mCard2, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition + 2);
    }

    private void nextLayout(int count) {
        if (!mVertical) {
            horizontalNextLayout(count);
        } else {
            verticalNextLayout(count);
        }
        mCard2.bringToFront();
        mCard1.bringToFront();
        mCard0.bringToFront();
    }

    private void verticalNextLayout(int count) {
        float ratio = Math.min(mOffset / mCardWidth, 1.0f);

        int offset = (int) -mOffset;
        float scale = 1.0f;
        float alpha = 1.0f;
        updateCardLayout(mCard0, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition);

        if (mPosition + 1 >= count) {
            return;
        }

        offset = (int) (mCardPadding - mCardPadding * ratio - mAnimating * mCardPadding);
        scale = mCardScale2 + ratio * (1 - mCardScale2);
        alpha = mCardAlpha2 + ratio * (1 - mCardAlpha2);
        updateCardLayout(mCard1, mCardPaddingLeft, mCardPaddingTop - offset, mCardPaddingLeft + mCardWidth, mCardPaddingTop + mCardHeight - offset, scale, alpha, mPosition + 1);

        if (mPosition + 2 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 2 - mCardPadding * ratio - mAnimating * mCardPadding * 2);
        scale = mCardScale3 + ratio * (mCardScale2 - mCardScale3);
        alpha = mCardAlpha3 + ratio * (mCardAlpha2 - mCardAlpha3);
        updateCardLayout(mCard2, mCardPaddingLeft, mCardPaddingTop - offset, mCardPaddingLeft + mCardWidth, mCardPaddingTop + mCardHeight - offset, scale, alpha, mPosition + 2);

        if (mPosition + 3 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 3 - mCardPadding * ratio - mAnimating * mCardPadding * 3);
        scale = mCardScale4 + ratio * (mCardScale3 - mCardScale4);
        alpha = mCardAlpha4 + ratio * (mCardAlpha3 - mCardAlpha4);
        updateCardLayout(mCard3, mCardPaddingLeft, mCardPaddingTop - offset, mCardPaddingLeft + mCardWidth, mCardPaddingTop + mCardHeight - offset, scale, alpha, mPosition + 3);
    }

    private void horizontalNextLayout(int count) {
        float ratio = Math.min(mOffset / mCardWidth, 1.0f);

        int offset = (int) -mOffset;
        float scale = 1.0f;
        float alpha = 1.0f;
        updateCardLayout(mCard0, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition);

        if (mPosition + 1 >= count) {
            return;
        }

        offset = (int) (mCardPadding - mCardPadding * ratio - mAnimating * mCardPadding);
        scale = mCardScale2 + ratio * (1 - mCardScale2);
        alpha = mCardAlpha2 + ratio * (1 - mCardAlpha2);
        updateCardLayout(mCard1, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition + 1);

        if (mPosition + 2 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 2 - mCardPadding * ratio - mAnimating * mCardPadding * 2);
        scale = mCardScale3 + ratio * (mCardScale2 - mCardScale3);
        alpha = mCardAlpha3 + ratio * (mCardAlpha2 - mCardAlpha3);
        updateCardLayout(mCard2, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition + 2);

        if (mPosition + 3 >= count) {
            return;
        }

        offset = (int) (mCardPadding * 3 - mCardPadding * ratio - mAnimating * mCardPadding * 3);
        scale = mCardScale4 + ratio * (mCardScale3 - mCardScale4);
        alpha = mCardAlpha4 + ratio * (mCardAlpha3 - mCardAlpha4);
        updateCardLayout(mCard3, mCardPaddingLeft + offset, mCardPaddingTop, mCardPaddingLeft + mCardWidth + offset, mCardPaddingTop + mCardHeight, scale, alpha, mPosition + 3);

    }

    public void setAdapter(CardStackAdapter adapter) {
        mAdapter = adapter;
        mPosition = 0;
        mCard0.setTag(null);
        mCard1.setTag(null);
        mCard2.setTag(null);
        mCard3.setTag(null);
        mCard0.removeAllViews();
        mCard1.removeAllViews();
        mCard2.removeAllViews();
        mCard3.removeAllViews();
        if (mAdapter != null) {
            mAdapter.registerAdapterDataObserver(mDataObserver);
        }
        requestLayout();
    }

    private void updateCard(CardView card, int pos) {
        ViewHolder viewHolder = (ViewHolder) card.getTag();
        if (viewHolder == null) {
            viewHolder = (ViewHolder) mAdapter.createViewHolder(card, 0);
            card.setTag(viewHolder);
            card.addView(viewHolder.itemView);
        }
        mAdapter.bindView(viewHolder, pos);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            onEnd();
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private void cancelAnimator() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    private void onEnd() {
        if (mOffset == 0) {
            return;
        }
        cancelAnimator();

        ValueAnimator animator = null;
        if (mOffset > 0) {
            if (mOffset > mCriticalOffset && mPosition < getItemCount() - 1) {
                animator = ValueAnimator.ofFloat(mOffset, mMaxOffset);
                animator.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator = null;
                        mOffset = 0;
                        mPosition++;
                        CardView tmp = mCard0;
                        mCard0 = mCard1;
                        mCard1 = mCard2;
                        mCard2 = mCard3;
                        mCard3 = tmp;
                        requestLayout();
                        callOnPositionChanged();
                    }
                });
            } else {
                animator = ValueAnimator.ofFloat(mOffset, 0);
                animator.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator = null;
                        requestLayout();
                    }
                });
            }
        } else {
            if (mOffset < -mCriticalOffset && mPosition > 0) {
                animator = ValueAnimator.ofFloat(mOffset, -mMaxOffset);
                animator.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mPosition--;
                        mAnimator = null;
                        mOffset = 0;
                        CardView tmp = mCard0;
                        mCard0 = mCard3;
                        mCard3 = mCard2;
                        mCard2 = mCard1;
                        mCard1 = tmp;
                        requestLayout();
                        callOnPositionChanged();
                    }
                });
            } else if (mOffset >= -mCriticalOffset) {
                animator = ValueAnimator.ofFloat(mOffset, 0);
                animator.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator = null;
                        requestLayout();
                    }
                });
            }
        }

        if (animator != null) {
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mOffset = (float) animation.getAnimatedValue();
                    requestLayout();
                }
            });
            mAnimator = animator;
            mAnimator.start();
        } else {
            mOffset = 0;
        }
    }

    private int getItemCount() {
        if (mAdapter != null) {
            return mAdapter.getItemCount();
        }
        return 0;
    }

    public void setOnCardStackListener(OnCardStackListener listener) {
        mListener = listener;
    }

    private void callOnSingleTapUp() {
        if (mListener != null) {
            mListener.onSingleTapUp(mPosition);
        }
    }

    public void callOnPositionChanged() {
        if (mListener != null) {
            mListener.onPositionChanged(mPosition);
        }
    }

    public void callOnScroll() {
        if (mListener != null) {
            mListener.onScroll(mPosition, mOffset / mMaxOffset);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        cancelAnimator();
        return getItemCount() > 0 && mAnimating == 0;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        if (x > mCard0.getLeft() && x < mCard0.getRight() && y > mCard0.getTop() && y < mCard0.getBottom()) {
            callOnSingleTapUp();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (distanceX == 0) {
            return true;
        }
        mOffset += distanceX;
        mOffset = Math.max(-mMaxOffset, Math.min(mOffset, mMaxOffset));
        if (mOffset < 0 && mPosition <= 0) {
            return false;
        }
        requestLayout();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public interface OnCardStackListener {
        void onSingleTapUp(int pos);

        void onPositionChanged(int pos);

        void onScroll(int pos, float offset);
    }

    public static abstract class CardStackAdapter extends RecyclerView.Adapter {
        public void bindView(ViewHolder viewHolder, int pos) {
            if (pos < 0 || pos >= getItemCount() || viewHolder.pos == pos) {
                return;
            }
            viewHolder.pos = pos;
            onBindViewHolder(viewHolder, pos);
        }
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder {

        protected int pos = -10;  /* 当前所表示的位置 */

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
