package com.wiky.customui.cardpager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wiky.customui.R;
import com.wiky.ui.adapter.CardAdapter;
import com.wiky.ui.layout.CardPager;

public class CardPagerActivity extends ActionBarActivity {

    private CardPager mCardPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_pager);

        mCardPager = (CardPager) findViewById(R.id.cardPager);
        CardAdapter adapter = new CardAdapter() {
            @Override
            public CardAdapter.ViewHolder createViewHolder(CardView cardView, int i) {
                View v = LayoutInflater.from(CardPagerActivity.this).inflate(R.layout.item_card_pager, cardView, false);
                v.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return new ViewHolder(v);
            }

            @Override
            public void showViewHolder(CardView cardView, CardAdapter.ViewHolder viewHolder, int i) {
                ViewHolder holder = (ViewHolder) viewHolder;
                holder.mTv.setText(String.valueOf(i) + " Index");
            }

            @Override
            public void onItemClick(CardView cardView, CardAdapter.ViewHolder viewHolder, int i) {
                Toast.makeText(CardPagerActivity.this, "single tap", Toast.LENGTH_LONG).show();
            }


            @Override
            public int size() {
                return 4;
            }

            class ViewHolder extends CardAdapter.ViewHolder {

                public TextView mTv;

                public ViewHolder(View v) {
                    super(v);
                    mTv = (TextView) v.findViewById(R.id.tvHello);
                }
            }
        };
        mCardPager.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }
}
