package com.app.jetpackvideo.ui.home;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.app.jetpackvideo.base.AbsViewModel;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.ui.MuteablePageKeyedDataSource;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;
import com.app.lib_network.JSONCallback;
import com.app.lib_network.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean withCache = false;
    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();
    // 上拉加载同步为标记
    private AtomicBoolean loadAfter = new AtomicBoolean(false);
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return new FeedDataSource();
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    class FeedDataSource extends ItemKeyedDataSource<Integer, Feed> {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            // 初始化
            loadData(0, params.requestedLoadSize, callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            // 加载分页数据
            loadData(params.key, params.requestedLoadSize, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            // 向前加载数据
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }

    private void loadData(int key, int count, ItemKeyedDataSource.LoadCallback<Feed> callback) {

        if (key > 0) {
            loadAfter.set(true);
        }
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("userId", 0)
                .addParam("feedId", key)
                .addParam("pageCount", count)
                .addParam("feedType", mFeedType)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());
        if (withCache) {
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JSONCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    MuteablePageKeyedDataSource dataSource = new MuteablePageKeyedDataSource<Integer, Feed>();
                    dataSource.data.addAll(response.body);
                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    cacheLiveData.postValue(pagedList);
                }
            });
        }

        try {
            Request netRequest = withCache ? request.clone() : request;
            netRequest.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(data);

            if (key > 0) {
                // 通过Livedata发送数据，告诉UI层 是否应该主动关闭上拉加载分页动画
                getBoundaryPageData().postValue(data.size() > 0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id, config.pageSize, callback);
            }
        });
    }

}