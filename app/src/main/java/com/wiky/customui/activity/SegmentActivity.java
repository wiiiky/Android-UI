package com.wiky.customui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.wiky.customui.R;
import com.wiky.ui.layout.SegmentPager;

import java.util.ArrayList;
import java.util.List;

public class SegmentActivity extends AppCompatActivity {

    private SegmentPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment);

        mPager = (SegmentPager) findViewById(R.id.segment_pager);

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.GRAY);
        colors.add(Color.YELLOW);
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        mPager.setAdapter(new SegmentAdapter(colors));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }

    private class SegmentAdapter extends RecyclerView.Adapter {

        private List<Integer> mColors;

        public SegmentAdapter(List<Integer> colors) {
            mColors = colors;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = new View(parent.getContext());
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.itemView.setBackgroundColor(mColors.get(position));
        }

        @Override
        public int getItemCount() {
            return mColors.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
