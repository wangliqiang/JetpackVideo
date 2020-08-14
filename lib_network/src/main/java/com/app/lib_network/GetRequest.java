package com.app.lib_network;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class GetRequest<T> extends Request<T, GetRequest> {


    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        okhttp3.Request request = builder.get().url(URLCreator.createUrlFromParams(mUrl, mParams)).build();
        return request;
    }


    static class URLCreator {
        public static String createUrlFromParams(String url, Map<String, Object> params) {
            StringBuilder builder = new StringBuilder();
            builder.append(url);
            if (url.indexOf("?") > 0 || url.indexOf("&") > 0) {
                builder.append("&");
            } else {
                builder.append("?");
            }

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                try {
                    String value = URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8");
                    builder.append(entry.getKey()).append("=").append(value).append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }

    }

}
