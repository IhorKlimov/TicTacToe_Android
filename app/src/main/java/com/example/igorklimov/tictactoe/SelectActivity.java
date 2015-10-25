package com.example.igorklimov.tictactoe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SelectActivity extends AppCompatActivity {
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        image = (ImageView) findViewById(R.id.image);
        TextView x = (TextView) findViewById(R.id.x);
        TextView o = (TextView) findViewById(R.id.o);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.x:
                        MainActivity.playersChar = MainActivity.Side.X;
                        MainActivity.opponentChar = MainActivity.Side.O;
                        break;
                    case R.id.o:
                        MainActivity.playersChar = MainActivity.Side.O;
                        MainActivity.opponentChar = MainActivity.Side.X;
                        break;
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/MakeOut.ttf");
        x.setTypeface(type);
        o.setTypeface(type);
        x.setOnClickListener(listener);
        o.setOnClickListener(listener);
    }
}
