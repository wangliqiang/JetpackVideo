package com.app.jetpackvideo.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.jetpackvideo.R;

public class EmptyView extends LinearLayout {

    private ImageView emptyIcon;
    private TextView emptyText;
    private Button emptyBtn;

    public EmptyView(@NonNull Context context) {
        this(context, null);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int style) {
        super(context, attrs, defStyleAttr, style);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        LayoutInflater.from(context).inflate(R.layout.layout_empty_view, this, true);

        emptyIcon = findViewById(R.id.empty_icon);
        emptyText = findViewById(R.id.empty_text);
        emptyBtn = findViewById(R.id.empty_btn);
    }

    public void setEmptyIcon(@DrawableRes int iconRes) {
        emptyIcon.setImageResource(iconRes);
    }

    public void setEmptyText(String text) {
        if (TextUtils.isEmpty(text)) {
            emptyText.setVisibility(GONE);
        } else {
            emptyText.setText(text);
            emptyText.setVisibility(VISIBLE);
        }
    }

    public void setEmptyBtn(String text, View.OnClickListener listener) {
        if (TextUtils.isEmpty(text)) {
            emptyBtn.setVisibility(GONE);
        } else {
            emptyBtn.setText(text);
            emptyBtn.setVisibility(VISIBLE);
            emptyBtn.setOnClickListener(listener);
        }
    }
}
