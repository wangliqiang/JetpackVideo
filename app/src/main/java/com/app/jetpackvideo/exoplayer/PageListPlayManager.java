package com.app.jetpackvideo.exoplayer;

import android.app.Application;
import android.net.Uri;

import com.app.jetpackvideo.utils.AppGlobals;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;

public class PageListPlayManager {

    private static HashMap<String, PageListPlay> sPageListPlayHashMap = new HashMap<>();
    private static final ProgressiveMediaSource.Factory mediaSourceFactory;

    static {
        Application application = AppGlobals.getApplication();
        //创建http视频资源加载的工厂对象
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(application, application.getPackageName()));
        // 创建缓存，指定缓存位置和缓存策略，为最近最少使用原则，最大为200M
        Cache cache = new SimpleCache(application.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200));
        // 把缓存对象cache和负责缓存数据读取、写入的工厂类关联
        CacheDataSinkFactory cacheDataSinkFactory = new CacheDataSinkFactory(cache, Long.MAX_VALUE);

        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(cache,
                dataSourceFactory,
                new FileDataSource.Factory(),
                cacheDataSinkFactory,
                CacheDataSource.FLAG_BLOCK_ON_CACHE,
                null);
        // 创建一个MediaSource媒体资源加载的工厂类
        // 创建的MediaSource能够实现边缓冲边播放的效果
        // 如果需要播放hls，m3u8，则需要创建DashMediaSource.Factory()
        mediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    public static MediaSource createMediaSource(String url) {
        return mediaSourceFactory.createMediaSource(Uri.parse(url));
    }

    public static PageListPlay get(String pageName) {
        PageListPlay pageListPlay = sPageListPlayHashMap.get(pageName);
        if (pageListPlay == null) {
            pageListPlay = new PageListPlay();
            sPageListPlayHashMap.put(pageName, pageListPlay);
        }
        return pageListPlay;
    }

    public static void release(String pageName) {
        PageListPlay pageListPlay = sPageListPlayHashMap.remove(pageName);
        if (pageListPlay != null) {
            pageListPlay.release();
        }
    }
}
