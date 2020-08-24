package com.app.jetpackvideo.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.base.AbsPagedListAdapter;
import com.app.jetpackvideo.common.InteractionPresenter;
import com.app.jetpackvideo.databinding.LayoutTagListItemBinding;
import com.app.jetpackvideo.model.TagList;

public class TagListAdapter extends AbsPagedListAdapter<TagList, TagListAdapter.ViewHolder> {


    private LayoutInflater inflater;
    private Context context;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId == newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolderOfSub(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding itemBinding = LayoutTagListItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(itemBinding.getRoot(), itemBinding);
    }

    @Override
    protected void onBindViewHolderOfSub(ViewHolder holder, int position) {
        TagList item = getItem(position);
        holder.bindData(item);
        holder.binding.actionFollow.setOnClickListener(v -> InteractionPresenter.toggleTagLike((LifecycleOwner) context, item));
        holder.itemView.setOnClickListener(v -> TagFeedListActivity.startActivity(context, item));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutTagListItemBinding binding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding itemBinding) {
            super(itemView);
            binding = itemBinding;
        }

        public void bindData(TagList item) {
            binding.setTagList(item);
        }
    }
}
