package com.wiky.ui.cardview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

public class CardViewEclairMr1 implements CardViewImpl {
    final RectF sCornerRect = new RectF();

    public CardViewEclairMr1() {
    }

    public void initStatic() {
        RoundRectDrawableWithShadow.sRoundRectHelper = new RoundRectDrawableWithShadow.RoundRectHelper() {
            public void drawRoundRect(Canvas canvas, RectF bounds, float cornerRadius, Paint paint) {
                float twoRadius = cornerRadius * 2.0F;
                float innerWidth = bounds.width() - twoRadius - 1.0F;
                float innerHeight = bounds.height() - twoRadius - 1.0F;
                if (cornerRadius >= 1.0F) {
                    cornerRadius += 0.5F;
                    CardViewEclairMr1.this.sCornerRect.set(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius);
                    int saved = canvas.save();
                    canvas.translate(bounds.left + cornerRadius, bounds.top + cornerRadius);
                    canvas.drawArc(CardViewEclairMr1.this.sCornerRect, 180.0F, 90.0F, true, paint);
                    canvas.translate(innerWidth, 0.0F);
                    canvas.rotate(90.0F);
                    canvas.drawArc(CardViewEclairMr1.this.sCornerRect, 180.0F, 90.0F, true, paint);
                    canvas.translate(innerHeight, 0.0F);
                    canvas.rotate(90.0F);
                    canvas.drawArc(CardViewEclairMr1.this.sCornerRect, 180.0F, 90.0F, true, paint);
                    canvas.translate(innerWidth, 0.0F);
                    canvas.rotate(90.0F);
                    canvas.drawArc(CardViewEclairMr1.this.sCornerRect, 180.0F, 90.0F, true, paint);
                    canvas.restoreToCount(saved);
                    canvas.drawRect(bounds.left + cornerRadius - 1.0F, bounds.top, bounds.right - cornerRadius + 1.0F, bounds.top + cornerRadius, paint);
                    canvas.drawRect(bounds.left + cornerRadius - 1.0F, bounds.bottom - cornerRadius + 1.0F, bounds.right - cornerRadius + 1.0F, bounds.bottom, paint);
                }

                canvas.drawRect(bounds.left, bounds.top + Math.max(0.0F, cornerRadius - 1.0F), bounds.right, bounds.bottom - cornerRadius + 1.0F, paint);
            }
        };
    }

    public void initialize(CardViewDelegate cardView, Context context, int backgroundColor, float radius, float elevation, float maxElevation) {
        RoundRectDrawableWithShadow background = this.createBackground(context, backgroundColor, radius, elevation, maxElevation);
        background.setAddPaddingForCorners(cardView.getPreventCornerOverlap());
        cardView.setBackgroundDrawable(background);
        this.updatePadding(cardView);
    }

    public RoundRectDrawableWithShadow createBackground(Context context, int backgroundColor, float radius, float elevation, float maxElevation) {
        return new RoundRectDrawableWithShadow(context.getResources(), backgroundColor, radius, elevation, maxElevation);
    }

    public void updatePadding(CardViewDelegate cardView) {
        Rect shadowPadding = new Rect();
        this.getShadowBackground(cardView).getMaxShadowAndCornerPadding(shadowPadding);
        ((View) cardView).setMinimumHeight((int) Math.ceil((double) this.getMinHeight(cardView)));
        ((View) cardView).setMinimumWidth((int) Math.ceil((double) this.getMinWidth(cardView)));
        cardView.setShadowPadding(shadowPadding.left, shadowPadding.top, shadowPadding.right, shadowPadding.bottom);
    }

    public void onCompatPaddingChanged(CardViewDelegate cardView) {
    }

    public void onPreventCornerOverlapChanged(CardViewDelegate cardView) {
        this.getShadowBackground(cardView).setAddPaddingForCorners(cardView.getPreventCornerOverlap());
        this.updatePadding(cardView);
    }

    public void setBackgroundColor(CardViewDelegate cardView, int color) {
        this.getShadowBackground(cardView).setColor(color);
    }

    public void setRadius(CardViewDelegate cardView, float radius) {
        this.getShadowBackground(cardView).setCornerRadius(radius);
        this.updatePadding(cardView);
    }

    public float getRadius(CardViewDelegate cardView) {
        return this.getShadowBackground(cardView).getCornerRadius();
    }

    public void setElevation(CardViewDelegate cardView, float elevation) {
        this.getShadowBackground(cardView).setShadowSize(elevation);
    }

    public float getElevation(CardViewDelegate cardView) {
        return this.getShadowBackground(cardView).getShadowSize();
    }

    public void setMaxElevation(CardViewDelegate cardView, float maxElevation) {
        this.getShadowBackground(cardView).setMaxShadowSize(maxElevation);
        this.updatePadding(cardView);
    }

    public float getMaxElevation(CardViewDelegate cardView) {
        return this.getShadowBackground(cardView).getMaxShadowSize();
    }

    public float getMinWidth(CardViewDelegate cardView) {
        return this.getShadowBackground(cardView).getMinWidth();
    }

    public float getMinHeight(CardViewDelegate cardView) {
        return this.getShadowBackground(cardView).getMinHeight();
    }

    private RoundRectDrawableWithShadow getShadowBackground(CardViewDelegate cardView) {
        return (RoundRectDrawableWithShadow) cardView.getBackground();
    }
}
