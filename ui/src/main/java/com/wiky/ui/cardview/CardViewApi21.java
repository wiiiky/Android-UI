package com.wiky.ui.cardview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

public class CardViewApi21 implements CardViewImpl {
    public CardViewApi21() {
    }

    public void initialize(CardViewDelegate cardView, Context context, int backgroundColor, float radius, float elevation, float maxElevation) {
        RoundRectDrawable backgroundDrawable = new RoundRectDrawable(backgroundColor, radius);
        cardView.setBackgroundDrawable(backgroundDrawable);
        View view = (View) cardView;
        view.setClipToOutline(true);
        view.setElevation(elevation);
        this.setMaxElevation(cardView, maxElevation);
    }

    public Drawable createBackground(Context context, int backgroundColor, float radius, float elevation, float maxElevation) {
        return new RoundRectDrawable(backgroundColor, radius);
    }

    public void setRadius(CardViewDelegate cardView, float radius) {
        ((RoundRectDrawable) ((RoundRectDrawable) cardView.getBackground())).setRadius(radius);
    }

    public void initStatic() {
    }

    public void setMaxElevation(CardViewDelegate cardView, float maxElevation) {
        ((RoundRectDrawable) ((RoundRectDrawable) cardView.getBackground())).setPadding(maxElevation, cardView.getUseCompatPadding(), cardView.getPreventCornerOverlap());
        this.updatePadding(cardView);
    }

    public float getMaxElevation(CardViewDelegate cardView) {
        return ((RoundRectDrawable) ((RoundRectDrawable) cardView.getBackground())).getPadding();
    }

    public float getMinWidth(CardViewDelegate cardView) {
        return this.getRadius(cardView) * 2.0F;
    }

    public float getMinHeight(CardViewDelegate cardView) {
        return this.getRadius(cardView) * 2.0F;
    }

    public float getRadius(CardViewDelegate cardView) {
        return ((RoundRectDrawable) ((RoundRectDrawable) cardView.getBackground())).getRadius();
    }

    public void setElevation(CardViewDelegate cardView, float elevation) {
        ((View) cardView).setElevation(elevation);
    }

    public float getElevation(CardViewDelegate cardView) {
        return ((View) cardView).getElevation();
    }

    public void updatePadding(CardViewDelegate cardView) {
        if (!cardView.getUseCompatPadding()) {
            cardView.setShadowPadding(0, 0, 0, 0);
        } else {
            float elevation = this.getMaxElevation(cardView);
            float radius = this.getRadius(cardView);
            int hPadding = (int) Math.ceil((double) RoundRectDrawableWithShadow.calculateHorizontalPadding(elevation, radius, cardView.getPreventCornerOverlap()));
            int vPadding = (int) Math.ceil((double) RoundRectDrawableWithShadow.calculateVerticalPadding(elevation, radius, cardView.getPreventCornerOverlap()));
            cardView.setShadowPadding(hPadding, vPadding, hPadding, vPadding);
        }
    }

    public void onCompatPaddingChanged(CardViewDelegate cardView) {
        this.setMaxElevation(cardView, this.getMaxElevation(cardView));
    }

    public void onPreventCornerOverlapChanged(CardViewDelegate cardView) {
        this.setMaxElevation(cardView, this.getMaxElevation(cardView));
    }

    public void setBackgroundColor(CardViewDelegate cardView, int color) {
        ((RoundRectDrawable) ((RoundRectDrawable) cardView.getBackground())).setColor(color);
    }
}
