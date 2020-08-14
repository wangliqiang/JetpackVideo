package com.app.jetpackvideo.ui.publish;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.app.jetpackvideo.R;
import com.app.lib_nav_annotation.ActivityDestination;

@ActivityDestination(pageUrl = "main/tab/publish")
public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
    }
}