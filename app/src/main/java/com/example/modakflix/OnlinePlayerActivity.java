package com.example.modakflix;

import static android.net.Uri.parse;
import static android.net.wifi.WifiConfiguration.Status.strings;
import static com.example.modakflix.Description.modakflixPlayerAction;
import static com.google.android.exoplayer2.offline.Download.STATE_COMPLETED;
import static com.google.android.exoplayer2.offline.Download.STATE_DOWNLOADING;
import static com.google.android.exoplayer2.offline.Download.STATE_FAILED;
import static com.google.android.exoplayer2.offline.Download.STATE_QUEUED;
import static com.google.android.exoplayer2.offline.Download.STATE_REMOVING;
import static com.google.android.exoplayer2.offline.Download.STATE_RESTARTING;
import static com.google.android.exoplayer2.offline.Download.STATE_STOPPED;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import Opensubs.OpenSubtitle;
import Opensubs.SubtitleInfo;


public class OnlinePlayerActivity extends AppCompatActivity implements View.OnClickListener, PlaybackPreparer, PlayerControlView.VisibilityListener, DownloadTracker.Listener {

    private static final int playerHeight = 250;
    ProgressDialog pDialog;
    protected static final CookieManager DEFAULT_COOKIE_MANAGER;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
    private static final int UI_ANIMATION_DELAY = 300;
    static Uri subtitleUri = parse("");
    // Saved instance state keys.
    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private static int REQEST_CODE = 0;
    boolean closeFlag = true;
    boolean finishFlag = false;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private final Handler mHideHandler = new Handler();
    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }

        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    int tapCount = 1;
    LinearLayout llParentContainer;
    Boolean isScreenLandscape = false;
    List<TrackKey> trackKeys = new ArrayList<>();
    List<String> optionsToDownload = new ArrayList<String>();
//    TrackKey trackKeyDownload;
DefaultTrackSelector.Parameters qualityParams;
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private PlayerView playerView;
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer player;
    private MediaSource mediaSource;
    private DefaultTrackSelector trackSelector;
    private boolean isShowingTrackSelectionDialog;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;
    private TextView tvPlaybackSpeed, tvPlaybackSpeedSymbol;
    private boolean startAutoPlay;
    private int startWindow;
    // Fields used only for ad playback. The ads loader is loaded via reflection.
    private long startPosition;
    private AdsLoader adsLoader;
    private Uri loadedAdTagUri;
    private FrameLayout frameLayoutMain;
    private ImageView imgBwd;
    private ImageView exoPlay;
    private ImageView exoPause;
    private ImageView imgFwd,imgBackPlayer;
    private TextView tvPlayerCurrentTime;
    private DefaultTimeBar exoTimebar;
    private ProgressBar exoProgressbar;
    private TextView tvPlayerEndTime;
    private ImageView imgSetting;
    private ImageView imgFullScreenEnterExit;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private DownloadTracker downloadTracker;
    private DownloadManager downloadManager;
    private DownloadHelper myDownloadHelper;
    private LinearLayout llDownloadContainer;
    private LinearLayout llDownloadVideo;
    private ImageView imgDownloadState;
    private TextView tvDownloadState;
    private ProgressBar progressBarPercentage;
    private String videoId,videoUrl,videoName;
    private long videoDurationInSeconds, videoDurationInMilliSeconds;
    private Runnable runnableCode;
    private Handler handler;
    private String description, imageUrl;


    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        overridePendingTransition(R.anim.activity_slide_from_right, R.anim.nothing);

        dataSourceFactory = buildDataSourceFactory();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        hideStatusBar();

        setContentView(R.layout.activity_online_player);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
           
            videoId = bundle.getString("video_id");
            videoName = bundle.getString("video_name");
            videoUrl = bundle.getString("video_url");
            videoDurationInMilliSeconds = bundle.getLong("video_duration");
            videoDurationInSeconds = videoDurationInMilliSeconds/1000;
            description = bundle.getString("description");
            imageUrl = bundle.getString("image_url");
            startPosition = Long.parseLong(bundle.getString("position"));
        }

        setDescription();

        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        } else {
            updateTrackSelectorParameters();


            trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();

            clearStartPosition();
        }

        ModakflixAdaptivePlayer application = (ModakflixAdaptivePlayer) getApplication();
        downloadTracker = application.getDownloadTracker();
        downloadManager = application.getDownloadManager();


        // Start the download service if it should be running but it's not currently.
        // Starting the service in the foreground causes notification flicker if there is no scheduled
        // action. Starting it in the background throws an exception if the app is in the background too
        // (e.g. if device screen is locked).

        try {
            DownloadService.start(this, DemoDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, DemoDownloadService.class);
        }

