package com.app.jetpackvideo.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.Observer;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.databinding.LayoutCommentDialogBinding;
import com.app.jetpackvideo.model.Comment;
import com.app.jetpackvideo.ui.publish.CaptureActivity;
import com.app.jetpackvideo.utils.AppGlobals;
import com.app.jetpackvideo.utils.FileUploadManager;
import com.app.jetpackvideo.utils.FileUtils;
import com.app.jetpackvideo.utils.PixUtils;
import com.app.jetpackvideo.widget.LoadingDialog;
import com.app.jetpackvideo.widget.ViewHelper;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;
import com.app.lib_network.JSONCallback;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("RestrictedApi")
public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private LayoutCommentDialogBinding binding;
    private long itemId;
    private static final String KEY_ITEM_ID = "key_item_id";
    private commentAddListener mListener;
    private String filePath;
    private int width, height;
    private boolean isVideo;
    private LoadingDialog loadingDialog;
    private String coverUrl;
    private String fileUrl;

    public static CommentDialog newInstance(long itemId) {
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(0);

        binding = LayoutCommentDialogBinding.inflate(inflater, ((ViewGroup) window.findViewById(android.R.id.content)), false);
        binding.commentVideo.setOnClickListener(this);
        binding.commentDelete.setOnClickListener(this);
        binding.commentSend.setOnClickListener(this);


        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        this.itemId = getArguments().getLong(KEY_ITEM_ID);

        ViewHelper.setViewOutline(binding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);

        binding.getRoot().post(() -> showSoftInputMethod());

        dismissWhenPressBack();
        return binding.getRoot();
    }

    private void showSoftInputMethod() {
        binding.inputView.setFocusable(true);
        binding.inputView.setFocusableInTouchMode(true);
        // 请求获得焦点
        binding.inputView.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(binding.inputView, 0);

    }

    private void dismissWhenPressBack() {
        binding.inputView.setOnBackKeyEventListener(() -> {
            binding.inputView.postDelayed(() -> dismiss(), 200);
            return true;
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_send:
                publishComment();
                break;
            case R.id.comment_video:
                CaptureActivity.startActivityForResult(getActivity());
                break;
            case R.id.comment_delete:
                filePath = null;
                isVideo = false;
                width = 0;
                height = 0;
                binding.commentCover.setImageDrawable(null);
                binding.commentExtLayout.setVisibility(View.GONE);

                binding.commentVideo.setEnabled(true);
                binding.commentVideo.setImageAlpha(255);
                break;
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(binding.inputView.getText())) {
            return;
        }
        if (isVideo && !TextUtils.isEmpty(filePath)) {
            FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFile(coverPath, filePath);
                }
            });
        } else if (!TextUtils.isEmpty(filePath)) {
            uploadFile(null, filePath);
        } else {
            publish();
        }
    }

    private void uploadFile(String coverPath, String filePath) {
        showLoadingDialog();
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    int remain = count.decrementAndGet();
                    coverUrl = FileUploadManager.upload(coverPath);
                    if (remain <= 0) {
                        if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                            publish();
                        } else {
                            dismissLoadingDialog();
                            showToast("文上传失败,请重新发布");
                        }
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int remain = count.decrementAndGet();
                fileUrl = FileUploadManager.upload(filePath);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        showToast("文上传失败,请重新发布");
                    }
                }
            }
        });
    }

    private void publish() {
        String commentText = binding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("image_url", isVideo ? coverUrl : fileUrl)
                .addParam("video_url", isVideo ? fileUrl : null)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new JSONCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                    }

                    @Override
                    public void onError(ApiResponse response) {
                        showToast("评论失败：" + response.message);
                    }
                });
    }

    private void onCommentSuccess(Comment body) {
        showToast("评论发布成功！");
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            if (mListener != null) {
                mListener.onAddComment(body);
            }
            dismiss();
        });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
            loadingDialog.setLoadingText("正在发布...");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                    }
                });
            } else if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismissLoadingDialog();
        filePath = null;
        fileUrl = null;
        coverUrl = null;
        isVideo = false;
        width = 0;
        height = 0;
    }


    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(commentAddListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK) {
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            if (!TextUtils.isEmpty(filePath)) {
                binding.commentExtLayout.setVisibility(View.VISIBLE);
                binding.commentCover.setImageUrl(filePath);
                if (isVideo) {
                    binding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }
        }
        binding.commentVideo.setEnabled(false);
        binding.commentVideo.setImageAlpha(80);

    }

}
