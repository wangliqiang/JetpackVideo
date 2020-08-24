package com.app.jetpackvideo.ui.find;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.base.AbsPagedListAdapter;
import com.app.jetpackvideo.databinding.ActivityTagFeedListBinding;
import com.app.jetpackvideo.databinding.LayoutTagFeedListHeaderBinding;
import com.app.jetpackvideo.exoplayer.PageListPlayDetector;
import com.app.jetpackvideo.exoplayer.PageListPlayManager;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.model.TagList;
import com.app.jetpackvideo.ui.home.FeedAdapter;
import com.app.jetpackvideo.utils.PixUtils;
import com.app.jetpackvideo.utils.StatusBar;
import com.app.jetpackvideo.widget.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

public class TagFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnRefreshListener, OnLoadMoreListener {

    public static final String KEY_TAG_LIST = "tag_list";
    public static final String KEY_FEED_TYPE = "tag_feed_list";
    private ActivityTagFeedListBinding binding;
    private RecyclerView recyclerView;
    private EmptyView emptyView;
    private SmartRefreshLayout refreshLayout;
    private AbsPagedListAdapter adapter;
    private TagList tagList;
    private PageListPlayDetector playDetector;
    private boolean shouldPause = true;
    private TagFeedListViewModel tagFeedListViewModel;
    private int totalScrollY;

    public static void startActivity(Context context, TagList tagList) {
        Intent intent = new Intent(context, TagFeedListActivity.class);
        intent.putExtra(KEY_TAG_LIST, tagList);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tag_feed_list);

        recyclerView = binding.refreshLayout.recyclerView;
        emptyView = binding.refreshLayout.emptyView;
        refreshLayout = binding.refreshLayout.refreshLayout;
        binding.actionBack.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = (AbsPagedListAdapter) getAdapter();
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemAnimator(null);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);

        tagList = (TagList) getIntent().getSerializableExtra(KEY_TAG_LIST);
        binding.setTagList(tagList);
        binding.setOwner(this);

        tagFeedListViewModel = new ViewModelProvider(this).get(TagFeedListViewModel.class);
        tagFeedListViewModel.setFeedType(tagList.title);
        tagFeedListViewModel.getPageData().observe(this, feeds -> submitList(feeds));
        tagFeedListViewModel.getBoundaryPageData().observe(this, hasData -> finishRefresh(hasData));

        playDetector = new PageListPlayDetector(this, recyclerView);

        addHeaderView();

    }

    private void addHeaderView() {
        LayoutTagFeedListHeaderBinding headerBinding = LayoutTagFeedListHeaderBinding.inflate(LayoutInflater.from(this), recyclerView, false);
        headerBinding.setTagList(tagList);
        headerBinding.setOwner(this);
        adapter.addHeaderView(headerBinding.getRoot());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScrollY += dy;
                boolean overHeight = totalScrollY > PixUtils.dp2px(48);
                binding.tagLogo.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                binding.tagTitle.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                binding.topBarFollow.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                binding.actionBack.setImageResource(overHeight ? R.drawable.icon_back_black : R.drawable.icon_back_white);
                binding.topBar.setBackgroundColor(overHeight ? Color.WHITE : Color.TRANSPARENT);
                binding.topLine.setVisibility(overHeight ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            adapter.submitList(feeds);
        }
        finishRefresh(feeds.size() > 0);
    }

    private void finishRefresh(Boolean hasData) {
        PagedList currentList = adapter.getCurrentList();
        hasData = currentList != null && currentList.size() > 0 || hasData;
        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }

        RefreshState state = refreshLayout.getState();
        if (state.isOpening && state.isHeader) {
            refreshLayout.finishRefresh();
        } else {
            refreshLayout.finishLoadMore();
        }
    }

    private PagedListAdapter getAdapter() {
        return new FeedAdapter(this, KEY_FEED_TYPE) {
            @Override
            protected void onViewAttachedToWindowOfSub(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            protected void onViewDetachedFromWindowOfSub(ViewHolder holder) {
                playDetector.removeTarget(holder.getListPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        recyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shouldPause) {
            playDetector.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldPause = true;
        playDetector.onResume();
    }

    @Override
    protected void onDestroy() {
        PageListPlayManager.release(KEY_FEED_TYPE);
        super.onDestroy();
    }
}