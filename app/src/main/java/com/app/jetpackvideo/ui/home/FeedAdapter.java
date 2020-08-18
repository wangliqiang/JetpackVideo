package com.app.jetpackvideo.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.BR;
import com.app.jetpackvideo.R;
import com.app.jetpackvideo.base.AbsPagedListAdapter;
import com.app.jetpackvideo.common.InteractionPresenter;
import com.app.jetpackvideo.databinding.LayoutFeedTypeImageBinding;
import com.app.jetpackvideo.databinding.LayoutFeedTypeVideoBinding;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.ui.detail.FeedDetailActivity;
import com.app.jetpackvideo.utils.LiveDataBus;
import com.app.jetpackvideo.widget.ListPlayerView;

public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.ViewHolder> {

    private LayoutInflater inflater;
    protected Context mContext;
    protected String mCategory;

    public FeedAdapter(Context context, String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        inflater = LayoutInflater.from(context);
        mContext = context;
        mCategory = category;
    }

    @Override
    public int getItemViewTypeOfSub(int position) {
        Feed feed = getItem(position);
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            return R.layout.layout_feed_type_image;
        } else if (feed.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolderOfSub(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolderOfSub(@NonNull FeedAdapter.ViewHolder holder, int position) {
        final Feed feed = getItem(position);
        holder.bindData(feed);

        holder.itemView.setOnClickListener(v -> {
            FeedDetailActivity.startFeedDetailActivity(mContext, feed, mCategory);
            onStartFeedDetailActivity(feed);
            if (mFeedObserver == null) {
                mFeedObserver = new FeedObserver();
                LiveDataBus.get()
                        .with(InteractionPresenter.DATA_FROM_INTERACTION)
                        .observe((LifecycleOwner) mContext, mFeedObserver);
            }
            mFeedObserver.setFeed(feed);
        });

    }

    public void onStartFeedDetailActivity(Feed feed) {
    }

    private FeedObserver mFeedObserver;

    private class FeedObserver implements Observer<Feed> {

        private Feed mFeed;

        @Override
        public void onChanged(Feed newOne) {
            if (mFeed.id != newOne.id) return;
            mFeed.author = newOne.author;
            mFeed.ugc = newOne.ugc;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) {
            mFeed = feed;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;
        public ImageView feedImage;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            mBinding.setVariable(com.app.jetpackvideo.BR.feed, item);
            mBinding.setVariable(BR.lifeCycleOwner, mContext);
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                feedImage = imageBinding.feedImage;
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
            } else if (mBinding instanceof LayoutFeedTypeVideoBinding) {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                listPlayerView = videoBinding.listPlayerView;
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }
}
