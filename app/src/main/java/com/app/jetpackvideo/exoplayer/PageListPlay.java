package com.app.jetpackvideo.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.utils.AppGlobals;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class PageListPlay {

    public SimpleExoPlayer exoPlayer;
    public PlayerView playerView;
    public PlayerControlView controllerView;

    public String playUrl;

    public PageListPlay() {
        Application application = AppGlobals.getApplication();
        // 创建ExoPlayer播放器实例
        exoPlayer = new SimpleExoPlayer
                // 视频每一帧的画面如何渲染，实现默认的实现类
                .Builder(application)
                // 视频的音视频轨道如何加载，使用默认的贵高选择器
                .setTrackSelector(new DefaultTrackSelector(application))
                // 视频缓存控制逻辑，使用默认的即刻
                .setLoadControl(new DefaultLoadControl())
                .build();

        // 加载布局层级优化之后能够展示视频画面的View
        playerView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_view, null, false);
        // 加载布局层级优化之后的视频播放控制器
        controllerView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_controller_view, null, false);

        // 把播放器实例和控制器实例与PlayerView和PlayerControllerView关联
        // 如此视频画面才呢能正常显示，播放进度条才能自动更新
        playerView.setPlayer(exoPlayer);
        controllerView.setPlayer(exoPlayer);

    }

    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        playerView.setPlayer(attach ? null : exoPlayer);
        newPlayerView.setPlayer(attach ? exoPlayer : null);
    }

    // 释放播放器和控制器
    public void release() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop(true);
            exoPlayer.release();
            exoPlayer = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }
        if (controllerView != null) {
            controllerView.setPlayer(null);
            controllerView.addVisibilityListener(null);
            controllerView = null;
        }
    }
}
