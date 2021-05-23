package com.example.modakflix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;


public class ModakFlixPlayer extends AppCompatActivity{

    SimpleExoPlayer player;
    PlayerView playerView;
    ImageView fullscreenButton;
    boolean fullscreen = false;
    Runnable vanish;
    Handler vanishHandle;
    private HttpProxyCacheServer proxyCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modak_flix_player);

        String videoUrl = getIntent().getStringExtra("url");
        int seekTo = getIntent().getIntExtra("resume_pos", 0);
       // Long seekTo = Long.parseLong(seekToStr);


        TrackSelector trackSelector = new DefaultTrackSelector();

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(32*1024, 64*1024, 1024, 1024).createDefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        playerView = findViewById(R.id.exoPlayer);

        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);

        fullscreenButton.setVisibility(View.INVISIBLE);

         vanish = new Runnable() {
            @Override
            public void run() {

                // hide your button here
                fullscreenButton.setVisibility(View.INVISIBLE);
            }
        };

        fullscreenButton.setVisibility(View.VISIBLE);
        vanishHandle = new Handler();
        vanishHandle.postDelayed(vanish, playerView.getControllerShowTimeoutMs());

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickToggleControlVisible();
            }
        });

        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fullscreen) {
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(ModakFlixPlayer.this, R.drawable.exo_controls_fullscreen_enter));

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                    if(getSupportActionBar() != null){
                        getSupportActionBar().show();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) ( 200 * getApplicationContext().getResources().getDisplayMetrics().density);
                    playerView.setLayoutParams(params);

                    fullscreen = false;
                }else{
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(ModakFlixPlayer.this, R.drawable.exo_controls_fullscreen_exit));

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                    if(getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    playerView.setLayoutParams(params);

                    fullscreen = true;
                }
            }
        });

        playerView.setPlayer(player);

        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),Util.getUserAgent(getApplicationContext(),getApplicationContext().getString(R.string.app_name)));

        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(videoUrl));

        player.prepare(videoSource);
        player.setPlayWhenReady(true);
        player.seekTo(seekTo);

    }

    private void onClickToggleControlVisible()
    {
        if(!playerView.isControllerVisible())
        {
            fullscreenButton.setVisibility(View.VISIBLE);
            vanishHandle.postDelayed(vanish, playerView.getControllerShowTimeoutMs());
        }
        else
        {
            fullscreenButton.setVisibility(View.INVISIBLE);
            vanishHandle.removeCallbacks(vanish);
        }

    }
    private void onVideoClosed()
    {
        player.release();
        player.setPlayWhenReady(false);
        Intent playerDataIntent = new Intent();
        playerDataIntent.putExtra("modakflix_player_current_pos", player.getContentPosition());
        setResult(RESULT_OK, playerDataIntent);
        super.onActivityResult(1, RESULT_OK, playerDataIntent);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        onVideoClosed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onVideoClosed();
        if (proxyCache != null) {
            proxyCache.shutdown();
        }
    }

    public static Intent makeIntent(Context context)
    {
        return new Intent(context, ModakFlixPlayer.class);
    }
}