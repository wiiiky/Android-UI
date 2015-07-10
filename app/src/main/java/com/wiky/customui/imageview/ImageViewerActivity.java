package com.wiky.customui.imageview;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import com.wiky.customui.R;

public class ImageViewerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scalable_image_view, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }
}
