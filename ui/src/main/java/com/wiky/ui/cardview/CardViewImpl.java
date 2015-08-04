package com.wiky.ui.cardview;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface CardViewImpl {
    void initialize(CardViewDelegate var1, Context var2, int var3, float var4, float var5, float var6);

    void setRadius(CardViewDelegate var1, float var2);

    float getRadius(CardViewDelegate var1);

    void setElevation(CardViewDelegate var1, float var2);

    float getElevation(CardViewDelegate var1);

    void initStatic();

    void setMaxElevation(CardViewDelegate var1, float var2);

    float getMaxElevation(CardViewDelegate var1);

    float getMinWidth(CardViewDelegate var1);

    float getMinHeight(CardViewDelegate var1);

    void updatePadding(CardViewDelegate var1);

    void onCompatPaddingChanged(CardViewDelegate var1);

    void onPreventCornerOverlapChanged(CardViewDelegate var1);

    void setBackgroundColor(CardViewDelegate var1, int var2);

    Drawable createBackground(Context context, int backgroundColor, float radius, float elevation, float maxElevation);

}
