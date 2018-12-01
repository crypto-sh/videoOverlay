package com.videooverlay;


import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class VideoPlayerOverviewService extends Service implements View.OnTouchListener {

    float offsetX;
    float offsetY;

    int originalXPos;
    int originalYPos;

    boolean moving;

    SimpleExoPlayer player          = null;
    WindowManager windowManager     = null;
    PlayerView simpleExoPlayerView  = null;

    final String TAG = VideoPlayerOverviewService.class.getSimpleName();

    public VideoPlayerOverviewService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    Utils.convertDpToPixelsInt(300, getApplicationContext()),
                    Utils.convertDpToPixelsInt(169, getApplicationContext()),
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

            }
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 100;
            params.y = 200;

            if (simpleExoPlayerView != null) {
                simpleExoPlayerView.setKeepScreenOn(true);
                simpleExoPlayerView.setLayoutParams(params);
                simpleExoPlayerView.setOnTouchListener(this);
                windowManager.addView(simpleExoPlayerView, params);
            } else {
                simpleExoPlayerView = new PlayerView(getApplicationContext());
                simpleExoPlayerView.setKeepScreenOn(true);
                simpleExoPlayerView.setLayoutParams(params);
                simpleExoPlayerView.setOnTouchListener(this);
                windowManager.addView(simpleExoPlayerView, params);
            }
        } catch (Exception e) {
            Utils.LogData(false, TAG, e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (simpleExoPlayerView != null) {
            String url = intent.getStringExtra("data");
            initializePlayer(url);
        }
        return START_NOT_STICKY;
    }

    void initializePlayer(String contentUrl) {
        // Create a default track selector.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // Create a player instance.
        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        // Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), getString(R.string.app_name)));
        // This is the MediaSource representing the content media (i.e. not the ad).
        MediaSource contentMediaSource =
                new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(contentUrl));

        player.prepare(contentMediaSource);
        player.seekTo(0L);
        player.setPlayWhenReady(true);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int[] topLeftLocationOnScreen = new int[2];
            simpleExoPlayerView.getLocationOnScreen(topLeftLocationOnScreen);
            moving = false;
            offsetX = topLeftLocationOnScreen[0] - event.getRawX();
            offsetY = topLeftLocationOnScreen[1] - event.getRawY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) simpleExoPlayerView.getLayoutParams();
            int newX = (int) (offsetX + event.getRawX());
            int newY = (int) (offsetY + event.getRawY());
            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }
            params.x = newX;
            params.y = newY;
            windowManager.updateViewLayout(simpleExoPlayerView, params);
            moving = true;
        }
        return false;
    }
}
