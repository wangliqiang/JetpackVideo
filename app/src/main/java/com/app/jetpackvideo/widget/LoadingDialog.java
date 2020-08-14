package com.app.jetpackvideo.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.utils.PixUtils;

public class LoadingDialog extends AlertDialog {
    private TextView loadingText;

    public LoadingDialog(Context context) {
        super(context);
    }

    protected LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setLoadingText(String loadingText) {
        if (this.loadingText != null) {
            this.loadingText.setText(loadingText);
        }
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.layout_loading_view);
        loadingText = findViewById(R.id.loading_text);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        attributes.dimAmount = 0.5f;
        // 必须设置透明，否则对话框的宽度很宽
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置圆角
        ViewHelper.setViewOutline(findViewById(R.id.loading_layout), PixUtils.dp2px(10), ViewHelper.RADIUS_ALL);
        window.setAttributes(attributes);
    }
}
