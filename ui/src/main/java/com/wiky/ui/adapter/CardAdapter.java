package com.wiky.ui.adapter;

import android.support.v7.widget.CardView;
import android.view.View;

/**
 * Created by wiky on 6/14/15.
 */
public abstract class CardAdapter {


    public abstract ViewHolder createViewHolder(CardView cardView, int i);

    public abstract void showViewHolder(CardView cardView, ViewHolder viewHolder, int i);

    public abstract int size();

    public void onItemClick(CardView cardView, ViewHolder viewHolder, int i) {

    }

    public void onLongPress(CardView cardView, ViewHolder viewHolder, int i) {

    }


    public static abstract class ViewHolder {
        public View mView;

        public ViewHolder(View v) {
            mView = v;
        }
    }
}
