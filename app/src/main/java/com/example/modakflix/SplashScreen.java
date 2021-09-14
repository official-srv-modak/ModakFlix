package com.example.modakflix;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    ImageView logo;
    SharedPreferences prefs;
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

        setRandomnAnimation();
    }

    void setRandomnAnimation()
    {
        int max = 10, min = 1;
        int randomNumber = (int)(Math.random()*(max-min+1)+min);

        prefs = getSharedPreferences("com.example.modakflix", MODE_PRIVATE);
        if(randomNumber == 5 || randomNumber == 7 || prefs.getBoolean("firstrun", true))
            startAnimation();
        else
            noAnimation();
    }

    void noAnimation()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, Profiles.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    void startAnimation()
    {
        prefs.edit().putBoolean("firstrun", false).commit();
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
    boolean hasAnimationStarted;

    /*public void onWindowFocusChanged(boolean hasFocus) {
        logo = findViewById(R.id.logo);
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !hasAnimationStarted) {
            hasAnimationStarted=true;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            ObjectAnimator translationY = ObjectAnimator.ofFloat(logo, "y", metrics.heightPixels / 2 - (metrics.heightPixels / 2) - (int)(0.19*(metrics.heightPixels / 2))); // metrics.heightPixels or root.getHeight()
            translationY.setDuration(1700);
            translationY.setStartDelay(3500);
            translationY.start();
        }
    }*/
}