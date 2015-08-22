package com.wiky.customui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wiky.customui.R;
import com.wiky.ui.layout.CardStack;

public class CardStackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_stack);

        CardStack cardStack= (CardStack) findViewById(R.id.card_stack);
        cardStack.setAdapter(new Adapter());
    }

    private class Adapter extends CardStack.CardStackAdapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view=new TextView(parent.getContext());
            view.setTextSize(60);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder= (ViewHolder) holder;
            TextView tv= (TextView) viewHolder.itemView;
            tv.setText(""+position);
        }

        @Override
        public int getItemCount() {
            return 5;
        }

        private class ViewHolder extends CardStack.ViewHolder{

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
