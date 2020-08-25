package com.app.jetpackvideo.ui.mine;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.app.jetpackvideo.base.AbsViewModel;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.model.Feed;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileViewModel extends AbsViewModel<Feed> {
    private String profileType;

    public void setProfileType(String tabType) {
        this.profileType = tabType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        private void loadData(Integer key, LoadCallback<Feed> callback) {
            ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryProfileFeeds")
                    .addParam("feedId", key)
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", 10)
                    .addParam("profileType", profileType)
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();

            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
            if (key > 0) {
                getBoundaryPageData().postValue(result.size() > 0);
            }
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }
}