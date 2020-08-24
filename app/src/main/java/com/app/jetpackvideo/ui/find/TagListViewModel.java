package com.app.jetpackvideo.ui.find;

import android.annotation.SuppressLint;
import android.nfc.Tag;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.app.jetpackvideo.base.AbsViewModel;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.model.TagList;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagListViewModel extends AbsViewModel<TagList> {
    private String tagType;
    private int offset;
    private AtomicBoolean loadAfter = new AtomicBoolean();
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    private class DataSource extends ItemKeyedDataSource<Long, TagList> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(0L, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        private void loadData(Long requestKey, LoadCallback<TagList> callback) {
            if (requestKey > 0) {
                loadAfter.set(true);
            }

            ApiResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("tagId", requestKey)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset)
                    .responseType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .execute();

            List<TagList> result = response.body == null ? Collections.emptyList() : response.body;

            callback.onResult(result);
            if (requestKey > 0) {
                loadAfter.set(false);
                offset += result.size();
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            } else {
                offset = result.size();
            }

        }

        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            return item.tagId;
        }
    }

    @SuppressLint("RestrictedApi")
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback) {
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ((TagListViewModel.DataSource) getDataSource()).loadData(tagId, callback);
            }
        });
    }
}
