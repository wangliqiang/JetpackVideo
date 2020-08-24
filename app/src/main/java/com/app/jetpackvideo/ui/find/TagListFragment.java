package com.app.jetpackvideo.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.app.jetpackvideo.base.AbsListFragment;
import com.app.jetpackvideo.model.TagList;
import com.app.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

public class TagListFragment extends AbsListFragment<TagList, TagListViewModel> {

    public static final String KEY_TAG_TYPE = "tag_type";
    private String tagType;

    public static TagListFragment newInstance(String tagType) {

        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        TagListFragment fragment = new TagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            emptyView.setEmptyText("还没关注任何数据，去推荐页面看看吧~");
            emptyView.setEmptyBtn("看看推荐", v -> viewModel.getSwitchTabLiveData().setValue(new Object()));
        }
        recyclerView.removeItemDecorationAt(0);
        viewModel.setTagType(tagType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        tagType = getArguments().getString(KEY_TAG_TYPE);
        TagListAdapter tagListAdapter = new TagListAdapter(getContext());
        return tagListAdapter;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<TagList> currentList = getAdapter().getCurrentList();
        long tagId = currentList == null ? 0 : currentList.get(currentList.size() - 1).tagId;

        viewModel.loadData(tagId, new ItemKeyedDataSource.LoadCallback() {
            @Override
            public void onResult(@NonNull List data) {
                if (data != null && data.size() > 0) {
                    MutableItemKeyedDataSource<Long, TagList> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Long, TagList>((ItemKeyedDataSource) viewModel.getDataSource()) {

                        @NonNull
                        @Override
                        public Long getKey(@NonNull TagList item) {
                            return item.tagId;
                        }
                    };
                    mutableItemKeyedDataSource.data.addAll(currentList);
                    mutableItemKeyedDataSource.data.addAll(data);
                    PagedList<TagList> pagedList = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
                    submitList(pagedList);
                } else {
                    finishRefresh(false);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        viewModel.getDataSource().invalidate();
    }
}