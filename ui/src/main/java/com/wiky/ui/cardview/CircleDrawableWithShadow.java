package com.wiky.ui.cardview;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.wiky.ui.R;

/**
 * Created by wiky on 7/31/15.
 */
public class CircleDrawableWithShadow extends Drawable {

    private Paint mShadowPaint =new Paint();
    private Paint mCirclePaint=new Paint();
    private final int mShadowSize;
    private final int mShadowStartColor;
    private final int mShadowEndColor;
    private boolean mDirty=true;

    public CircleDrawableWithShadow(Resources resources, int color){
        mShadowSize=resources.getDimensionPixelSize(R.dimen.cardview_default_elevation);
        mShadowStartColor=resources.getColor(R.color.cardview_shadow_start_color);
        mShadowEndColor=resources.getColor(R.color.cardview_shadow_end_color);
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setDither(true);

        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(color);
    }


    @Override
    public int getIntrinsicWidth(){
        return 100;
    }

    @Override
    public int getIntrinsicHeight(){
        return 100;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mDirty = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if(mDirty){
            buildComponents();
        }
        drawShadow(canvas);
        drawCircle(canvas);
    }

    private void drawShadow(Canvas canvas){
        canvas.drawCircle(mCenterX, mCenterX, mShadowRadius, mShadowPaint);
    }

    private void drawCircle(Canvas canvas){
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mCirclePaint);
    }

    private float mCenterX;
    private float mCenterY;
    private float mShadowRadius;
    private float mCircleRadius;

    private void buildComponents(){
        mDirty=false;

        Rect outRect=getBounds();
        mShadowRadius=Math.min(outRect.width(),outRect.height())/2.0f;
        mCircleRadius=mShadowRadius-mShadowSize;
        mCenterX=outRect.centerX();
        mCenterY=outRect.centerY();
        RadialGradient radialGradient=new RadialGradient(mCenterX,mCenterY, mShadowRadius,
                new int[]{mShadowStartColor, mShadowStartColor, mShadowEndColor},
                new float[]{0.0f, (mShadowRadius-mShadowSize*2.0f)/mShadowRadius, 1.0f},
                Shader.TileMode.CLAMP);
        mShadowPaint.setShader(radialGradient);
    }

    @Override
    public void setAlpha(int alpha) {
        mShadowPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mShadowPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mShadowPaint.getAlpha();
    }
}
