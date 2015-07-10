package com.wiky.customui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wiky.customui.cardpager.CardPagerActivity;
import com.wiky.customui.imageview.ImageViewerActivity;
import com.wiky.customui.pageview.PageViewActivity;
import com.wiky.customui.widgets.WidgetActivity;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<MenuAdapter.MenuItem> data = new ArrayList<>();
        data.add(new MenuAdapter.MenuItem("上滑翻页 - PageView", 0));
        data.add(new MenuAdapter.MenuItem("缩放的图片 - ImageViewer", 1));
        data.add(new MenuAdapter.MenuItem("CardPager", 2));
        data.add(new MenuAdapter.MenuItem("各种小控件", 3));

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(new MenuAdapter(this, data));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuAdapter.MenuItem item = (MenuAdapter.MenuItem) parent.getItemAtPosition(position);
        Intent intent = null;
        if (item.mId == 0) {
            intent = new Intent(this, PageViewActivity.class);
        } else if (item.mId == 1) {
            intent = new Intent(this, ImageViewerActivity.class);
        } else if (item.mId == 2) {
            intent = new Intent(this, CardPagerActivity.class);
        } else if (item.mId == 3) {
            intent = new Intent(this, WidgetActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.from_right, R.anim.to_left);
    }
}
