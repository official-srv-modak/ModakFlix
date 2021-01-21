package com.example.modakflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        setContentView(R.layout.activity_splash_screen);

        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, Profiles.class);
                startActivity(intent);
                finish();
            }
        }, 5000);


        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_out);
        ImageView logo = findViewById(R.id.logo);
        logo.setAnimation(slideIn);

        TextView tv = findViewById(R.id.tag);
        Animation slideLt = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        tv.setAnimation(slideLt);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.tag);
                Animation slideRt = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.slide_down);
                tv.setAnimation(slideRt);
            }
        }, 3500);

    }
}