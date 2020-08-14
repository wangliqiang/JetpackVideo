package com.app.jetpackvideo.model;

import java.util.List;

public class BottomBar {

    /**
     * activeColor : #333333
     * inActiveColor : #666666
     * tabs : [{"size":24,"enable":true,"index":0,"pageUrl":"main/tab/home","title":"首页"},{"size":24,"enable":true,"index":1,"pageUrl":"main/tab/home","title":"发现"},{"size":40,"enable":true,"index":2,"tintColor":"#FFDF09","pageUrl":"main/tab/publish","title":""},{"size":24,"enable":true,"index":3,"pageUrl":"main/tab/notifications","title":"通知"},{"size":24,"enable":true,"index":4,"pageUrl":"main/tab/mine","title":"我的"}]
     */

    public String activeColor;
    public String inActiveColor;
    public List<Tabs> tabs;
    public int selectTab;//底部导航栏默认选中项

    public static class Tabs {
        /**
         * size : 24
         * enable : true
         * index : 0
         * pageUrl : main/tab/home
         * title : 首页
         * tintColor : #ff678f
         */

        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;
        public String tintColor;
    }
}
