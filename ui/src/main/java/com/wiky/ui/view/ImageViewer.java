package com.wiky.ui.view;

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
    private RectF mOriginRect = new RectF();
    private Rect mBitmapRect = new Rect();
    private GoodGestureDetector mGestureDetector;

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

    private void setScale(float x, float y, float factor) {
        if (mCanvasRect.width() / mOriginRect.width() <= mMinScale && factor <= 1.0f) {
            return;
        } else if (mCanvasRect.width() / mOriginRect.width() >= mMaxScale && factor >= 1.0f) {
            return;
        }

        mCanvasRect.left = x - (x - mCanvasRect.left) * factor;
        mCanvasRect.top = y - (y - mCanvasRect.top) * factor;
        mCanvasRect.right = x - (x - mCanvasRect.right) * factor;
        mCanvasRect.bottom = y - (y - mCanvasRect.bottom) * factor;
        invalidate();
    }

    private void setTranslate(float dx, float dy) {
        mCanvasRect.left -= dx;
        mCanvasRect.right -= dx;
        mCanvasRect.top -= dy;
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
            mOriginRect.left = cx - bWidth / 2.0f * mMinScale;
            mOriginRect.right = cx + bWidth / 2.0f * mMinScale;
            mOriginRect.top = cy - bHeight / 2.0f * mMinScale;
            mOriginRect.bottom = cy + bHeight / 2.0f * mMinScale;
            mCanvasRect.set(mOriginRect);
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
        setTranslate(dx, dy);
    }

    @Override
    public void onScale(float cx, float cy, float factor) {
        setScale(cx, cy, factor);
    }

    @Override
    public void onEnd() {
    }

    private float getCurrentScale() {
        return mCanvasRect.width() / mOriginRect.width();
    }
}
