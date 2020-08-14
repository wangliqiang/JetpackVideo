package com.app.jetpackvideo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * dispatchKeyEventPreIme 复写这个方法，可以在对话框弹窗中，
 * 监听backPress事件，以销毁对话框
 */
public class CusEditTextView extends AppCompatEditText {

    private onBackKeyEvent keyEvent;

    public CusEditTextView(Context context) {
        super(context);
    }

    public CusEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (keyEvent != null) {
                if (keyEvent.onKeyEvent()) {
                    return true;
                }
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public void setOnBackKeyEventListener(onBackKeyEvent event) {
        this.keyEvent = event;
    }

    public interface onBackKeyEvent {
        boolean onKeyEvent();
    }
}
