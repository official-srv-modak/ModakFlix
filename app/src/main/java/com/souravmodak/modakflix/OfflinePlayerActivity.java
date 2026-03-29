package com.souravmodak.modakflix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.video.VideoSize;
import com.souravmodak.modakflix.R;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Formatter;
import java.util.Locale;

public class OfflinePlayerActivity extends AppCompatActivity implements View.OnClickListener, VideoRendererEventListener, PlayerControlView.VisibilityListener {

    private static final String TAG = "OfflinePlayer";
    protected static CookieManager DEFAULT_COOKIE_MANAGER;
    private ImageView imgBackPlayer;
    private ImageView imgBwd;
    private ImageView exoPlay;
    private ImageView exoPause;
    private ImageView imgFwd;
    private LinearLayout linMediaController;
    private TextView tvPlayerCurrentTime;
    private ProgressBar exoProgressbar;
    private TextView tvPlayerEndTime;
    private TextView tvPlaybackSpeed;
    private TextView tvPlayBackSpeedSymbol;
    private ImageView imgFullScreenEnterExit;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private FrameLayout frameLayoutMain;
    int tapCount = 1;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private Handler handler;
    private DataSource.Factory dataSourceFactory;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    String offlineVideoLink, title;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_offline_player);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString("video_title");
            offlineVideoLink = bundle.getString("video_url");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        dataSourceFactory = buildDataSourceFactory();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        FullScreencall();
        initView();
        prepareView();
        setOnClickListner();
    }

    private void initView() {
        playerView = findViewById(R.id.player_view);
        frameLayoutMain = findViewById(R.id.frame_layout_main);
        imgBwd = findViewById(R.id.img_bwd);
        exoPlay = findViewById(R.id.exo_play);
        exoPause = findViewById(R.id.exo_pause);
        imgBackPlayer = findViewById(R.id.img_back_player);
        imgBackPlayer.setOnClickListener(this);
        imgFwd = findViewById(R.id.img_fwd);
        linMediaController = findViewById(R.id.lin_media_controller);
        tvPlayerCurrentTime = findViewById(R.id.tv_player_current_time);
        exoProgressbar = findViewById(R.id.loading_exoplayer);
        tvPlayerEndTime = findViewById(R.id.tv_player_end_time);
        tvPlaybackSpeed = findViewById(R.id.tv_play_back_speed);
        tvPlaybackSpeed.setOnClickListener(this);
        tvPlaybackSpeed.setText("" + tapCount);
        tvPlayBackSpeedSymbol = findViewById(R.id.tv_play_back_speed_symbol);
        imgFullScreenEnterExit = findViewById(R.id.img_full_screen_enter_exit);

        tvPlayBackSpeedSymbol.setOnClickListener(this);
    }

    public void prepareView() {
        setProgress();
    }

    private void initExoplayer() {
        AdaptiveTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, videoTrackSelectionFactory);

        DefaultAllocator defaultAllocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        DefaultLoadControl defaultLoadControl = new DefaultLoadControl.Builder()
                .setAllocator(defaultAllocator)
                .build();

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(defaultLoadControl)
                .build();

        playerView.setUseController(true);
        playerView.requestFocus();
        playerView.setPlayer(exoPlayer);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.setPlayWhenReady(true);

        DownloadRequest downloadRequest = ModakflixAdaptivePlayer.getInstance().getDownloadTracker().getDownloadRequest(Uri.parse(offlineVideoLink));
        MediaSource mediaSource;
        if (downloadRequest != null) {
            mediaSource = DownloadHelper.createMediaSource(downloadRequest, dataSourceFactory);
        } else {
            mediaSource = buildMediaSource(Uri.parse(offlineVideoLink));
        }

        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        exoProgressbar.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_READY:
                        exoProgressbar.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Player error", error);
                exoPlayer.prepare();
                exoPlayer.play();
            }
        });

        initBwd();
        initFwd();
    }

    private void setProgress() {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null) {
                    tvPlayerCurrentTime.setText(stringForTime((int) exoPlayer.getCurrentPosition()));
                    tvPlayerEndTime.setText(stringForTime((int) exoPlayer.getDuration()));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void initBwd() {
        imgBwd.requestFocus();
        imgBwd.setOnClickListener(v -> {
            if (exoPlayer != null) {
                exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 10000);
            }
        });
    }

    private void initFwd() {
        imgFwd.requestFocus();
        imgFwd.setOnClickListener(v -> {
            if (exoPlayer != null) {
                exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 10000);
            }
        });
    }

    private void setOnClickListner() {
        imgFullScreenEnterExit.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_full_screen_enter_exit) {
            Display display = getWindowManager().getDefaultDisplay();
            int rotation = display.getRotation();

            if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                frameLayoutMain.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 600));
                // Use default icons if exoplayer ones are missing
                imgFullScreenEnterExit.setImageResource(android.R.drawable.ic_menu_zoom); 
                hide();
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                frameLayoutMain.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                imgFullScreenEnterExit.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                hide();
            }
        } else if (view.getId() == R.id.tv_play_back_speed || view.getId() == R.id.tv_play_back_speed_symbol) {
            float speed = 1.0f;
            if (tvPlaybackSpeed.getText().equals("1")) speed = 1.25f;
            else if (tvPlaybackSpeed.getText().equals("1.25")) speed = 1.5f;
            else if (tvPlaybackSpeed.getText().equals("1.5")) speed = 1.75f;
            else if (tvPlaybackSpeed.getText().equals("1.75")) speed = 2.0f;
            
            if (exoPlayer != null) {
                exoPlayer.setPlaybackParameters(new PlaybackParameters(speed));
            }
            tvPlaybackSpeed.setText(speed == 1.0f ? "1" : String.valueOf(speed));
        } else if (view.getId() == R.id.img_back_player) {
            onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initExoplayer();
            if (playerView != null) playerView.onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            initExoplayer();
            if (playerView != null) playerView.onResume();
        }
        FullScreencall();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) playerView.onPause();
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (playerView != null) playerView.onPause();
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    public void finishActivity() {
        finish();
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void FullScreencall() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();
        mVisible = false;
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private String stringForTime(int timeMs) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        else return mFormatter.format("%02d:%02d", minutes, seconds).toString();
    }

    @Override
    public void onVisibilityChange(int visibility) {}

    private DataSource.Factory buildDataSourceFactory() {
        return ((ModakflixAdaptivePlayer) getApplication()).buildDataSourceFactory();
    }

    private MediaSource buildMediaSource(Uri uri) {
        @C.ContentType int type = Util.inferContentType(uri);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        switch (type) {
            case C.CONTENT_TYPE_DASH: return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_SS: return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_HLS: return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_OTHER: return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            default: throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    @Override public void onVideoEnabled(DecoderCounters counters) {}
    @Override public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {}
    @Override public void onVideoInputFormatChanged(Format format) {}
    @Override public void onVideoInputFormatChanged(Format format, @Nullable com.google.android.exoplayer2.decoder.DecoderReuseEvaluation decoderReuseEvaluation) {}
    @Override public void onDroppedFrames(int count, long elapsedMs) {}
    @Override public void onVideoSizeChanged(VideoSize videoSize) {}
    @Override public void onRenderedFirstFrame(Object surface, long renderTimeMs) {}
    @Override public void onVideoDisabled(DecoderCounters counters) {}
}