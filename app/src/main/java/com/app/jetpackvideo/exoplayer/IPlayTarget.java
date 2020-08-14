package com.app.jetpackvideo.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget
{
    ViewGroup getOwner();

    // 视频播放
    void onActive();

    // 视频暂停
    void inActive();

    boolean isPlaying();
}
