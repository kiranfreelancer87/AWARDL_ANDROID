package com.piddlepops.awardl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        ImageView iv = findViewById(R.id.ivSplash);
        Glide.with(this).load(R.drawable.loading).into(iv);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        }, 2800);
    }
}