//        Requirements requirements = new Requirements(Requirements.NETWORK_UNMETERED);
//        DownloadService.sendSetRequirements(
//                OnlinePlayerActivity.this,
//                DemoDownloadService.class,
//                requirements,
//                /* foreground= */ false);


        createView();
        prepareView();


        runnableCode = new Runnable() {
            @Override
            public void run() {
                observerVideoStatus();
                handler.postDelayed(this, 100);
            }
        };

        handler.post(runnableCode);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setDescription() {
        TextView descriptionTV = findViewById(R.id.descriptionOnlineTv);
        descriptionTV.setText(description);

        ImageView iv = findViewById(R.id.imageOnlineIv);
        Glide.with(this).load(imageUrl).into(iv);
        /*LoadImageTask lit = new LoadImageTask(iv);
        lit.execute(imageUrl);*/

        TextView headingView = findViewById(R.id.showNameOnlinePlayer);
        headingView.setText(videoName);

        Button subs = findViewById(R.id.subtitles);
        subs.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {

              //  while(!PermissionClass.permissionCompletedFlag);
                player.setPlayWhenReady(false);
                openSubtitleSelector();
            }
        });
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    private void observerVideoStatus() {
                                      if (downloadManager.getCurrentDownloads().size() > 0) {
                                          for (int i = 0; i < downloadManager.getCurrentDownloads().size(); i++) {
                                              Download currentDownload = downloadManager.getCurrentDownloads().get(i);
                                              if (!videoUrl.isEmpty() && currentDownload.request.uri.equals(parse(videoUrl))) {
                                                  runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          if (downloadTracker.downloads.size() > 0) {
                                                              if (currentDownload.request.uri.equals(parse(videoUrl))) {

                                                                  Download downloadFromTracker = downloadTracker.downloads.get(parse(videoUrl));
                                                                  if (downloadFromTracker != null) {
                                                                      switch (downloadFromTracker.state) {
                                                                          case STATE_QUEUED:
                                                                              setCommonDownloadButton(ExoDownloadState.DOWNLOAD_QUEUE);
                                                                              break;

                                                                          case STATE_STOPPED:
                                                                              setCommonDownloadButton(ExoDownloadState.DOWNLOAD_RESUME);
                                                                              break;

                                                                          case STATE_DOWNLOADING:

                                                                              setCommonDownloadButton(ExoDownloadState.DOWNLOAD_PAUSE);

                                                                              if(downloadFromTracker.getPercentDownloaded() != -1){
                                                                                  progressBarPercentage.setVisibility(View.VISIBLE);
                                                                                  progressBarPercentage.setProgress(Integer.parseInt(AppUtil.floatToPercentage(downloadFromTracker.getPercentDownloaded()).replace("%","")));
//                                                                                  tvProgressPercentage.setText(AppUtil.floatToPercentage(downloadFromTracker.getPercentDownloaded()));
                                                                              }


                                                                              Log.d("EXO STATE_DOWNLOADING ", +downloadFromTracker.getBytesDownloaded() + " " + downloadFromTracker.contentLength);
                                                                              Log.d("EXO  STATE_DOWNLOADING ", "" + downloadFromTracker.getPercentDownloaded());


                                                                              break;
                                                                          case STATE_COMPLETED:


                                                                              setCommonDownloadButton(ExoDownloadState.DOWNLOAD_COMPLETED);
                                                                              progressBarPercentage.setVisibility(View.GONE);


                                                                              Log.d("EXO STATE_COMPLETED ", +downloadFromTracker.getBytesDownloaded() + " " + downloadFromTracker.contentLength);
                                                                              Log.d("EXO  STATE_COMPLETED ", "" + downloadFromTracker.getPercentDownloaded());

                                                                              progressBarPercentage.setVisibility(View.GONE);


                                                                              break;

                                                                          case STATE_FAILED:
                                                                              setCommonDownloadButton(ExoDownloadState.DOWNLOAD_RETRY);


                                                                              break;

                                                                          case STATE_REMOVING:


                                                                              break;

                                                                          case STATE_RESTARTING:


                                                                              break;
                                                                      }
                                                                  }
                                                              }

                                                          }
                                                      }
                                                  });
                                              }
                                          }
                                      }

    }



    protected void createView() {
        handler = new Handler();
        tvPlaybackSpeed = findViewById(R.id.tv_play_back_speed);
        tvPlaybackSpeed.setOnClickListener(this);
        tvPlaybackSpeed.setText("" + tapCount);
        tvPlaybackSpeedSymbol = findViewById(R.id.tv_play_back_speed_symbol);
        tvPlaybackSpeedSymbol.setOnClickListener(this);
        imgBwd = findViewById(R.id.img_bwd);
        exoPlay = findViewById(R.id.exo_play);
        exoPause = findViewById(R.id.exo_pause);
        imgFwd = findViewById(R.id.img_fwd);
        tvPlayerCurrentTime = findViewById(R.id.tv_player_current_time);
        exoTimebar = findViewById(R.id.exo_progress);
        exoProgressbar = findViewById(R.id.loading_exoplayer);
        tvPlayerEndTime = findViewById(R.id.tv_player_end_time);
        imgSetting = findViewById(R.id.img_setting);
        imgFullScreenEnterExit = findViewById(R.id.img_full_screen_enter_exit);
        imgFullScreenEnterExit.setOnClickListener(this);
        imgBackPlayer = findViewById(R.id.img_back_player);
        playerView = findViewById(R.id.player_view);
        imgSetting.setOnClickListener(this);
        playerView.setControllerVisibilityListener(this);
        playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
        playerView.requestFocus();
        llParentContainer = (LinearLayout) findViewById(R.id.ll_parent_container);
        frameLayoutMain = findViewById(R.id.frame_layout_main);
        findViewById(R.id.img_back_player).setOnClickListener(this);
        progressBarPercentage = findViewById(R.id.progress_horizontal_percentage);
        progressBarPercentage.setVisibility(View.GONE);
        llDownloadContainer = (LinearLayout) findViewById(R.id.ll_download_container);
        llDownloadVideo = (LinearLayout) findViewById(R.id.ll_download_video);
        imgDownloadState = (ImageView) findViewById(R.id.img_download_state);
        tvDownloadState = (TextView) findViewById(R.id.tv_download_state);
        llDownloadVideo.setOnClickListener(this);


        setProgress();
    }


    public void prepareView() {
        playerView.setLayoutParams(
                new PlayerView.LayoutParams(
                        // or ViewGroup.LayoutParams.WRAP_CONTENT
                        PlayerView.LayoutParams.MATCH_PARENT,
                        // or ViewGroup.LayoutParams.WRAP_CONTENT,
                        ScreenUtils.convertDIPToPixels(OnlinePlayerActivity.this, playerHeight)));


        frameLayoutMain.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));



    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        releasePlayer();
        clearStartPosition();
        setIntent(intent);

        Bundle bundle = getIntent().getExtras();


        if (bundle != null) {
            videoId = bundle.getString("video_id");
            videoName = bundle.getString("video_name");
            videoUrl = bundle.getString("video_url");
            videoDurationInMilliSeconds = bundle.getLong("video_duration");
            videoDurationInSeconds = videoDurationInMilliSeconds/1000;
        }


    }

    @Override
    public void onStart() {
        super.onStart();

        downloadTracker.addListener(this);


        if (Util.SDK_INT > 23) {
            initializePlayer();
            setProgress();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!finishFlag)
        {
            if (Util.SDK_INT <= 23 || player == null) {
                initializePlayer();
                setProgress();

                if (playerView != null) {
                    playerView.onResume();
                }
            }

            FullScreencall();
            closeFlag = true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();


        }
    }

    @Override
    public void onStop() {
        super.onStop();
        downloadTracker.removeListener(this);
        handler.removeCallbacks(runnableCode);


        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void finishResultAction() {

        if(closeFlag)
        {
            //overridePendingTransition(R.anim.activity_slide_to_right, R.anim.activity_slide_from_left);

            releasePlayer();
            releaseInstance();
            updateStartPosition();
            closeFlag = false;

            Intent intent = new Intent();
            intent.setAction(modakflixPlayerAction);
            intent.setData(parse(videoUrl));
            intent.putExtra("position", startPosition);
            intent.putExtra("duration", videoDurationInMilliSeconds);
            this.setResult(RESULT_OK, intent);
            finish();
            this.overridePendingTransition(R.anim.nothing,R.anim.activity_slide_to_right);
        }
    }


    // OnClickListener methods
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);
    }

