package com.example.igorklimov.tictactoe.wifi;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.igorklimov.tictactoe.R;
import com.example.igorklimov.tictactoe.databinding.ActivityNoInternetBinding;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNoInternetBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_no_internet);

    }

    public void retry(View view) {
        if (Utils.isInternetAvailable(this)) {
            startActivity(new Intent(this, ConnectActivity.class));
        }
    }
}
