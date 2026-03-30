package com.souravmodak.modakflix;

import static android.net.Uri.parse;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.souravmodak.modakflix.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OnlinePlayerActivity extends AppCompatActivity implements View.OnClickListener, PlayerControlView.VisibilityListener, DownloadTracker.Listener {

    private static final int playerHeight = 250;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String KEY_AUTO_PLAY = "auto_play";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";

    private final Handler mHideHandler = new Handler(Looper.getMainLooper());
    
    private PlayerView playerView;
    private DataSource.Factory dataSourceFactory;
    private ExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean isShowingTrackSelectionDialog;
    private TextView tvPlaybackSpeed, tvPlaybackSpeedSymbol;
    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;
    private FrameLayout frameLayoutMain;
    private ImageView imgBwd, exoPlay, exoPause, imgFwd, imgBackPlayer;
    private TextView tvPlayerCurrentTime, tvPlayerEndTime;
    private ProgressBar exoProgressbar;
    private ImageView imgSetting, imgFullScreenEnterExit;

    private DownloadTracker downloadTracker;
    private DownloadManager downloadManager;
    private LinearLayout llDownloadVideo;
    private ImageView imgDownloadState;
    private TextView tvDownloadState;
    private ProgressBar progressBarPercentage;
    private String videoId, videoUrl, videoName;
    private long videoDurationInMilliSeconds;
    private Runnable runnableCode;
    private Handler handler;
    private String description, imageUrl;
    private boolean mVisible;
    private ProgressDialog pDialog;

    private ActivityResultLauncher<Intent> subtitlePickerLauncher;

    private static boolean isBehindLiveWindow(PlaybackException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) return true;
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        overridePendingTransition(R.anim.activity_slide_from_right, R.anim.nothing);

        dataSourceFactory = buildDataSourceFactory();
        hideStatusBar();
        setContentView(R.layout.activity_online_player);

        subtitlePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            addSubtitle(uri);
                        }
                    }
                }
        );

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            videoId = bundle.getString("video_id");
            videoName = bundle.getString("video_name");
            videoUrl = bundle.getString("video_url");
            videoDurationInMilliSeconds = bundle.getLong("video_duration");
            description = bundle.getString("description");
            imageUrl = bundle.getString("image_url");
            String posStr = bundle.getString("position");
            startPosition = posStr != null ? Long.parseLong(posStr) : C.TIME_UNSET;
        } else {
            startPosition = C.TIME_UNSET;
        }

        setDescription();

        if (savedInstanceState != null) {
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        } else {
            startAutoPlay = true;
            startWindow = C.INDEX_UNSET;
            // Keep startPosition from Intent if already set
        }

        ModakflixAdaptivePlayer application = (ModakflixAdaptivePlayer) getApplication();
        downloadTracker = application.getDownloadTracker();
        downloadManager = application.getDownloadManager();

        try {
            DownloadService.start(this, DemoDownloadService.class);
        } catch (Exception e) {
            DownloadService.startForeground(this, DemoDownloadService.class);
        }

        createView();
        prepareView();

        handler = new Handler(Looper.getMainLooper());
        runnableCode = new Runnable() {
            @Override
            public void run() {
                observerVideoStatus();
                handler.postDelayed(this, 500);
            }
        };
        handler.post(runnableCode);
    }

    private void setDescription() {
        TextView descriptionTV = findViewById(R.id.descriptionOnlineTv);
        if (descriptionTV != null) descriptionTV.setText(description);
        ImageView iv = findViewById(R.id.imageOnlineIv);
        if (iv != null) Glide.with(this).load(imageUrl).into(iv);
        TextView headingView = findViewById(R.id.showNameOnlinePlayer);
        if (headingView != null) headingView.setText(videoName);
        View subtitlesBtn = findViewById(R.id.subtitles);
        if (subtitlesBtn != null) {
            subtitlesBtn.setOnClickListener(view -> {
                if (player != null) player.pause();
                openSubtitleSelector();
            });
        }
    }

    private void observerVideoStatus() {
        if (videoUrl == null || videoUrl.isEmpty()) return;
        Download download = downloadTracker.downloads.get(parse(videoUrl));
        if (download != null) {
            runOnUiThread(() -> {
                switch (download.state) {
                    case Download.STATE_QUEUED: setCommonDownloadButton(ExoDownloadState.DOWNLOAD_QUEUE); break;
                    case Download.STATE_STOPPED: setCommonDownloadButton(ExoDownloadState.DOWNLOAD_RESUME); break;
                    case Download.STATE_DOWNLOADING:
                        setCommonDownloadButton(ExoDownloadState.DOWNLOAD_PAUSE);
                        if (download.getPercentDownloaded() != -1) {
                            progressBarPercentage.setVisibility(View.VISIBLE);
                            progressBarPercentage.setProgress((int) download.getPercentDownloaded());
                        }
                        break;
                    case Download.STATE_COMPLETED:
                        setCommonDownloadButton(ExoDownloadState.DOWNLOAD_COMPLETED);
                        progressBarPercentage.setVisibility(View.GONE);
                        break;
                    case Download.STATE_FAILED: setCommonDownloadButton(ExoDownloadState.DOWNLOAD_RETRY); break;
                }
            });
        } else {
            setCommonDownloadButton(ExoDownloadState.DOWNLOAD_START);
        }
    }

    protected void createView() {
        tvPlaybackSpeed = findViewById(R.id.tv_play_back_speed);
        tvPlaybackSpeedSymbol = findViewById(R.id.tv_play_back_speed_symbol);
        imgBwd = findViewById(R.id.img_bwd);
        imgFwd = findViewById(R.id.img_fwd);
        tvPlayerCurrentTime = findViewById(R.id.tv_player_current_time);
        tvPlayerEndTime = findViewById(R.id.tv_player_end_time);
        exoProgressbar = findViewById(R.id.loading_exoplayer);
        imgSetting = findViewById(R.id.img_setting);
        imgFullScreenEnterExit = findViewById(R.id.img_full_screen_enter_exit);
        playerView = findViewById(R.id.player_view);
        frameLayoutMain = findViewById(R.id.frame_layout_main);
        imgBackPlayer = findViewById(R.id.img_back_player);
        if (imgBackPlayer != null) imgBackPlayer.setOnClickListener(this);
        tvPlaybackSpeed.setOnClickListener(this);
        tvPlaybackSpeedSymbol.setOnClickListener(this);
        llDownloadVideo = findViewById(R.id.ll_download_video);
        imgDownloadState = findViewById(R.id.img_download_state);
        tvDownloadState = findViewById(R.id.tv_download_state);
        progressBarPercentage = findViewById(R.id.progress_horizontal_percentage);

        imgSetting.setOnClickListener(this);
        imgFullScreenEnterExit.setOnClickListener(this);
        llDownloadVideo.setOnClickListener(this);

        setProgress();
    }

    public void prepareView() {
        playerView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                ScreenUtils.convertDIPToPixels(this, playerHeight)));
    }

    @Override
    public void onStart() {
        super.onStart();
        downloadTracker.addListener(this);
        if (Util.SDK_INT > 23) initializePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) initializePlayer();
        FullScreencall();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        downloadTracker.removeListener(this);
        if (Util.SDK_INT > 23) releasePlayer();
    }

    private void initializePlayer() {
        if (player == null) {
            trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.getParameters().buildUpon().setForceHighestSupportedBitrate(true).build());
            
            RenderersFactory renderersFactory = ((ModakflixAdaptivePlayer) getApplication()).buildRenderersFactory(true);
            
            player = new ExoPlayer.Builder(this, renderersFactory)
                    .setTrackSelector(trackSelector)
                    .build();
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (exoProgressbar != null) exoProgressbar.setVisibility(state == Player.STATE_BUFFERING ? View.VISIBLE : View.GONE);
                }
                @Override
                public void onPlayerError(PlaybackException error) {
                    if (isBehindLiveWindow(error)) {
                        clearStartPosition();
                        initializePlayer();
                    }
                }
            });
            player.setPlayWhenReady(startAutoPlay);
            playerView.setPlayer(player);
            
            MediaSource mediaSource = buildMediaSource(parse(videoUrl));
            player.setMediaSource(mediaSource);
            
            if (startWindow != C.INDEX_UNSET) {
                player.seekTo(startWindow, startPosition);
            } else if (startPosition != C.TIME_UNSET) {
                player.seekTo(startPosition);
            }
            
            player.prepare();
        }
        initBwd();
        initFwd();
    }

    private MediaSource buildMediaSource(Uri uri) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.CONTENT_TYPE_DASH: return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_SS: return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_HLS: return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_OTHER: return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            default: throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            updateStartPosition();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentMediaItemIndex();
            startPosition = Math.max(0, player.getContentPosition());
            videoDurationInMilliSeconds = player.getDuration();
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private DataSource.Factory buildDataSourceFactory() {
        return ((ModakflixAdaptivePlayer) getApplication()).buildDataSourceFactory();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.img_setting) {
            if (!isShowingTrackSelectionDialog && TrackSelectionDialog.willHaveContent(trackSelector)) {
                isShowingTrackSelectionDialog = true;
                TrackSelectionDialog trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(trackSelector, dismissedDialog -> isShowingTrackSelectionDialog = false);
                trackSelectionDialog.show(getSupportFragmentManager(), null);
            }
        } else if (id == R.id.img_full_screen_enter_exit) {
            toggleFullscreen();
        } else if (id == R.id.tv_play_back_speed || id == R.id.tv_play_back_speed_symbol) {
            changePlaybackSpeed();
        } else if (id == R.id.img_back_player) {
            onBackPressed();
        } else if (id == R.id.ll_download_video) {
            exoVideoDownloadDecision((ExoDownloadState) llDownloadVideo.getTag());
        }
    }

    private void toggleFullscreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void changePlaybackSpeed() {
        float speed = 1.0f;
        String current = tvPlaybackSpeed.getText().toString();
        if (current.equals("1")) speed = 1.25f;
        else if (current.equals("1.25")) speed = 1.5f;
        else if (current.equals("1.5")) speed = 1.75f;
        else if (current.equals("1.75")) speed = 2.0f;
        
        if (player != null) player.setPlaybackParameters(new PlaybackParameters(speed));
        tvPlaybackSpeed.setText(speed == 1.0f ? "1" : String.valueOf(speed));
    }

    private void exoVideoDownloadDecision(ExoDownloadState state) {
        if (state == null) return;
        switch (state) {
            case DOWNLOAD_START: fetchDownloadOptions(); break;
            case DOWNLOAD_PAUSE: downloadManager.addDownload(downloadTracker.getDownloadRequest(parse(videoUrl)), Download.STATE_STOPPED); break;
            case DOWNLOAD_RESUME: downloadManager.addDownload(downloadTracker.getDownloadRequest(parse(videoUrl)), Download.STOP_REASON_NONE); break;
        }
    }

    private void fetchDownloadOptions() {
        pDialog = ProgressDialog.show(this, null, "Preparing Download Options...", true, false);
        DownloadHelper helper = DownloadHelper.forMediaItem(this, MediaItem.fromUri(videoUrl), new DefaultRenderersFactory(this), dataSourceFactory);
        helper.prepare(new DownloadHelper.Callback() {
            @Override
            public void onPrepared(DownloadHelper helper) {
                pDialog.dismiss();
                showDownloadOptionsDialog(helper);
            }
            @Override
            public void onPrepareError(DownloadHelper helper, IOException e) {
                pDialog.dismiss();
                Toast.makeText(OnlinePlayerActivity.this, "Download error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDownloadOptionsDialog(DownloadHelper helper) {
        List<Format> formats = new ArrayList<>();
        List<String> options = new ArrayList<>();
        for (int i = 0; i < helper.getPeriodCount(); i++) {
            TrackGroupArray groups = helper.getTrackGroups(i);
            for (int j = 0; j < groups.length; j++) {
                TrackGroup group = groups.get(j);
                for (int k = 0; k < group.length; k++) {
                    Format f = group.getFormat(k);
                    if (f.height != Format.NO_VALUE) {
                        formats.add(f);
                        options.add(f.height + "p");
                    }
                }
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Quality")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    DownloadRequest request = helper.getDownloadRequest(Util.getUtf8Bytes(videoName));
                    downloadManager.addDownload(request);
                })
                .show();
    }

    private void setProgress() {
        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    if (tvPlayerCurrentTime != null) tvPlayerCurrentTime.setText(stringForTime((int) player.getCurrentPosition()));
                    if (tvPlayerEndTime != null) tvPlayerEndTime.setText(stringForTime((int) player.getDuration()));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void initBwd() {
        if (imgBwd != null) imgBwd.setOnClickListener(v -> { if (player != null) player.seekTo(player.getCurrentPosition() - 10000); });
    }

    private void initFwd() {
        if (imgFwd != null) imgFwd.setOnClickListener(v -> { if (player != null) player.seekTo(player.getCurrentPosition() + 10000); });
    }

    private String stringForTime(int timeMs) {
        if (timeMs < 0) return "00:00";
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        else return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void FullScreencall() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onDownloadsChanged(Download download) {
        observerVideoStatus();
    }

    @Override
    public void onVisibilityChange(int visibility) {}

    public void setCommonDownloadButton(ExoDownloadState state) {
        if (llDownloadVideo != null) llDownloadVideo.setTag(state);
        if (tvDownloadState != null) tvDownloadState.setText(state.getValue());
        int resId = R.drawable.ic_download;
        switch (state) {
            case DOWNLOAD_COMPLETED: resId = R.drawable.ic_download_complete; break;
            case DOWNLOAD_PAUSE: resId = android.R.drawable.ic_media_pause; break;
            case DOWNLOAD_RESUME: resId = android.R.drawable.ic_media_play; break;
            case DOWNLOAD_QUEUE: resId = R.drawable.ic_queue; break;
        }
        if (imgDownloadState != null) imgDownloadState.setImageResource(resId);
    }

    private void openSubtitleSelector() {
        new AlertDialog.Builder(this)
                .setTitle("Subtitles")
                .setItems(new String[]{"Open from Storage"}, (dialog, which) -> chooseSubtitle())
                .show();
    }

    private void chooseSubtitle() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/x-subrip", "text/vtt", "text/plain", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        subtitlePickerLauncher.launch(intent);
    }

    private void addSubtitle(Uri uri) {
        if (player == null) return;
        long pos = player.getCurrentPosition();
        MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(uri)
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build();
        MediaItem mediaItem = MediaItem.fromUri(videoUrl).buildUpon()
                .setSubtitleConfigurations(Collections.singletonList(subtitle))
                .build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.seekTo(pos);
        player.play();
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();
        mVisible = false;
    }

    @Override
    public void onBackPressed() {
        finishResultAction();
        super.onBackPressed();
    }

    private void finishResultAction() {
        if (player != null) {
            long pos = player.getCurrentPosition();
            long dur = player.getDuration();
            Intent intent = new Intent();
            intent.setAction(Description.modakflixPlayerAction);
            intent.setData(Uri.parse(videoUrl));
            intent.putExtra("position", pos);
            intent.putExtra("duration", dur);
            setResult(RESULT_OK, intent);
        }
    }
}