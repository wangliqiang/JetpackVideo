package com.app.jetpackvideo.ui.mine;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.InteractionPresenter;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.app.jetpackvideo.ui.home.FeedAdapter;
import com.app.jetpackvideo.utils.TimeUtils;

public class ProfileListAdapter extends FeedAdapter {

    public ProfileListAdapter(Context context, String category) {
        super(context, category);
    }

    @Override
    public int getItemViewTypeOfSub(int position) {
        if (TextUtils.equals(mCategory, ProfileActivity.TAB_TYPE_COMMENT)) {
            return R.layout.layout_feed_type_comment;
        } else if (TextUtils.equals(mCategory, ProfileActivity.TAB_TYPE_ALL)) {
            Feed item = getItem(position);
            if (item.topComment != null && item.topComment.userId == UserManager.get().getUserId()) {
                return R.layout.layout_feed_type_comment;
            }
        }
        return super.getItemViewTypeOfSub(position);
    }

    @Override
    public void onBindViewHolderOfSub(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolderOfSub(holder, position);
        View deleteView = holder.itemView.findViewById(R.id.feed_delete);
        TextView createTime = holder.itemView.findViewById(R.id.create_time);
        Feed item = getItem(position);
        createTime.setVisibility(View.VISIBLE);
        createTime.setText(TimeUtils.calculate(item.createTime));

        boolean isCommentTab = TextUtils.equals(mCategory, ProfileActivity.TAB_TYPE_COMMENT);
        deleteView.setVisibility(View.VISIBLE);
        deleteView.setOnClickListener(v -> {
            if (isCommentTab) {
                InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.topComment.commentId)
                        .observe((LifecycleOwner) mContext, success -> refreshList(item));
            } else {
                InteractionPresenter.deleteFeed(mContext, item.itemId)
                        .observe((LifecycleOwner) mContext, success -> {
                            refreshList(item);
                        });
            }
        });

    }

    private void refreshList(Feed item) {
        PagedList<Feed> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Feed> dataSource = new MutableItemKeyedDataSource<Integer, Feed>((ItemKeyedDataSource) currentList.getDataSource()) {

            @NonNull
            @Override
            public Integer getKey(@NonNull Feed item) {
                return item.id;
            }
        };
        for (Feed feed : currentList) {
            if (feed != item) {
                dataSource.data.add(feed);
            }
        }
        PagedList<Feed> pagedList = dataSource.buildNewPagedList(currentList.getConfig());
        submitList(pagedList);
    }
}
