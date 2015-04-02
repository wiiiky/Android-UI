package com.wiky.customui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wiky.customui.pageview.PageViewActivity;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<MenuAdapter.MenuItem> data = new ArrayList<>();
        data.add(new MenuAdapter.MenuItem("上滑翻页 - PageView", 0));

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(new MenuAdapter(this, data));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuAdapter.MenuItem item = (MenuAdapter.MenuItem) parent.getItemAtPosition(position);
        if (item.mId == 0) {
            Intent intent = new Intent(this, PageViewActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.from_right, R.anim.to_left);
        }
    }
}
