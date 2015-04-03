package com.wiky.customui.pageview;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
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
        list.add(new PageAdapterImpl.PageItem(R.drawable.pikaqiu, "hello Everyone", "#0000ff"));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_page_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }
}
