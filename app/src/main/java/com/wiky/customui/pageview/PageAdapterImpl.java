package com.wiky.customui.pageview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wiky.customui.R;
import com.wiky.ui.adapter.PageAdapter;

import java.util.List;

/**
 * Created by wiky on 4/2/15.
 */
public class PageAdapterImpl extends PageAdapter {

    private List<PageItem> mList;
    private Context mContext;

    public PageAdapterImpl(Context context, List<PageItem> list) {
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
        if (convertView == null) {
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.page_template, null);
        }
        PageItem item = (PageItem) getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image_view);
        imageView.setImageResource(item.mImageId);
        TextView textView = (TextView) convertView.findViewById(R.id.text_view);
        textView.setText(item.mText);
        textView.setBackgroundColor(Color.parseColor(item.mColor));
        return convertView;
    }

    public static class PageItem {
        public int mImageId;
        public String mText;
        public String mColor;

        public PageItem(int id, String text, String color) {
            mImageId = id;
            mText = text;
            mColor = color;
        }
    }
}
