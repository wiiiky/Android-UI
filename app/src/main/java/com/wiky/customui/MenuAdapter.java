package com.wiky.customui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wiky on 4/2/15.
 */
public class MenuAdapter extends BaseAdapter {
    private List<MenuItem> mList;
    private Context mContext;

    public MenuAdapter(Context context, List<MenuItem> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuItem item = (MenuItem) getItem(position);
        TextView tv = (TextView) convertView;
        if (tv == null) {
            tv = new TextView(mContext);
            tv.setTextSize(30);
        }
        tv.setText(item.mText);
        return tv;
    }

    public static class MenuItem {
        public String mText;
        public int mId;

        public MenuItem(String text, int i) {
            mText = text;
            mId = i;
        }
    }
}
