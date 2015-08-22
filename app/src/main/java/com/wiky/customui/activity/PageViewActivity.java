package com.wiky.customui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.wiky.customui.R;
import com.wiky.ui.layout.PageView;

import java.util.ArrayList;


public class PageViewActivity extends ActionBarActivity {

    private PageView mPageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);

        mPageView = (PageView) findViewById(R.id.page_view);

        ArrayList<PageAdapterImpl.PageItem> list = new ArrayList<>();
        list.add(new PageAdapterImpl.PageItem(R.drawable.pikaqiu, "hello world", "#ff0000"));
        list.add(new PageAdapterImpl.PageItem(R.drawable.xidada, "asjdfajsdfjalkadfjadfakdfkas;df", "#00ff00"));
        list.add(new PageAdapterImpl.PageItem(R.drawable.nsapassword, "hello Everyone", "#0000ff"));
        list.add(new PageAdapterImpl.PageItem(R.drawable.b823kajsdfe, "I don't understand why people believe in so much thing that is ridiculous", "#ff00ff"));
        list.add(new PageAdapterImpl.PageItem(R.drawable.pikaqiu, "Holy that", "#00ffff"));
        mPageView.setAdapter(new PageAdapterImpl(this, list));

        mPageView.setOnPageChangeListener(new PageView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int pos) {
                Toast.makeText(PageViewActivity.this, "now you are in " + pos, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNextCancelled(int pos) {

            }

            @Override
            public void onPrevCancelled(int pos) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }
}
