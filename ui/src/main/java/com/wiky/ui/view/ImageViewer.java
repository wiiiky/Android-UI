package com.wiky.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private static final float DEFAULT_MIN_BLOW_SCALE = 0.75f;
    private static final float DEFAULT_MAX_OVER_SCALE = 1.5f;
    private static final float DEFAULT_MAX_SCALE = 2.5f;
    private static final float DEFAULT_MAX_OVER_TRANSLATE = 0.1f;

    private Bitmap mBitmap = null;
    private int mBackgroundColor;
    private float mMinScale = 1.0f;
    private float mMaxBlowScale;
    private float mMaxScale;
    private float mMaxOverScale;
    private float mMaxOverTrans;
    private float mMaxOverTranslate;
    private RectF mCanvasRect = new RectF();    /* 避免在onDraw中分配内存 */
    private GoodGestureDetector mGestureDetector;
    private RectF mOriginRect = new RectF();
    private CanvasScaleType mCanvasScaleType = CanvasScaleType.SMALL;
    private RectF mLargeRect = new RectF();

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

        mMaxScale = a.getFloat(R.styleable.ImageViewer_maxScale, DEFAULT_MAX_SCALE);
        mMaxOverTrans = a.getFloat(R.styleable.ImageViewer_maxOverTrans, DEFAULT_MAX_OVER_TRANSLATE);
        mMaxBlowScale = a.getFloat(R.styleable.ImageViewer_maxBlowScale, DEFAULT_MIN_BLOW_SCALE);
        mMaxOverScale = a.getFloat(R.styleable.ImageViewer_maxOverScale, DEFAULT_MAX_OVER_SCALE);

        mGestureDetector = new GoodGestureDetector(context, this);
        setOnTouchListener(this);
    }

    /* 在现有基础上以(x,y)为中心缩放 */
    private void doScale(float x, float y, float factor) {
        float scale = getBitmapScale();
        if (factor == 1.0f || (factor > 1.0f && scale >= mMaxScale * mMaxOverScale) || (factor < 1.0f && scale <= mMinScale * mMaxBlowScale)) {
            return;
        }
        mCanvasRect.left = x - (x - mCanvasRect.left) * factor;
        mCanvasRect.top = y - (y - mCanvasRect.top) * factor;
        mCanvasRect.right = x - (x - mCanvasRect.right) * factor;
        mCanvasRect.bottom = y - (y - mCanvasRect.bottom) * factor;

        mCanvasScaleType = getCanvasScaleType();
//        if (mCanvasScaleType != CanvasScaleType.SMALL) {
            float width = getWidth();
            float height = getHeight();
            float cWidth = mCanvasRect.width();
            float cHeight = mCanvasRect.height();
            if (width < cWidth) {
                mLargeRect.left = width - cWidth;
                mLargeRect.right = cWidth;
            } else {
                mLargeRect.left = (width - cWidth) / 2.0f;
                mLargeRect.right = (width + cWidth) / 2.0f;
            }
            if (height < cHeight) {
                mLargeRect.top = height - cHeight;
                mLargeRect.bottom = cHeight;
            } else {
                mLargeRect.top = (height - cHeight) / 2.0f;
                mLargeRect.bottom = (height + cHeight) / 2.0f;
            }
//        }
        invalidate();
    }

    /*
     * 在当前位置的基础上移动(dx,dy)
     */
    private void doTranslate(float dx, float dy, boolean force) {
        if (!force) {
//            if (mCanvasScaleType == CanvasScaleType.SMALL) {
//                return;
//            }
            float left = mCanvasRect.left - dx;
            float top = mCanvasRect.top - dy;
            float right = mCanvasRect.right - dx;
            float bottom = mCanvasRect.bottom - dy;
            RectF largeRect = new RectF(mLargeRect);
            largeRect.left -= mMaxOverTranslate;
            largeRect.right += mMaxOverTranslate;
            largeRect.top -= mMaxOverTranslate;
            largeRect.bottom += mMaxOverTranslate;
            /* 下面代码调整移动距离 */
            if (left < largeRect.left && dx > 0) {
                if (mCanvasRect.left > largeRect.left) {
                    dx = mCanvasRect.left - largeRect.left;
                } else {
                    dx = 0;
                }
            } else if (left < mLargeRect.left) {     /* 当移动范围超过时移动将受到阻力 */
                dx *= (left - largeRect.left) / (mLargeRect.left - largeRect.left);
            }
            if (right > largeRect.right && dx < 0) {
                if (mCanvasRect.right < largeRect.right) {
                    dx = mCanvasRect.right - largeRect.right;
                } else {
                    dx = 0;
                }
            } else if (right > mLargeRect.right) {
                dx *= (largeRect.right - right) / (largeRect.right - mLargeRect.right);
            }
            if (top < largeRect.top && dy > 0) {
                if (mCanvasRect.top > largeRect.top) {
                    dy = mCanvasRect.top - largeRect.top;
                } else {
                    dy = 0;
                }
            } else if (top < mLargeRect.top) {
                dy *= (top - largeRect.top) / (mLargeRect.top - largeRect.top);
            }
            if (bottom > largeRect.bottom && dy < 0) {
                if (mCanvasRect.bottom < largeRect.bottom) {
                    dy = mCanvasRect.bottom - largeRect.bottom;
                } else {
                    dy = 0;
                }
            } else if (bottom > mLargeRect.bottom) {
                dy *= (largeRect.bottom - bottom) / (largeRect.bottom - mLargeRect.bottom);
            }
        }
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
            mLargeRect.set(mCanvasRect);

            mMaxOverTranslate = width * mMaxOverTrans;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBackgroundColor);
        if (mBitmap == null) {
            return;
        }
        canvas.drawBitmap(mBitmap, null, mCanvasRect, null);
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
            new ScaleAnimator(scale, mMaxScale, 200, true).start();
        }
        adjustCanvas();
    }

    private float getBitmapScale() {
        if (mCanvasRect.height() > mCanvasRect.width()) {
            return mCanvasRect.width() / mOriginRect.width();
        }
        return mCanvasRect.height() / mOriginRect.height();
    }

    /*
     * 获取当前图片显示尺寸与view尺寸的比例类型
     */
    private CanvasScaleType getCanvasScaleType() {
        float cWidth = mCanvasRect.width();
        float cHeight = mCanvasRect.height();
        float widthScale = cWidth / getWidth();
        float heightScale = cHeight / getHeight();
        if (widthScale <= 1.0f && heightScale <= 1.0f) {
            return CanvasScaleType.SMALL;
        }
        if (cWidth < cHeight) {
            if (widthScale > 1.0f && heightScale <= 1.0f) {
                return CanvasScaleType.WIDTH;
            }
            return CanvasScaleType.LARGE;
        }
        if (widthScale <= 1.0f && heightScale > 1.0f) {
            return CanvasScaleType.HEIGHT;
        }
        return CanvasScaleType.LARGE;
    }

    /* 调整图片显示位置  */
    private void adjustCanvas() {
        float heightOffset = 0.0f;
        float widthOffset = 0.0f;
        if (mCanvasRect.right < getWidth()) {
            widthOffset = -mCanvasRect.right + getWidth();
        } else if (mCanvasRect.left > 0) {
            widthOffset = -mCanvasRect.left;
        }
        if (mCanvasRect.bottom < getHeight()) {
            heightOffset = -mCanvasRect.bottom + getHeight();
        } else if (mCanvasRect.top > 0) {
            heightOffset = -mCanvasRect.top;
        }
        if (mCanvasScaleType == CanvasScaleType.SMALL) {
            new TranslateAnimator(mCanvasRect.centerX(), mCanvasRect.centerY(),
                    mOriginRect.centerX(), mOriginRect.centerY(), 200).start();
        } else if (mCanvasScaleType == CanvasScaleType.WIDTH) {
            new TranslateAnimator(0, mCanvasRect.centerY(), widthOffset, mOriginRect.centerY(), 200).start();
        } else if (mCanvasScaleType == CanvasScaleType.HEIGHT) {
            new TranslateAnimator(mCanvasRect.centerX(), 0, mOriginRect.centerX(), heightOffset, 200).start();
        } else {
            new TranslateAnimator(0, 0, widthOffset, heightOffset, 200).start();
        }
    }

    private void doAdjust() {
        float heightOffset = 0.0f;
        float widthOffset = 0.0f;
        if (mCanvasRect.right < getWidth()) {
            widthOffset = -mCanvasRect.right + getWidth();
        } else if (mCanvasRect.left > 0) {
            widthOffset = -mCanvasRect.left;
        }
        if (mCanvasRect.bottom < getHeight()) {
            heightOffset = -mCanvasRect.bottom + getHeight();
        } else if (mCanvasRect.top > 0) {
            heightOffset = -mCanvasRect.top;
        }
        doTranslate(-widthOffset, -heightOffset, true);
    }

    private enum CanvasScaleType {
        SMALL,  /* 图片显示尺寸小 */
        WIDTH,  /* 图片显示的横向尺寸大于view，纵向尺寸小于view */
        HEIGHT, /* 图片显示的的纵向尺寸大于view，横向尺寸小于view */
        LARGE,  /* 图片显示的横纵向尺寸都大于view */
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
            this(from, to, duration, false);
        }

        public ScaleAnimator(float from, float to, int duration, boolean adjust) {
            mAnimator = ValueAnimator.ofFloat(from, to);
            mAnimator.setDuration(duration);
            mAnimator.addUpdateListener(new ScaleAnimationListener(from, to, adjust) {
                @Override
                public void onAnimationEnd() {
                    mCanvasScaleType = getCanvasScaleType();
                    adjustCanvas();
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
        private boolean mAdjust;

        public ScaleAnimationListener(float from, float to, boolean adjust) {
            mFrom = from;
            mTo = to;
            mAdjust = adjust;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float scale = (Float) animation.getAnimatedValue();
            float ds = scale / mFrom;
            mFrom = scale;
            doScale(mCanvasRect.centerX(), mCanvasRect.centerY(), ds);
            if (mAdjust) {
                doAdjust();
            }
            if (scale == mTo) {
                onAnimationEnd();
            }
        }

        public abstract void onAnimationEnd();
    }
}
