package com.app.jetpackvideo.ui.detail;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.app.jetpackvideo.model.Comment;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.utils.PixUtils;
import com.app.jetpackvideo.widget.EmptyView;

public abstract class ViewHandler {
    protected FeedDetailViewModel viewModel;
    protected FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView recyclerView;
    protected FeedCommentAdapter commentAdapter;

    protected LayoutFeedDetailBottomInteractionBinding interactionBinding;
    private CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity) {
        mActivity = activity;
        viewModel = new ViewModelProvider(mActivity).get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
        interactionBinding.setOwner(mActivity);

        mFeed = feed;
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(null);
        commentAdapter = new FeedCommentAdapter(mActivity) {
            @Override
            public void onCurrentListChanged(@Nullable PagedList<Comment> previousList, @Nullable PagedList<Comment> currentList) {
                boolean empty = currentList.size() <= 0;
                handleEmpty(!empty);
            }
        };
        recyclerView.setAdapter(commentAdapter);

        viewModel.setItemId(mFeed.itemId);
        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                commentAdapter.submitList(comments);
                handleEmpty(comments.size() > 0);
            }
        });

        interactionBinding.inputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog();
            }
        });

    }

    private void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = CommentDialog.newInstance(mFeed.itemId);
        }
        commentDialog.setCommentAddListener(comment -> {
            handleEmpty(true);
            commentAdapter.addAndRefreshList(comment);
        });
        commentDialog.show(mActivity.getSupportFragmentManager(), "comment_dialog");
    }

    private EmptyView emptyView;

    protected void handleEmpty(boolean hasData) {
        if (hasData) {
            if (emptyView != null) {
                commentAdapter.removeHeaderView(emptyView);
            }
        } else {
            if (emptyView == null) {
                emptyView = new EmptyView(mActivity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = PixUtils.dp2px(40);
                emptyView.setLayoutParams(layoutParams);
                emptyView.setEmptyText("还没有评论，快来抢一楼吧");
            }
            commentAdapter.addHeaderView(emptyView);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (commentDialog != null && commentDialog.isAdded()) {
            commentDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onBackPressed() {

    }
}