// PlaybackControlView.PlaybackPreparer implementation

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // See whether the player view wants to handle media or DPAD keys events.
        return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.img_setting:

                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (!isShowingTrackSelectionDialog && TrackSelectionDialog.willHaveContent(trackSelector)) {
                        isShowingTrackSelectionDialog = true;
                        TrackSelectionDialog trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(trackSelector,/* onDismissListener= */ dismissedDialog -> isShowingTrackSelectionDialog = false);
                        trackSelectionDialog.show(getSupportFragmentManager(), /* tag= */ null);
                    }
                }

                break;

            case R.id.img_full_screen_enter_exit:
                Format videoFormat = player.getVideoFormat();
                if(videoFormat != null)
                {
                    Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                    int orientation = display.getOrientation();

                    if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        if(getSupportActionBar() != null){
                            getSupportActionBar().show();
                        }
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerView.getLayoutParams();
                        adjustBlackBars();
                        params.width = params.MATCH_PARENT;
                        params.height = (int) ( 200 * getApplicationContext().getResources().getDisplayMetrics().density);
                        playerView.setLayoutParams(params);
                    }

                    else{
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                                |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        if(getSupportActionBar() != null){
                            getSupportActionBar().hide();
                        }
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerView.getLayoutParams();
                        adjustBlackBars();
                        params.width = params.MATCH_PARENT;
                        params.height = params.MATCH_PARENT;
                        playerView.setLayoutParams(params);
                    }
                }
                else
                    Toast.makeText(this, "Video loading... please wait", Toast.LENGTH_LONG).show();

                break;

            case R.id.tv_play_back_speed:
            case R.id.tv_play_back_speed_symbol:

                if (tvPlaybackSpeed.getText().equals("1")) {
                    tapCount++;
                    PlaybackParameters param = new PlaybackParameters(1.25f);
                    player.setPlaybackParameters(param);
                    tvPlaybackSpeed.setText("" + 1.25);
                } else if (tvPlaybackSpeed.getText().equals("1.25")) {
                    tapCount++;
                    PlaybackParameters param = new PlaybackParameters(1.5f);
                    player.setPlaybackParameters(param);
                    tvPlaybackSpeed.setText("" + 1.5);

                } else if (tvPlaybackSpeed.getText().equals("1.5")) {
                    tapCount++;
                    PlaybackParameters param = new PlaybackParameters(1.75f);
                    player.setPlaybackParameters(param);
                    tvPlaybackSpeed.setText("" + 1.75);
                } else if (tvPlaybackSpeed.getText().equals("1.75")) {
                    tapCount++;
                    PlaybackParameters param = new PlaybackParameters(2f);
                    player.setPlaybackParameters(param);
                    tvPlaybackSpeed.setText("" + 2);
                } else {
                    tapCount = 0;
                    player.setPlaybackParameters(null);
                    tvPlaybackSpeed.setText("" + 1);

                }

                break;

            case R.id.img_back_player:
                onBackPressed();
                break;
            case R.id.ll_download_video:
                 ExoDownloadState exoDownloadState = (ExoDownloadState) llDownloadVideo.getTag();

                exoVideoDownloadDecision(exoDownloadState);

                break;

        }


    }

    private void adjustBlackBars()
    {
        Format videoFormat = player.getVideoFormat();
        if(videoFormat != null)
        {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerView.getLayoutParams();
            params.gravity = Gravity.CENTER;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int displaywidth = displayMetrics.widthPixels;
            int displayHeight = displayMetrics.heightPixels;

            int playerHeight = videoFormat.height;
            int playerWidth= videoFormat.width;
            double heightRatio = (double) displayHeight/playerWidth;
            double widthRatio = (double) displaywidth/playerHeight;

            if(heightRatio < widthRatio && isScreenOriatationPortrait(this))
            {
                double apparentHeight = (double) heightRatio * playerHeight;
                double diff = displaywidth - apparentHeight;
                double putTopMargin = diff/2.5;
                double putBottomMargin = diff/2.5;
                params.topMargin = (int) putTopMargin;
                params.bottomMargin = (int) putBottomMargin;
                params.setMarginEnd(0);
                params.setMarginStart(0);
                playerView.setLayoutParams(params);
            }
        }
    }
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private void exoVideoDownloadDecision(ExoDownloadState exoDownloadState){
        if(exoDownloadState == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Please, Tap Again", Toast.LENGTH_SHORT).show();

            return;
        }

        switch (exoDownloadState) {

            case DOWNLOAD_START:
                fetchDownloadOptions();

                break;

            case DOWNLOAD_PAUSE:

                downloadManager.addDownload(downloadTracker.getDownloadRequest(parse(videoUrl)), Download.STATE_STOPPED);

//                DownloadService.sendSetStopReason(
//                        OnlinePlayerActivity.this,
//                        DemoDownloadService.class,
//                        downloadTracker.getDownloadRequest(Uri.parse(videoUrl)).id,
//                        Download.STATE_STOPPED,
//                        /* foreground= */ false);

                break;

            case DOWNLOAD_RESUME:

                downloadManager.addDownload(downloadTracker.getDownloadRequest(parse(videoUrl)), Download.STOP_REASON_NONE);
//                DownloadService.sendSetStopReason(
//                        OnlinePlayerActivity.this,
//                        DemoDownloadService.class,
//                        downloadTracker.getDownloadRequest(Uri.parse(videoUrl)).id,
//                        Download.STOP_REASON_NONE,
//                        /* foreground= */ false);

                break;

            case DOWNLOAD_RETRY:

                break;

            case DOWNLOAD_COMPLETED:
                Toast.makeText(this, "Already Downloaded, Delete from Downloaded video ", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    private void exoButtonPrepareDecision(){
        if (downloadTracker.downloads.size() > 0) {
            Download download = downloadTracker.downloads.get(parse(videoUrl));

            if (download != null) {
                if (download.getPercentDownloaded() > 99.0) {
                    setCommonDownloadButton(ExoDownloadState.DOWNLOAD_COMPLETED);

                } else {
                    //Resume Download Not 100 % Downloaded
                    //So, resume download
                    setCommonDownloadButton(ExoDownloadState.DOWNLOAD_RESUME);
//                    String contentId = download.request.id;
//
//                    DownloadService.sendSetStopReason(
//                            OnlinePlayerActivity.this,
//                            DemoDownloadService.class,
//                            contentId,
//                            Download.STOP_REASON_NONE,
//                            /* foreground= */ false);

                }
            } else {
                // New Download
                setCommonDownloadButton(ExoDownloadState.DOWNLOAD_START);

//                DownloadRequest myDownloadRequest = downloadRequestt;
//                downloadManager.addDownload(myDownloadRequest);

            }

        }else {
            setCommonDownloadButton(ExoDownloadState.DOWNLOAD_START);
        }
    }



    private void fetchDownloadOptions() {
        trackKeys.clear();

        if (pDialog == null || !pDialog.isShowing()) {
            pDialog = new ProgressDialog(OnlinePlayerActivity.this);
            pDialog.setTitle(null);
            pDialog.setCancelable(false);
            pDialog.setMessage("Preparing Download Options...");
            pDialog.show();
        }


        DownloadHelper downloadHelper = DownloadHelper.forHls(OnlinePlayerActivity.this, parse(videoUrl), dataSourceFactory, new DefaultRenderersFactory(OnlinePlayerActivity.this));


        downloadHelper.prepare(new DownloadHelper.Callback() {
            @Override
            public void onPrepared(DownloadHelper helper) {
                // Preparation completes. Now other DownloadHelper methods can be called.
                myDownloadHelper = helper;
                for (int i = 0; i < helper.getPeriodCount(); i++) {
                    TrackGroupArray trackGroups = helper.getTrackGroups(i);
                    for (int j = 0; j < trackGroups.length; j++) {
                        TrackGroup trackGroup = trackGroups.get(j);
                        for (int k = 0; k < trackGroup.length; k++) {
                            Format track = trackGroup.getFormat(k);
                            if (shouldDownload(track)) {
                                trackKeys.add(new TrackKey(trackGroups, trackGroup, track));
                            }
                        }
                    }
                }



                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }


                optionsToDownload.clear();
                showDownloadOptionsDialog(myDownloadHelper, trackKeys);
            }

            @Override
            public void onPrepareError(DownloadHelper helper, IOException e) {

            }
        });
    }

    private void showDownloadOptionsDialog(DownloadHelper helper, List<TrackKey> trackKeyss) {

        if (helper == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(OnlinePlayerActivity.this);
        builder.setTitle("Select Download Format");
        int checkedItem = 1;


        for (int i = 0; i < trackKeyss.size(); i++) {
            TrackKey trackKey = trackKeyss.get(i);
            long bitrate = trackKey.getTrackFormat().bitrate;
            long getInBytes =  (bitrate * videoDurationInSeconds)/8;
             String getInMb = AppUtil.formatFileSize(getInBytes);
             String videoResoultionDashSize =  " "+trackKey.getTrackFormat().height +"      ("+getInMb+")";
             optionsToDownload.add(i, videoResoultionDashSize);
        }

        // Initialize a new array adapter instance
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                OnlinePlayerActivity.this, // Context
                android.R.layout.simple_list_item_single_choice, // Layout
                optionsToDownload // List
        );

        TrackKey trackKey = trackKeyss.get(0);
        qualityParams = ((DefaultTrackSelector) trackSelector).getParameters().buildUpon()
                .setMaxVideoSize(trackKey.getTrackFormat().width, trackKey.getTrackFormat().height)
                .setMaxVideoBitrate(trackKey.getTrackFormat().bitrate)
                .build();

        builder.setSingleChoiceItems(arrayAdapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TrackKey trackKey = trackKeyss.get(i);

                qualityParams = ((DefaultTrackSelector) trackSelector).getParameters().buildUpon()
                        .setMaxVideoSize(trackKey.getTrackFormat().width, trackKey.getTrackFormat().height)
                        .setMaxVideoBitrate(trackKey.getTrackFormat().bitrate)
                        .build();



            }
        });
        // Set the a;ert dialog positive button
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {


                for (int periodIndex = 0; periodIndex < helper.getPeriodCount(); periodIndex++) {
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = helper.getMappedTrackInfo(/* periodIndex= */ periodIndex);
                    helper.clearTrackSelections(periodIndex);
                    for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
//                        TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                        helper.addTrackSelection(
                                periodIndex,
                                qualityParams);
                    }

                }



                DownloadRequest downloadRequest = helper.getDownloadRequest(Util.getUtf8Bytes(videoUrl));
                if (downloadRequest.streamKeys.isEmpty()) {
                    // All tracks were deselected in the dialog. Don't start the download.
                    return;
                }


                startDownload(downloadRequest);

                dialogInterface.dismiss();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    private void startDownload(DownloadRequest downloadRequestt) {

        DownloadRequest myDownloadRequest = downloadRequestt;

        //       downloadManager.addDownload(downloadRequestt);

        if (myDownloadRequest.uri.toString().isEmpty()) {
            Toast.makeText(this, "Try Again!!", Toast.LENGTH_SHORT).show();

            return;
        } else {


//            DownloadRequest downloadRequest = new DownloadRequest(
//                    statusId,
//                    DownloadRequest.TYPE_PROGRESSIVE,
//                    Uri.parse(videoUrl),
//                    /* streamKeys= */ Collections.emptyList(),
//                    /* customCacheKey= */ null,
//                    null);


            downloadManager.addDownload(myDownloadRequest);

        }


    }

    @Override
    public void preparePlayback() {
        initializePlayer();
    }


    private void initializePlayer() {


        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();

    //    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this, null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
        RenderersFactory renderersFactory =  ((ModakflixAdaptivePlayer) getApplication()).buildRenderersFactory(true)  ;

        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        trackSelector.setParameters(trackSelectorParameters);
        lastSeenTrackGroupArray = null;

        DefaultAllocator defaultAllocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);

        DefaultLoadControl defaultLoadControl = new DefaultLoadControl(defaultAllocator,
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
                DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS
        );

        player = new SimpleExoPlayer.Builder(/* context= */ this, renderersFactory).setTrackSelector(trackSelector).setLoadControl(defaultLoadControl).build();
        player.addListener(new PlayerEventListener());
        player.setPlayWhenReady(startAutoPlay);
        player.addAnalyticsListener(new EventLogger(trackSelector));
        player.seekTo(startPosition);
        playerView.setPlayer(player);
        playerView.setPlaybackPreparer(this);

        mediaSource = buildMediaSource(parse(videoUrl));
        if(player != null){
            player.prepare(mediaSource, false, true);
        }


        exoButtonPrepareDecision();

        updateButtonVisibilities();
        initBwd();
        initFwd();

    }

    private boolean shouldDownload(Format track) {
       return track.height != 240 && track.sampleMimeType.equalsIgnoreCase("video/avc");
    }

    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }

    @SuppressWarnings("unchecked")
    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    /**
     * Returns a new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory() {
        return ((ModakflixAdaptivePlayer) getApplication()).buildDataSourceFactory();
    }


    private void updateButtonVisibilities() {
        if (player == null) {
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }

        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                int label;
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.video_player_audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.video_player_video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.video_player_subs;
                        break;
                    default:
                        continue;
                }
            }
        }
    }


    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    private void setProgress() {


        handler = new Handler();
        //Make sure you update Seekbar on UI thread
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (player != null) {
                    tvPlayerCurrentTime.setText(stringForTime((int) player.getCurrentPosition()));
                    tvPlayerEndTime.setText(stringForTime((int) player.getDuration()));

                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void initBwd() {
        imgBwd.requestFocus();
        imgBwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(player.getCurrentPosition() - 10000);
            }
        });
    }

    private void initFwd() {
        imgFwd.requestFocus();
        imgFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(player.getCurrentPosition() + 10000);
            }
        });

    }

    private String stringForTime(int timeMs) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void FullScreencall() {


        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
        }


    }

    public void hideStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        llParentContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        videoId = savedInstanceState.getString("video_id");
        videoName = savedInstanceState.getString("video_name");
        videoUrl = savedInstanceState.getString("video_url");
        description = savedInstanceState.getString("description");
        imageUrl = savedInstanceState.getString("image_url");
        startPosition = Long.parseLong(savedInstanceState.getString("position"));
        videoDurationInMilliSeconds = Long.parseLong(savedInstanceState.getString("video_duration"));

        startPosition = savedInstanceState.getInt(KEY_POSITION);
        trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
        startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
        startWindow = savedInstanceState.getInt(KEY_WINDOW);
        savedInstanceState.getString("");


    }


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onBackPressed() {

        Bundle bundle = new Bundle();
        bundle.putString("video_id","0");
        bundle.putString("video_name", videoName);
        bundle.putString("video_url", videoUrl);
        bundle.putString("description", description);
        bundle.putString("image_url", imageUrl);
        bundle.putString("position", String.valueOf(startPosition));
        bundle.putLong("video_duration", videoDurationInMilliSeconds);
        //onSaveInstanceState(bundle);
        if (!isScreenOriatationPortrait(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            playerView.setLayoutParams(
                    new PlayerView.LayoutParams(
                            // or ViewGroup.LayoutParams.WRAP_CONTENT
                            PlayerView.LayoutParams.MATCH_PARENT,
                            // or ViewGroup.LayoutParams.WRAP_CONTENT,
                            ScreenUtils.convertDIPToPixels(OnlinePlayerActivity.this, playerHeight)));


            frameLayoutMain.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            imgFullScreenEnterExit.setImageResource(R.drawable.exo_controls_fullscreen_enter);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            isScreenLandscape = false;
            hide();

        } else {
            finishResultAction();
            OnlinePlayerActivity.this.finish();
                super.onBackPressed();
        }
    }

    @Override
    public void onDownloadsChanged(Download download) {
        switch (download.state) {
            case STATE_QUEUED:

                break;

            case STATE_STOPPED:


                break;
            case STATE_DOWNLOADING:



                Log.d("EXO DOWNLOADING ", +download.getBytesDownloaded() + " " + download.contentLength);
                Log.d("EXO  DOWNLOADING ", "" + download.getPercentDownloaded());


                break;
            case STATE_COMPLETED:

                setCommonDownloadButton(ExoDownloadState.DOWNLOAD_COMPLETED);
                progressBarPercentage.setVisibility(View.GONE);

                Log.d("EXO COMPLETED ", +download.getBytesDownloaded() + " " + download.contentLength);
                Log.d("EXO  COMPLETED ", "" + download.getPercentDownloaded());


                if(download.request.uri.toString().equals(videoUrl)){

                    if(download.getPercentDownloaded() != -1){
                        progressBarPercentage.setVisibility(View.VISIBLE);
                        progressBarPercentage.setProgress(Integer.parseInt(AppUtil.floatToPercentage(download.getPercentDownloaded()).replace("%","")));
//                        tvDownloadProgressMb.setText(AppUtil.getProgressDisplayLine(download.getBytesDownloaded(),download.contentLength)+" MB");
//                        tvProgressPercentage.setText(AppUtil.floatToPercentage(download.getPercentDownloaded()));
                    }
                }

                progressBarPercentage.setVisibility(View.GONE);

                break;

            case STATE_FAILED:


                break;

            case STATE_REMOVING:


                break;

            case STATE_RESTARTING:

                break;

        }

    }

    private void releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            player.release();
            player = null;
            mediaSource = null;
            trackSelector = null;
        }
        if (adsLoader != null) {
            adsLoader.setPlayer(null);
        }
    }

    private void updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector.getParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
            videoDurationInMilliSeconds = player.getDuration();
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        //startPosition = C.TIME_UNSET;
    }



    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case ExoPlayer.STATE_READY:
                    exoProgressbar.setVisibility(View.GONE);

                    break;
                case ExoPlayer.STATE_BUFFERING:

                    exoProgressbar.setVisibility(View.VISIBLE);
                    break;
            }
            updateButtonVisibilities();
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (player.getPlaybackError() != null) {
                // The user has performed a seek whilst in the error state. Update the resume position so
                // that if the user then retries, playback resumes from the position to which they seeked.
                updateStartPosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                initializePlayer();
            } else {
                updateStartPosition();
                updateButtonVisibilities();
//                showControls();
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            updateButtonVisibilities();
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.codecInfo == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString =
                                    getString(
                                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                        } else {
                            errorString =
                                    getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString =
                                getString(
                                        R.string.error_instantiating_decoder,
                                        decoderInitializationException.codecInfo);
                    }
                }
            }
            return Pair.create(0, errorString);
        }
    }




    public void setCommonDownloadButton(ExoDownloadState exoDownloadState) {
        switch (exoDownloadState) {
            case DOWNLOAD_START:
                llDownloadVideo.setTag(exoDownloadState);
                tvDownloadState.setText(exoDownloadState.getValue());
                imgDownloadState.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));

                break;

            case DOWNLOAD_PAUSE:
                llDownloadVideo.setTag(exoDownloadState);
                tvDownloadState.setText(exoDownloadState.getValue());
                imgDownloadState.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                break;

            case DOWNLOAD_RESUME:
                llDownloadVideo.setTag(exoDownloadState);
                tvDownloadState.setText(exoDownloadState.getValue());
                imgDownloadState.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                break;

            case DOWNLOAD_RETRY:
                llDownloadVideo.setTag(exoDownloadState);
                tvDownloadState.setText(exoDownloadState.getValue());
                imgDownloadState.setImageDrawable(getResources().getDrawable(R.drawable.ic_retry));

                break;

            case DOWNLOAD_COMPLETED:
                llDownloadVideo.setTag(exoDownloadState);
                tvDownloadState.setText(exoDownloadState.getValue());
                imgDownloadState.setImageDrawable(getResources().getDrawable(R.drawable.ic_download_complete));

                break;

            case DOWNLOAD_QUEUE:
                llDownloadVideo.setTag(exoDownloadState);
                tvDownloadState.setText(exoDownloadState.getValue());
                imgDownloadState.setImageDrawable(getResources().getDrawable(R.drawable.ic_queue));

                break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    public Uri getSubtitle(String showName)
    {

        // connect to opensubtitle.org
        List<SubtitleInfo> subsList = searchSubtitle(showName);
        if(subsList!=null && subsList.size()>0)
        {
            showSubtitleListDialog("Subtitles found for : "+videoName, subsList);

        }


        return subtitleUri;

    }

    private void showSubtitleListDialog(String title, List<SubtitleInfo> subsList)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(OnlinePlayerActivity.this);
                    View layoutView = getLayoutInflater().inflate(R.layout.subtitle_list_preview_dialog, null);
                    Dialog settingsDialog = adb.setView(layoutView).create();
                    settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

                    TextView titleView = layoutView.findViewById(R.id.titleSubListPreview);
                    titleView.setText(title);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(settingsDialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                    settingsDialog.getWindow().setAttributes(lp);

                    // Set the elements
                    LinearLayout ll = layoutView.findViewById(R.id.previewSubAttach);
                    for(SubtitleInfo subtitleInfo : subsList)
                    {
                        TextView tv = new TextView(OnlinePlayerActivity.this);
                        tv.setText(subtitleInfo.getSubFileName());
                        ll.addView(tv);
                    }

                    settingsDialog.setContentView(layoutView);
                    settingsDialog.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public List<SubtitleInfo> searchSubtitle(String showName)
    {
        List<SubtitleInfo> output = new ArrayList<>();
        OpenSubtitle openSubtitle=new OpenSubtitle();
        try {

            openSubtitle.login();

//  openSubtitle.ServerInfo();
//  openSubtitle.getSubLanguages();

            output = openSubtitle.getMovieSubsByName(showName, "20", "en");

//  openSubtitle.getTvSeriesSubs("game of thrones","1","1","10","eng");
//  openSubtitle.Search("/home/Downloads/Minions.2015.720p.BRRip.850MB.MkvCage.mkv");



        }
        catch (Exception e1) {
            openSubtitle.logOut();
            e1.printStackTrace();
            showDialog(getString(R.string.server_no_response));
        }

        return output;

    }
    private class LoadSubsOnline extends AsyncTask<String, Void, Integer> {
        String zipPath;
        @RequiresApi(api = Build.VERSION_CODES.R)
        protected Integer doInBackground(String... urls) {
            subtitleUri = getSubtitle(videoName);
            zipPath = downloadZip(subtitleUri);

            try {
                // subtitleUri = unzip(new File(zipPath), new File(zipPath.split(".gz")[0]+".srt"));
                String filePath = unGunzipFile(zipPath, zipPath.split(".gz")[0]+".srt");
                if(filePath!=null && !filePath.isEmpty())
                {
                    String temp = uploadFile(filePath, filePath, new File(filePath).getName());
                    subtitleUri = Uri.parse(temp);
                }



            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(subtitleUri!= null && !subtitleUri.toString().isEmpty())
            {
                if(subtitleUri!=null && !subtitleUri.toString().isEmpty())
                    addSubtitle(subtitleUri);
            }
        }
    }

    private class LoadSubsOffline extends AsyncTask<String, Void, Integer> {
        String filePath;
        @RequiresApi(api = Build.VERSION_CODES.R)
        protected Integer doInBackground(String... urls) {

            try {
                filePath = urls[0];
                if(filePath!=null && !filePath.isEmpty())
                {
                    String temp = uploadFile(filePath, filePath, new File(filePath).getName());
                    subtitleUri = Uri.parse(temp);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(subtitleUri!= null && !subtitleUri.toString().isEmpty())
            {
                if(subtitleUri!=null && !subtitleUri.toString().isEmpty())
                    addSubtitle(subtitleUri);
            }
        }
    }

    public String chooseSubtitle()
    {
        final String[] filePathArray = {""};
        new ChooserDialog(OnlinePlayerActivity.this)
                .withStartFile("")
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        Toast.makeText(OnlinePlayerActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        LoadSubsOffline lsf = new LoadSubsOffline();
                        lsf.execute(path);
                    }
                })
                // to handle the back key pressed or clicked outside the dialog:
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Log.d("CANCEL", "CANCEL");
                        dialog.cancel(); // MUST have
                    }
                })
                .build()
                .show();

        return filePathArray[0];
    }

    public String uploadFile(String sourceFileUri, String uploadFilePath, String uploadFileName) {

        int serverResponseCode = 0;
        String output="";
        String upLoadServerUri = Profiles.upload;
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {



            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "" + uploadFileName);


        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                   dos.writeBytes(lineEnd);

                   // create a buffer of  maximum size
                   bytesAvailable = fileInputStream.available();

                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   buffer = new byte[bufferSize];

                   // read file and write it into form...
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                   while (bytesRead > 0) {

                     dos.write(buffer, 0, bufferSize);
                     bytesAvailable = fileInputStream.available();
                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                   // send multipart form data necesssary after file data...
                   dos.writeBytes(lineEnd);
                   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                   // Responses from the server (code and message)
                   serverResponseCode = conn.getResponseCode();
                   String serverResponseMessage = conn.getResponseMessage();

                   Log.i("uploadFile", "HTTP Response is : "
                           + serverResponseMessage + ": " + serverResponseCode);


                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    {
                        output += inputLine;
                    }
                }
                in.close();

                   //close the streams //
                   fileInputStream.close();
                   dos.flush();
                   dos.close();

              } catch (MalformedURLException ex) {

                  ex.printStackTrace();

                  runOnUiThread(new Runnable() {
                      public void run() {
                          Toast.makeText(OnlinePlayerActivity.this, "MalformedURLException",
                                                              Toast.LENGTH_SHORT).show();
                      }
                  });

                  Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
              } catch (Exception e) {

                  e.printStackTrace();

                  runOnUiThread(new Runnable() {
                      public void run() {
                          Toast.makeText(OnlinePlayerActivity.this, "Got Exception : see logcat ",
                                  Toast.LENGTH_SHORT).show();
                      }
                  });
                  Log.e("Upload file to server Exception", "Exception : "
                                                   + e.getMessage(), e);
              }


           } // End else block
        return output;
         }

    private String downloadZip(Uri uri) {
        File sdCardRoot = new File("");

        HttpURLConnection urlConnection = null;
        try {
            strings[0] = uri.toString();
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();


            sdCardRoot = new File(Environment.getExternalStorageDirectory(), "ModakFlix");

            if (!sdCardRoot.exists()) {
                sdCardRoot.mkdirs();
            }

            Log.e("check_path", "" + sdCardRoot.getAbsolutePath());

            String fileName =
                    strings[0].substring(strings[0].lastIndexOf('/') + 1, strings[0].length());
            Log.e("dfsdsjhgdjh", "" + fileName);
            File imgFile =
                    new File(sdCardRoot, fileName);
            if (!sdCardRoot.exists()) {
                imgFile.createNewFile();
            }
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            FileOutputStream outPut = new FileOutputStream(imgFile);
            int downloadedSize = 0;
            byte[] buffer = new byte[2024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                outPut.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                Log.e("Progress:", "downloadedSize:" + Math.abs(downloadedSize * 100 / totalSize));
            }
            Log.e("Progress:", "imgFile.getAbsolutePath():" + imgFile.getAbsolutePath());

            Log.e("TAG", "check image path 2" + imgFile.getAbsolutePath());

            outPut.close();
            return imgFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("checkException:-", "" + e);
            return null;
        }
    }

    public String unGunzipFile(String compressedFile, String decompressedFile) {

        byte[] buffer = new byte[1024];

        try {

            FileInputStream fileIn = new FileInputStream(compressedFile);

            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);

            FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile);

            int bytes_read;

            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {

                fileOutputStream.write(buffer, 0, bytes_read);
            }

            gZIPInputStream.close();
            fileOutputStream.close();

            System.out.println("The file was decompressed successfully!");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return decompressedFile;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Uri unzip(File zipFile, File targetDirectory) throws IOException {

        File file = new File("");
        InputStream inputStream = null;
        try {
            Path filePath = Paths.get(String.valueOf(zipFile));
            inputStream = Files.newInputStream(filePath);
            ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
            ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
            ArchiveEntry archiveEntry = null;
            while((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                Path path = Paths.get(String.valueOf(targetDirectory), archiveEntry.getName());
                file = path.toFile();
                if(archiveEntry.isDirectory()) {
                    if(!file.isDirectory()) {
                        file.mkdirs();
                    }
                } else {
                    File parent = file.getParentFile();
                    if(!parent.isDirectory()) {
                        parent.mkdirs();
                    }
                    try (OutputStream outputStream = Files.newOutputStream(path)) {
                        IOUtils.copy(archiveInputStream, outputStream);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArchiveException e) {
            e.printStackTrace();
        }


        return Uri.fromFile(file);
    }

    private void openSubtitleSelector()
    {
        try {
            boolean dismissFlag = true;
            androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(this);
            View layoutView = getLayoutInflater().inflate(R.layout.subtitle_preview_dialog, null);
            Dialog settingsDialog = adb.setView(layoutView).create();
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(settingsDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            settingsDialog.getWindow().setAttributes(lp);

            // Set the elements

            // Set open from storage button
            Button openStgBtn = layoutView.findViewById(R.id.openFromInternal);
            openStgBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onClick(View v) {

                    if(!PermissionClass.checkRequiredPermission(OnlinePlayerActivity.this))
                    {
                        player.setPlayWhenReady(false);
                        REQEST_CODE = 122;
                        PermissionClass p = new PermissionClass(OnlinePlayerActivity.this, OnlinePlayerActivity.this, REQEST_CODE);
                        p.getPermission();
                    }
                    else
                    {
                        player.setPlayWhenReady(false);
                        chooseSubtitle();
                    }
                    settingsDialog.dismiss();
                }
            });

            // Set download subtitle button
            Button downloadSubtitle = layoutView.findViewById(R.id.downloadSubtitle);
            downloadSubtitle.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onClick(View v) {

                    /*if(!PermissionClass.checkRequiredPermission(OnlinePlayerActivity.this))
                    {
                        REQEST_CODE = 123;
                        PermissionClass p = new PermissionClass(OnlinePlayerActivity.this, OnlinePlayerActivity.this, REQEST_CODE);
                        p.getPermission();
                    }
                    else
                    {
                        LoadSubsOnline ls =  new LoadSubsOnline();
                        ls.execute();
                    }
                    settingsDialog.dismiss();*/
                    Toast.makeText(OnlinePlayerActivity.this, getString(R.string.feature_disabled), Toast.LENGTH_LONG).show();
                }
            });

            settingsDialog.setContentView(layoutView);
            settingsDialog.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void showDialog(String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(OnlinePlayerActivity.this);
                alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chooseSubtitle();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialogBuilder.show();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(!Environment.isExternalStorageManager())
            {
                Toast.makeText(OnlinePlayerActivity.this, "Cannot continue without write permission.", Toast.LENGTH_LONG).show();
                System.exit(0);
            }
            PermissionClass p = new PermissionClass(OnlinePlayerActivity.this, OnlinePlayerActivity.this, REQEST_CODE);
            p.permission(1);
            // PermissionClass.permissionCompletedFlag = true;

            switch (REQEST_CODE)
            {
                case 122:
                    break;
                case 123:
                    LoadSubsOnline ls =  new LoadSubsOnline();
                    ls.execute();
                    break;
                default:
                    Toast.makeText(this, "Invalid Request made.", Toast.LENGTH_LONG).show();
            }


    }

    private static final int SELECT_ITEM = 1;
    private String selectedImagePath;
    private void selectFromExternalStorage()
    {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select subtitles"), SELECT_ITEM);
    }

    private void addSubtitle(Uri subtitleUri)
    {

        startPosition = Math.max(0, player.getContentPosition());
        Uri videoURI = parse(videoUrl);
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");

        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(videoURI, dataSourceFactory, extractorsFactory, null, null);

        // Build the subtitle MediaSource.
        Format subtitleFormat = Format.createTextSampleFormat("", MimeTypes.APPLICATION_SUBRIP,Format.NO_VALUE, "en");


        MediaSource subtitleSource =new SingleSampleMediaSource(subtitleUri, dataSourceFactory, subtitleFormat, C.TIME_UNSET);

        MergingMediaSource mergedSource =
                new MergingMediaSource(mediaSource, subtitleSource);

        playerView.setPlayer(player);
        player.prepare(mergedSource);
        player.setPlayWhenReady(startAutoPlay);

        player.seekTo(startPosition);
    }


}