package com.wiky.ui.cardview;

import android.graphics.drawable.Drawable;

interface CardViewDelegate {
    void setBackgroundDrawable(Drawable var1);

    Drawable getBackground();

    boolean getUseCompatPadding();

    boolean getPreventCornerOverlap();

    float getRadius();

    void setShadowPadding(int var1, int var2, int var3, int var4);
}
