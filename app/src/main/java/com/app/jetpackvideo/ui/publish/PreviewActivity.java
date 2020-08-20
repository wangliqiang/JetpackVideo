package com.app.jetpackvideo.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.databinding.ActivityPreviewBinding;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_PREVIEW_URL = "preview_url";
    public static final String KEY_PREVIEW_VIDEO = "preview_video";
    public static final String KEY_PREVIEW_BTNTEXT = "preview_btntext";
    public static final int REQ_PREVIEW = 1000;
    private ActivityPreviewBinding binding;
    private SimpleExoPlayer player;

    public static void startActivityForResult(Activity activity, String previewUrl, boolean isVideo, String btnText) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL, previewUrl);
        intent.putExtra(KEY_PREVIEW_VIDEO, isVideo);
        intent.putExtra(KEY_PREVIEW_BTNTEXT, btnText);
        activity.startActivityForResult(intent, REQ_PREVIEW);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview);
        String previewUrl = getIntent().getStringExtra(KEY_PREVIEW_URL);
        boolean isVideo = getIntent().getBooleanExtra(KEY_PREVIEW_VIDEO, false);
        String btnText = getIntent().getStringExtra(KEY_PREVIEW_BTNTEXT);

        if (TextUtils.isEmpty(btnText)) {
            binding.actionOk.setVisibility(View.GONE);
        } else {
            binding.actionOk.setVisibility(View.VISIBLE);
            binding.actionOk.setText(btnText);
            binding.actionOk.setOnClickListener(this);
        }

        binding.actionClose.setOnClickListener(this);

        if (isVideo) {
            previewVideo(previewUrl);
        } else {
            previewImage(previewUrl);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_close) {
            finish();
        } else if (v.getId() == R.id.action_ok) {
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

    private void previewImage(String previewUrl) {
        binding.photoView.setVisibility(View.VISIBLE);
        Glide.with(this).load(previewUrl).into(binding.photoView);
    }

    private void previewVideo(String previewUrl) {
        binding.playerView.setVisibility(View.VISIBLE);
        player = new SimpleExoPlayer
                // 视频每一帧的画面如何渲染，实现默认的实现类
                .Builder(this)
                // 视频的音视频轨道如何加载，使用默认的贵高选择器
                .setTrackSelector(new DefaultTrackSelector(this))
                // 视频缓存控制逻辑，使用默认的即刻
                .setLoadControl(new DefaultLoadControl())
                .build();
        Uri uri = null;
        File file = new File(previewUrl);
        if (file.exists()) {
            DataSpec dataSpec = new DataSpec(Uri.fromFile(file));
            FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri = fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        } else {
            uri = Uri.parse(previewUrl);
        }
        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, Util.getUserAgent(this, getPackageName())));
        ProgressiveMediaSource mediaSource = factory.createMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        binding.playerView.setPlayer(player);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop(true);
            player.release();
        }
    }
}