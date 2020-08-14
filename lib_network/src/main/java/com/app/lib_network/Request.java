package com.app.lib_network;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.app.lib_network.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class Request<T, R extends Request> implements Cloneable {
    private static final String TAG = "Request";
    protected String mUrl;
    protected HashMap<String, String> mHeaders = new HashMap<>();
    protected HashMap<String, Object> mParams = new HashMap<>();

    // 仅访问本地缓存，几遍本地缓存不存在，也不会发起网络请求
    public static final int CACHE_ONLY = 1;
    // 先访问缓存，同事发起网络请求，成功后缓存到本地
    public static final int CACHE_FIRST = 2;
    // 仅访问服务器，不存储到本地
    public static final int NET_ONLY = 3;
    // 先访问网络，成功后缓存到本地
    public static final int NET_CACHE = 4;
    private String cacheKey; // 缓存Key
    private int cacheStrategy = NET_ONLY; // 缓存策略

    private Type mType;
    private Class mClazz;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {

    }

    public Request(String url) {
        this.mUrl = url;
    }

    public R addHeader(String key, String value) {
        mHeaders.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }
        try {
            if (value.getClass() == String.class) {
                mParams.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");
                Class claz = (Class) field.get(null);
                if (claz.isPrimitive()) {
                    mParams.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseType(Class clazz) {
        mClazz = clazz;
        return (R) this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
        return (R) this;
    }

    /**
     * 异步请求
     *
     * @param callback
     */
    @SuppressLint("RestrictedApi")
    public void execute(final JSONCallback<T> callback) {

        if (cacheStrategy != NET_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
                ApiResponse<T> response = readCache();
                if (callback != null) {
                    callback.onCacheSuccess(response);
                }
            });
        }

        if (cacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> apiResponse = new ApiResponse<>();
                    apiResponse.message = e.getMessage();
                    callback.onError(apiResponse);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response, callback);
                    if (apiResponse.success) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse);
                    }
                }
            });
        }
    }

    /**
     * 同步请求
     *
     * @return
     */
    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步方法，response 返回值类型必须设置");
        }

        if (cacheStrategy == CACHE_ONLY) {
            return readCache();
        }

        ApiResponse<T> result = null;
        try {
            Response response = getCall().execute();
            result = parseResponse(response, null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new ApiResponse<>();
            result.message = e.getMessage();
        }
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JSONCallback callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) convert.convert(content, mType);
                } else if (mClazz != null) {
                    result.body = (T) convert.convert(content, mClazz);
                } else {
                    Log.e(TAG, "parseResponse: 无法解析");
                }
            } else {
                message = content;
                success = false;
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            status = 0;
        }
        result.success = success;
        result.status = status;
        result.message = message;

        if (cacheStrategy != NET_ONLY && result.success && result.body != null && result.body instanceof Serializable) {
            saveCache(result.body);
        }

        return result;
    }

    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存读取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        cacheKey = GetRequest.URLCreator.createUrlFromParams(mUrl, mParams);
        return cacheKey;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request<T, R>) super.clone();
    }
}
