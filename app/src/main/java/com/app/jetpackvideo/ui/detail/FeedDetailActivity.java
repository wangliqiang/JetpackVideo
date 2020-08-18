package com.app.jetpackvideo.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.utils.StatusBar;

public class FeedDetailActivity extends AppCompatActivity {

    protected static final String KEY_FEED = "KEY_FEED";
    protected static final String KEY_CATEGORY = "KEY_CATEGORY";

    private ViewHandler viewHandler = null;

    public static void startFeedDetailActivity(Context context, Feed item, String category) {
        Intent intent = new Intent(context,FeedDetailActivity.class);
        intent.putExtra(KEY_FEED, item);
        intent.putExtra(KEY_CATEGORY, category);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        Feed feed = (Feed) getIntent().getSerializableExtra(KEY_FEED);
        if (feed == null) {
            finish();
            return;
        }
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }

        viewHandler.bindInitData(feed);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(viewHandler != null){
            viewHandler.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewHandler != null) {
            viewHandler.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewHandler != null) {
            viewHandler.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewHandler != null) {
            viewHandler.onBackPressed();
        }
        super.onBackPressed();
    }
}