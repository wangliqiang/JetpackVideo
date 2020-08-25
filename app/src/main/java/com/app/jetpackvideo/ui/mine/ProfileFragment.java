package com.app.jetpackvideo.ui.mine;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import android.text.TextUtils;
import android.view.View;

import com.app.jetpackvideo.base.AbsListFragment;
import com.app.jetpackvideo.exoplayer.PageListPlayDetector;
import com.app.jetpackvideo.exoplayer.PageListPlayManager;
import com.app.jetpackvideo.model.Feed;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

public class ProfileFragment extends AbsListFragment<Feed, ProfileViewModel> {


    private String tabType;
    private PageListPlayDetector playDetector;
    private boolean shouldPause = true;

    public static ProfileFragment newInstance(String tabType) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ProfileActivity.KEY_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playDetector = new PageListPlayDetector(this, recyclerView);
        viewModel.setProfileType(tabType);
        refreshLayout.setEnableRefresh(false);
    }

    @Override
    public PagedListAdapter getAdapter() {
        tabType = getArguments().getString(ProfileActivity.KEY_TAB_TYPE);
        return new ProfileListAdapter(getContext(), tabType) {
            @Override
            protected void onViewAttachedToWindowOfSub(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.listPlayerView);
                }
            }

            @Override
            protected void onViewDetachedFromWindowOfSub(ViewHolder holder) {
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
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = adapter.getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

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
        if (TextUtils.equals(tabType, ProfileActivity.TAB_TYPE_COMMENT)) {
            playDetector.onPause();
        } else {
            playDetector.onResume();
        }
    }

    @Override
    public void onDestroyView() {
        PageListPlayManager.release(tabType);
        super.onDestroyView();
    }
}