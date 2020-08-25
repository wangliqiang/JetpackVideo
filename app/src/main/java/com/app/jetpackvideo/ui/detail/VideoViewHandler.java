package com.app.jetpackvideo.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.databinding.LayoutFeedDetailTypeVideoBinding;
import com.app.jetpackvideo.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.utils.StatusBar;
import com.app.jetpackvideo.widget.FullScreenPlayerView;

public class VideoViewHandler extends ViewHandler {

    private LayoutFeedDetailTypeVideoBinding binding;
    private FullScreenPlayerView playerView;
    private CoordinatorLayout coordinator;
    private String category;
    private boolean backPressed;

    public VideoViewHandler(FragmentActivity mActivity) {
        super(mActivity);
        binding = DataBindingUtil.setContentView(mActivity, R.layout.layout_feed_detail_type_video);
        interactionBinding = binding.bottomInteraction;
        recyclerView = binding.recyclerView;
        playerView = binding.playerView;
        coordinator = binding.coordinator;

        View anthorInfoView = binding.authorInfo.getRoot();

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) anthorInfoView.getLayoutParams();
        layoutParams.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        binding.actionClose.setOnClickListener(v -> mActivity.finish());

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) params.getBehavior();
        behavior.setViewZoomCallback(height -> {
            int bottom = playerView.getBottom();
            boolean moveUp = height < bottom;
            boolean fullscreen = moveUp ? height >= coordinator.getBottom() - interactionBinding.getRoot().getHeight()
                    : height >= coordinator.getBottom();
            setViewAppearance(fullscreen);
        });

    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        binding.setFeed(feed);
        binding.setStatusBarHeight(StatusBar.getStatusBarHeight(mActivity));

        category = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        playerView.bindData(category, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        playerView.post(() -> {
            boolean fullscreen = playerView.getBottom() >= coordinator.getBottom();
            setViewAppearance(fullscreen);
        });

        LayoutFeedDetailTypeVideoHeaderBinding headerBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(mActivity), recyclerView, false);

        headerBinding.setFeed(feed);

        commentAdapter.addHeaderView(headerBinding.getRoot());
    }

    public void setViewAppearance(boolean fullscreen) {
        binding.setFullscreen(fullscreen);
        interactionBinding.setFullscreen(fullscreen);
        binding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);

        // 底部互动区域的高度
        int inputHeight = interactionBinding.getRoot().getMeasuredHeight();
        // 播放控制器的高度
        int ctrlViewHeight = playerView.getPlayController().getMeasuredHeight();
        // 播放控制器的bottom值
        int bottom = playerView.getPlayController().getBottom();
        // 全屏播放时，播放控制器需要处在底部互动区域的上面
        playerView.getPlayController().setY(fullscreen ? bottom - inputHeight - ctrlViewHeight : bottom - ctrlViewHeight);

        interactionBinding.inputView.setBackgroundResource(fullscreen ? R.drawable.bg_edit_view2 : R.drawable.bg_edit_view);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        //按了返回键后需要 恢复 播放控制器的位置。否则回到列表页时 可能会不正确的显示
        playerView.getPlayController().setTranslationY(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            playerView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        playerView.onActive();
    }
}
