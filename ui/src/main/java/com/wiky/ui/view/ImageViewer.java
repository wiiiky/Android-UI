package com.wiky.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wiky.pageview.R;
import com.wiky.ui.utils.GoodGestureDetector;

/**
 * Created by wiky on 4/8/15.
 */
public class ImageViewer extends View implements View.OnTouchListener, GoodGestureDetector.OnGestureListener {
    private Bitmap mBitmap = null;
    private int mBackgroundColor;
    private float mMinScale = 1.0f;
    private float mMaxScale = 2.0f;
    private RectF mCanvasRect = new RectF();    /* 避免在onDraw中分配内存 */
    private Rect mBitmapRect = new Rect();
    private GoodGestureDetector mGestureDetector;
    private RectF mOriginRect = new RectF();

    public ImageViewer(Context context) {
        this(context, null);
    }

    public ImageViewer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewer);
        int id = a.getResourceId(R.styleable.ImageViewer_src, 0);
        if (id != 0) {
            Drawable drawable = context.getResources().getDrawable(id);
            if (drawable != null) {
                mBitmap = ((BitmapDrawable) drawable).getBitmap();
                mBitmapRect.top = 0;
                mBitmapRect.left = 0;
                mBitmapRect.right = mBitmap.getWidth();
                mBitmapRect.bottom = mBitmap.getHeight();
            }
        }
        mBackgroundColor = a.getColor(R.styleable.ImageViewer_background_color, Color.BLACK);
        id = a.getResourceId(R.styleable.ImageViewer_background_color, 0);
        if (id != 0) {
            mBackgroundColor = context.getResources().getColor(id);
        }
        String color = a.getString(R.styleable.ImageViewer_background_color);
        if (color != null) {
            mBackgroundColor = Color.parseColor(color);
        }
        a.recycle();

        mGestureDetector = new GoodGestureDetector(context, this);
        setOnTouchListener(this);
    }

    private void doScale(float x, float y, float factor) {
        float scale = getBitmapScale();
        if (factor == 1.0f || (factor > 1.0f && scale >= mMaxScale * 1.2) || (factor < 1.0f && scale <= mMinScale * 0.8)) {
            return;
        }
        mCanvasRect.left = x - (x - mCanvasRect.left) * factor;
        mCanvasRect.top = y - (y - mCanvasRect.top) * factor;
        mCanvasRect.right = x - (x - mCanvasRect.right) * factor;
        mCanvasRect.bottom = y - (y - mCanvasRect.bottom) * factor;
        invalidate();
    }

    /*
     * 在当前位置的基础上移动(dx,dy)
     */
    private void doTranslate(float dx, float dy, boolean force) {
        if (getCanvasScale() <= 1.0f && force == false) {
            return;
        }
        float left = mCanvasRect.left - dx;
        float top = mCanvasRect.top - dy;
        float right = mCanvasRect.right - dx;
        float bottom = mCanvasRect.bottom - dy;
        mCanvasRect.left -= dx;
        mCanvasRect.top -= dy;
        mCanvasRect.right -= dx;
        mCanvasRect.bottom -= dy;
        invalidate();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmap != null) {
            int width = right - left;
            int height = bottom - top;
            int bWidth = mBitmap.getWidth();
            int bHeight = mBitmap.getHeight();
            float fx = (float) bWidth / (float) width;
            float fy = (float) bHeight / (float) height;
            mMinScale = fx > fy ? fx : fy;
            mMinScale = 1.0f / mMinScale;
            if (mMinScale > 1.0f) {
                mMinScale = 1.0f;
            }
            float cx = width / 2.0f;
            float cy = height / 2.0f;
            mCanvasRect.left = cx - bWidth / 2.0f * mMinScale;
            mCanvasRect.right = cx + bWidth / 2.0f * mMinScale;
            mCanvasRect.top = cy - bHeight / 2.0f * mMinScale;
            mCanvasRect.bottom = cy + bHeight / 2.0f * mMinScale;
            mOriginRect.set(mCanvasRect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBackgroundColor);
        if (mBitmap == null) {
            return;
        }
        canvas.drawBitmap(mBitmap, mBitmapRect, mCanvasRect, null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onStart(MotionEvent e) {
        return true;
    }

    @Override
    public void onScroll(float dx, float dy) {
        doTranslate(dx, dy, false);
    }

    @Override
    public void onScale(float cx, float cy, float factor) {
        doScale(cx, cy, factor);
    }

    @Override
    public void onEnd() {
        float scale = getBitmapScale();
        if (scale < mMinScale) {
            new ScaleAnimator(scale, mMinScale, 200).start();
        } else if (scale > mMaxScale) {
            new ScaleAnimator(scale, mMaxScale, 200).start();
        }
        if (getCanvasScale() <= 1.0f) {
            new TranslateAnimator(mCanvasRect.centerX(), mCanvasRect.centerY(),
                    mOriginRect.centerX(), mOriginRect.centerY(), 200).start();
        }
    }

    private float getBitmapScale() {
        if (mCanvasRect.height() > mCanvasRect.width()) {
            return mCanvasRect.width() / mOriginRect.width();
        }
        return mCanvasRect.height() / mOriginRect.height();
    }

    private float getCanvasScale() {
        if (mCanvasRect.height() > mCanvasRect.width()) {
            return mCanvasRect.width() / getWidth();
        }
        return mCanvasRect.height() / getHeight();
    }

    private class TranslateAnimator {
        private ValueAnimator mXAnimator;
        private ValueAnimator mYAnimator;

        public TranslateAnimator(float fromx, float fromy, float tox, float toy, int duration) {
            mXAnimator = ValueAnimator.ofFloat(fromx, tox);
            mXAnimator.setDuration(duration);
            mXAnimator.addUpdateListener(new TranslateAnimatorListener(fromx, tox, true) {
                @Override
                public void onAnimationEnd() {
                    ImageViewer.this.setOnTouchListener(ImageViewer.this);
                }
            });
            mYAnimator = ValueAnimator.ofFloat(fromy, toy);
            mYAnimator.setDuration(duration);
            mYAnimator.addUpdateListener(new TranslateAnimatorListener(fromy, toy, false) {
                @Override
                public void onAnimationEnd() {
                    ImageViewer.this.setOnTouchListener(ImageViewer.this);
                }
            });
        }

        public void start() {
            ImageViewer.this.setOnTouchListener(null);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(mXAnimator, mYAnimator);
            set.start();
        }
    }

    private abstract class TranslateAnimatorListener implements ValueAnimator.AnimatorUpdateListener {
        private float mFrom;
        private float mTo;
        private boolean mX;

        public TranslateAnimatorListener(float from, float to, boolean x) {
            mFrom = from;
            mTo = to;
            mX = x;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float v = (Float) animation.getAnimatedValue();
            float ds = v - mFrom;
            mFrom = v;
            if (mX) {
                doTranslate(-ds, 0, true);
            } else {
                doTranslate(0, -ds, true);
            }
            if (v == mTo) {
                onAnimationEnd();
            }
        }

        public abstract void onAnimationEnd();
    }

    private class ScaleAnimator {
        private ValueAnimator mAnimator;

        public ScaleAnimator(float from, float to, int duration) {
            mAnimator = ValueAnimator.ofFloat(from, to);
            mAnimator.setDuration(duration);
            mAnimator.addUpdateListener(new ScaleAnimationListener(from, to) {
                @Override
                public void onAnimationEnd() {
                    ImageViewer.this.setOnTouchListener(ImageViewer.this);
                }
            });
        }

        public void start() {
            ImageViewer.this.setOnTouchListener(null);
            mAnimator.start();
        }
    }

    private abstract class ScaleAnimationListener implements ValueAnimator.AnimatorUpdateListener {
        private float mFrom;
        private float mTo;

        public ScaleAnimationListener(float from, float to) {
            mFrom = from;
            mTo = to;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float scale = (Float) animation.getAnimatedValue();
            float ds = scale / mFrom;
            mFrom = scale;
            doScale(mCanvasRect.centerX(), mCanvasRect.centerY(), ds);
            if (scale == mTo) {
                onAnimationEnd();
            }
        }

        public abstract void onAnimationEnd();
    }
}
