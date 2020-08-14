package com.app.lib_network.cache;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.app.lib_network.common.AppGlobals;

import org.json.JSONObject;

@Database(entities = {Cache.class}, version = 1, exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {

    private static final CacheDatabase database;

    static {
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "jetpackvideo_cache")
                .allowMainThreadQueries() // 是否允许在主线程中查询
                //.addCallback() // 数据库创建和打开后的回调
                //.setQueryExecutor() // 设置线程池
                .build();
    }

    public abstract CacheDao getCache();

    public static CacheDatabase get() {
        return database;
    }

}
