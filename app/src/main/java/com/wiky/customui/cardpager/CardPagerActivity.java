package com.wiky.customui.cardpager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
                //v.setBackgroundColor(Color.BLUE);
                return new ViewHolder(v);
            }

            @Override
            public void showViewHolder(CardView cardView, CardAdapter.ViewHolder viewHolder, int i) {
                if (i == 0) {
                    viewHolder.mView.setBackgroundColor(Color.GREEN);
                } else if (i == 1) {
                    viewHolder.mView.setBackgroundColor(Color.RED);
                } else {
                    viewHolder.mView.setBackgroundColor(Color.BLUE);
                }
            }


            @Override
            public int size() {
                return 4;
            }

            class ViewHolder extends CardAdapter.ViewHolder {

                public ViewHolder(View v) {
                    super(v);
                }
            }
        };
        mCardPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_pager, menu);
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
}
