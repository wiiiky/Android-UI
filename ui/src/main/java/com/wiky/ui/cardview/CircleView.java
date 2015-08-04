package com.wiky.ui.cardview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.wiky.ui.R;

/**
 * Created by wiky on 8/4/15.
 */
public class CircleView extends View{
    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context, attrs);
    }

    private CircleDrawableWithShadow mDrawable;

    private void initialize(Context context, AttributeSet attrs){

        TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        int backgroundColor=a.getColor(R.styleable.CircleView_circleBackgroundColor, Color.WHITE);
        mDrawable=new CircleDrawableWithShadow(context.getResources(), backgroundColor);
        a.recycle();
    }

    @Override
    public void onMeasure(int w, int h){
        if(MeasureSpec.getMode(w)!=MeasureSpec.EXACTLY){
            w=MeasureSpec.makeMeasureSpec(mDrawable.getIntrinsicWidth(), MeasureSpec.EXACTLY);
        }
        if(MeasureSpec.getMode(h)!=MeasureSpec.EXACTLY){
            h=MeasureSpec.makeMeasureSpec(mDrawable.getIntrinsicHeight(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(w, h);
    }

    @Override
    public void onDraw(Canvas canvas){
        int w=getMeasuredWidth();
        int h=getMeasuredHeight();

        float r= Math.min(h,w)/2.0f;
        float x=w/2.0f;
        float y=h/2.0f;
        mDrawable.setBounds((int)(x-r),(int)(y-r),(int)(x+r), (int) (y+r));
        mDrawable.draw(canvas);
    }
}
