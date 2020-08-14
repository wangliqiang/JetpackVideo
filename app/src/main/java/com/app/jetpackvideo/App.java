package com.app.jetpackvideo;

import android.app.Application;

import com.app.lib_network.ApiService;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://123.56.232.18:8080/serverdemo", null);
    }
}
