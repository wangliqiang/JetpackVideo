package com.app.jetpackvideo.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.databinding.ActivityFeedDetailTypeImageBinding;
import com.app.jetpackvideo.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.widget.CusImageView;

public class ImageViewHandler extends ViewHandler {

    private ActivityFeedDetailTypeImageBinding binding;
    private LayoutFeedDetailTypeImageHeaderBinding headerBinding;

    public ImageViewHandler(FragmentActivity mActivity) {
        super(mActivity);
        binding = DataBindingUtil.setContentView(mActivity, R.layout.activity_feed_detail_type_image);
        interactionBinding = binding.interactionLayout;
        recyclerView = binding.recyclerView;

        binding.back.setOnClickListener(v -> mActivity.finish());
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        binding.setFeed(mFeed);

        headerBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(LayoutInflater.from(mActivity), recyclerView, false);
        headerBinding.setFeed(mFeed);

        CusImageView headerImage = headerBinding.headerImage;
        headerImage.bindData(mFeed.width, mFeed.height, mFeed.width > mFeed.height ? 0 : 16, mFeed.cover);
        commentAdapter.addHeaderView(headerBinding.getRoot());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean visible = headerBinding.getRoot().getTop() <= -binding.titleLayout.getMeasuredHeight();
                binding.authorInfoLayout.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
                binding.title.setVisibility(visible ? View.GONE : View.VISIBLE);
            }

        });
        handleEmpty(false);
    }
}
