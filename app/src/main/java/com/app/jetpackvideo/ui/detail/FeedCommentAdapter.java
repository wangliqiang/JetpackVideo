package com.app.jetpackvideo.ui.detail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.base.AbsPagedListAdapter;
import com.app.jetpackvideo.common.InteractionPresenter;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.databinding.LayoutFeedCommentListItemBinding;
import com.app.jetpackvideo.model.Comment;
import com.app.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.app.jetpackvideo.utils.PixUtils;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {


    private Context context;
    private LayoutInflater inflater;

    public FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolderOfSub(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolderOfSub(ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        holder.mBinding.commentDelete.setOnClickListener(v -> {
            InteractionPresenter.deleteFeedComment(context, item.itemId, item.commentId)
                    .observe((LifecycleOwner) context, success -> {
                        if (success) {
                            deleteAndRefreshList(item);
                        }
                    });
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            boolean self = item.author == null ? false : UserManager.get().getUserId() == item.author.userId;

            mBinding.labelAuthor.setVisibility(self ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(self ? View.VISIBLE : View.GONE);

            if (!TextUtils.isEmpty(item.imageUrl)) {
                mBinding.commentExt.setVisibility(View.VISIBLE);
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(item.width, item.height, 0, PixUtils.dp2px(200), PixUtils.dp2px(200), item.imageUrl);
                if (!TextUtils.isEmpty(item.videoUrl)) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
                mBinding.commentExt.setVisibility(View.GONE);
            }
        }
    }

    public void addAndRefreshList(Comment comment) {
        MutableItemKeyedDataSource<Integer, Comment> mutableItemKeyedDataSource =
                new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
                    @NonNull
                    @Override
                    public Integer getKey(@NonNull Comment item) {
                        return item.id;
                    }
                };
        mutableItemKeyedDataSource.data.add(comment);
        mutableItemKeyedDataSource.data.addAll(getCurrentList());
        PagedList<Comment> pagedList = mutableItemKeyedDataSource.buildNewPagedList(getCurrentList().getConfig());
        submitList(pagedList);
    }

    public void deleteAndRefreshList(Comment item) {
        MutableItemKeyedDataSource<Integer, Comment> dataSource =
                new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
                    @NonNull
                    @Override
                    public Integer getKey(@NonNull Comment item) {
                        return item.id;
                    }
                };
        PagedList<Comment> currentList = getCurrentList();
        for (Comment comment : currentList) {
            if (comment != item) {
                dataSource.data.add(comment);
            }
        }
        PagedList<Comment> pagedList = dataSource.buildNewPagedList(getCurrentList().getConfig());
        submitList(pagedList);
    }
}
