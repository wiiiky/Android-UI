package com.wiky.customui.slidedrawer;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wiky.customui.R;

public class SlideDrawerActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout mContent;
    RelativeLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_drawer);

        mContent= (RelativeLayout) findViewById(R.id.sd_content);
        mContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("content", event.getAction()+"");
                return true;
            }
        });
        mDrawer= (RelativeLayout) findViewById(R.id.sd_drawer);
        mDrawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.tv_a).setOnClickListener(this);
        findViewById(R.id.tv_b).setOnClickListener(this);
        findViewById(R.id.tv_c).setOnClickListener(this);
        findViewById(R.id.tv_d).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }

    @Override
    public void onClick(View v) {
        TextView tv= (TextView) v;
        if(tv.getCurrentTextColor()== Color.WHITE){
            tv.setTextColor(Color.BLUE);
        }else{
            tv.setTextColor(Color.WHITE);
        }
    }
}
