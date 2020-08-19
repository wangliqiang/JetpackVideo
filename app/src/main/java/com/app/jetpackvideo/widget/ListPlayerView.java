package com.app.jetpackvideo.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.exoplayer.IPlayTarget;
import com.app.jetpackvideo.exoplayer.PageListPlay;
import com.app.jetpackvideo.exoplayer.PageListPlayManager;
import com.app.jetpackvideo.utils.PixUtils;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

public class ListPlayerView extends FrameLayout implements IPlayTarget,
        PlayerControlView.VisibilityListener, Player.EventListener {
    private View bufferView;
    public CusImageView cover, blur;
    protected ImageView playBtn;
    protected String mCategory;
    protected String mVideoUrl;
    protected int mWidthPx;
    protected int mHeightPx;

    protected Boolean isPlaying = false;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        bufferView = findViewById(R.id.buffer_view);
        cover = findViewById(R.id.cover);
        blur = findViewById(R.id.blur_backgrouond);
        playBtn = findViewById(R.id.play_btn);

        playBtn.setOnClickListener(v -> {
            if (isPlaying()) {
                inActive();
            } else {
                onActive();
            }
        });
    }

    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;
        mWidthPx = widthPx;
        mHeightPx = heightPx;

        cover.setImageUrl(cover, coverUrl, false);
        if (widthPx < heightPx) {
            blur.setBlurImageUrl(blur, coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx, heightPx);
    }

    protected void setSize(int widthPx, int heightPx) {
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeigth = maxWidth;

        int layoutWidth = maxWidth;
        int layoutHeigth = 0;

        int coverWidth;
        int coverHeigth;
        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeigth = coverHeigth = (int) (heightPx / (widthPx * 1.0 / maxWidth));
        } else {
            layoutHeigth = coverHeigth = maxHeigth;
            coverWidth = (int) (widthPx / (heightPx * 1.0f / maxHeigth));
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeigth;
        setLayoutParams(params);

        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeigth;
        blur.setLayoutParams(blurParams);

        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeigth;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);


        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);

    }

    // 视频播放
    public void onActive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = pageListPlay.playerView;
        PlayerControlView controlView = pageListPlay.controllerView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }

        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView, true);
        ViewParent parent = playerView.getParent();
        if (parent != this) {
            //把展示视频画面的View添加到ItemView的容器上
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                //还应该暂停掉列表上正在播放的那个
                ((ListPlayerView) parent).inActive();
            }

            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            //把视频控制器 添加到ItemView的容器上
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlay.playUrl = mVideoUrl;
        }
        controlView.show();
        controlView.addVisibilityListener(this);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }

    // 视频暂停
    public void inActive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        if (pageListPlay.exoPlayer == null || pageListPlay.controllerView == null || pageListPlay.exoPlayer == null)
            return;
        pageListPlay.exoPlayer.setPlayWhenReady(false);
        pageListPlay.controllerView.setOnSystemUiVisibilityChangeListener(null);
        pageListPlay.exoPlayer.removeListener(this);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 点击该去浴室，主动让视频控制器显示出来
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.controllerView.show();
        return true;
    }

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // 监听视频播放的状态
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            cover.setVisibility(GONE);
            bufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            bufferView.setVisibility(VISIBLE);
        }
        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;

        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public void onVisibilityChange(int visibility) {
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(GONE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    public View getPlayController() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        return pageListPlay.controllerView;
    }

}
