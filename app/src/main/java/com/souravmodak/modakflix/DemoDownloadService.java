package com.souravmodak.modakflix;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.util.Util;
import com.souravmodak.modakflix.R;

import java.util.List;
import java.util.Objects;

/**
 * A service for downloading media.
 */
public class DemoDownloadService extends DownloadService implements DownloadTracker.Listener {

    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableCode;
    private DownloadManager downloadManager;
    private DownloadTracker downloadTracker;

    public DemoDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        ModakflixAdaptivePlayer application = (ModakflixAdaptivePlayer) getApplication();
        DownloadManager downloadManager = application.getDownloadManager();
        downloadManager.setMaxParallelDownloads(1);
        return downloadManager;
    }

    @Override
    protected Scheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads, int notUsed) {
        // Find the first downloading item to show progress for
        Download activeDownload = null;
        for (Download download : downloads) {
            if (download.state == Download.STATE_DOWNLOADING || download.state == Download.STATE_RESTARTING) {
                activeDownload = download;
                break;
            }
        }

        if (activeDownload == null && !downloads.isEmpty()) {
            activeDownload = downloads.get(0);
        }

        if (activeDownload == null) {
            // Return a simple notification if no downloads
            return new NotificationCompat.Builder(this, AppUtil.createExoDownloadNotificationChannel(this))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Downloads")
                    .setContentText("Initializing...")
                    .build();
        }

        VideoModel videoModel = AppUtil.getVideoDetail(activeDownload.request.id);
        String title = videoModel != null ? videoModel.getVideoName() : "Video";

        return new NotificationCompat.Builder(this, AppUtil.createExoDownloadNotificationChannel(this))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(activeDownload.state == Download.STATE_DOWNLOADING ? "Downloading" : "Queued")
                .setProgress(100, (int) activeDownload.getPercentDownloaded(), false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        ModakflixAdaptivePlayer application = (ModakflixAdaptivePlayer) getApplication();
        downloadManager = application.getDownloadManager();
        downloadTracker = application.getDownloadTracker();
        downloadTracker.addListener(this);

        if (intent != null && intent.getAction() != null) {
            Uri uri = intent.getData();
            String action = intent.getAction();
            if (uri != null) {
                switch (action) {
                    case AppConstant.EXO_DOWNLOAD_ACTION_PAUSE:
                        downloadManager.addDownload(downloadTracker.getDownloadRequest(uri), Download.STATE_STOPPED);
                        break;
                    case AppConstant.EXO_DOWNLOAD_ACTION_START:
                        downloadManager.addDownload(downloadTracker.getDownloadRequest(uri), Download.STOP_REASON_NONE);
                        break;
                    case AppConstant.EXO_DOWNLOAD_ACTION_CANCEL:
                        downloadManager.removeDownload(downloadTracker.getDownloadRequest(uri).id);
                        break;
                }
            }
        }

        if (runnableCode == null) {
            runnableCode = new Runnable() {
                @Override
                public void run() {
                    checkAndStartDownload(getApplicationContext());
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnableCode);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void checkAndStartDownload(Context context) {
        if (downloadManager.getCurrentDownloads().isEmpty()) {
            handler.removeCallbacks(runnableCode);
            runnableCode = null;
        }
    }

    @Override
    public void onDestroy() {
        if (downloadTracker != null) {
            downloadTracker.removeListener(this);
        }
        if (runnableCode != null) {
            handler.removeCallbacks(runnableCode);
        }
        super.onDestroy();
    }

    @Override
    public void onDownloadsChanged(Download download) {
        // Handled by super class notification logic if configured
    }
}