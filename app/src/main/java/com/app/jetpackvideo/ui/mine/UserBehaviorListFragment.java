package com.app.jetpackvideo.ui.mine;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.app.jetpackvideo.base.AbsListFragment;
import com.app.jetpackvideo.exoplayer.PageListPlayDetector;
import com.app.jetpackvideo.exoplayer.PageListPlayManager;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.ui.home.FeedAdapter;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

public class UserBehaviorListFragment extends AbsListFragment<Feed, UserBehaviorViewModel> {
    private static final String CATEGORY = "user_behavior_list";
    private boolean shouldPause = true;
    private PageListPlayDetector playDetector;

    public static UserBehaviorListFragment newInstance(int behavior) {

        Bundle args = new Bundle();
        args.putInt(UserBehaviorListActivity.KEY_BEHAVIOR, behavior);
        UserBehaviorListFragment fragment = new UserBehaviorListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playDetector = new PageListPlayDetector(this, recyclerView);
        int behavior = getArguments().getInt(UserBehaviorListActivity.KEY_BEHAVIOR);
        viewModel.setBehavior(behavior);
    }

    @Override
    public PagedListAdapter getAdapter() {
        return new FeedAdapter(getContext(), CATEGORY) {
            @Override
            public void onViewAttachedToWindowOfSub(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.listPlayerView);
                }
            }

            @Override
            public void onViewDetachedFromWindowOfSub(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.removeTarget(holder.listPlayerView);
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                shouldPause = false;
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPause) {
            playDetector.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        playDetector.onResume();
    }

    @Override
    public void onDestroyView() {
        PageListPlayManager.release(CATEGORY);
        super.onDestroyView();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = adapter.getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        viewModel.getDataSource().invalidate();
    }
}
