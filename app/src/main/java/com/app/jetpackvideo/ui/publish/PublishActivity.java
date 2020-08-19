package com.app.jetpackvideo.ui.publish;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.common.UserManager;
import com.app.jetpackvideo.databinding.ActivityPublishBinding;
import com.app.jetpackvideo.model.Feed;
import com.app.jetpackvideo.model.TagList;
import com.app.jetpackvideo.utils.FileUtils;
import com.app.jetpackvideo.utils.StatusBar;
import com.app.jetpackvideo.widget.LoadingDialog;
import com.app.lib_nav_annotation.ActivityDestination;
import com.app.lib_network.ApiResponse;
import com.app.lib_network.ApiService;
import com.app.lib_network.JSONCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActivityDestination(pageUrl = "main/tab/publish")
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPublishBinding binding;
    private int width, height;
    private String filePath, coverFilePath;
    private boolean isVideo;
    private TagList mTagList;
    private UUID coverUploadUUID, fileUploadUUID;
    private String coverUploadUrl, fileUploadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_publish);

        binding.actionClose.setOnClickListener(this);
        binding.actionPublish.setOnClickListener(this);
        binding.actionDeleteFile.setOnClickListener(this);
        binding.actionAddTag.setOnClickListener(this);
        binding.actionAddFile.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.action_publish:
                publish();
                break;
            case R.id.action_add_tag:
                TagBottomSheetDialogFragment fragment = new TagBottomSheetDialogFragment();
                fragment.setOnTagItemSelectedListener(tagList -> {
                    mTagList = tagList;
                    binding.actionAddTag.setText(tagList.title);
                });
                fragment.show(getSupportFragmentManager(), "tag_dialog");
                break;
            case R.id.action_add_file:
                CaptureActivity.startActivityForResult(this);
                break;
            case R.id.action_delete_file:
                binding.actionAddFile.setVisibility(View.VISIBLE);
                binding.fileContainer.setVisibility(View.GONE);
                binding.cover.setImageDrawable(null);
                filePath = null;
                width = 0;
                height = 0;
                isVideo = false;
                break;
        }
    }

    private void publish() {
        showLoading();
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (!TextUtils.isEmpty(filePath)) {
            if (isVideo) {
                FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String coverPath) {
                        coverFilePath = coverPath;
                        OneTimeWorkRequest request = getOneTimeWorkRequest(coverPath);
                        coverUploadUUID = request.getId();
                        workRequests.add(request);
                        enqueue(workRequests);
                    }
                });
            }
            OneTimeWorkRequest request = getOneTimeWorkRequest(filePath);
            fileUploadUUID = request.getId();
            workRequests.add(request);
            //如果是视频文件则需要等待封面文件生成完毕后再一同提交到任务队列
            //否则 可以直接提交了
            if (!isVideo) {
                enqueue(workRequests);
            }
        } else {
            publishFeed();
        }
    }

    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation = WorkManager.getInstance(this).beginWith(workRequests);
        workContinuation.enqueue();

        workContinuation.getWorkInfosLiveData().observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                int completedCount = 0;
                int failedCount = 0;
                for (WorkInfo workInfo : workInfos) {
                    WorkInfo.State state = workInfo.getState();
                    Data outputData = workInfo.getOutputData();
                    UUID uuid = workInfo.getId();

                    if (state == WorkInfo.State.FAILED) {
                        if (uuid.equals(coverUploadUUID)) {
                            showToast("封面图上传失败");
                        } else if (uuid.equals(fileUploadUUID)) {
                            showToast("原始文件上传失败");
                        }
                        failedCount++;
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String fileUrl = outputData.getString("fileUrl");
                        if (uuid.equals(coverUploadUUID)) {
                            coverUploadUrl = fileUrl;
                        } else if (uuid.equals(fileUploadUUID)) {
                            fileUploadUrl = fileUrl;
                        }
                        completedCount++;
                    }
                }
                if (completedCount >= workInfos.size()) {
                    publishFeed();
                } else if (failedCount > 0) {
                    dismissLoading();
                }
            }
        });
    }

    private OneTimeWorkRequest getOneTimeWorkRequest(String filePath) {
        Data inputData = new Data.Builder()
                .putString("file", filePath)
                .build();

//        @SuppressLint("RestrictedApi") Constraints constraints = new Constraints();
//        //设备存储空间充足的时候 才能执行 ,>15%
//        constraints.setRequiresStorageNotLow(true);
//        //必须在执行的网络条件下才能好执行,不计流量 ,wifi
//        constraints.setRequiredNetworkType(NetworkType.UNMETERED);
//        //设备的充电量充足的才能执行 >15%
//        constraints.setRequiresBatteryNotLow(true);
//        //只有设备在充电的情况下 才能允许执行
//        constraints.setRequiresCharging(true);
//        //只有设备在空闲的情况下才能被执行 比如息屏，cpu利用率不高
//        constraints.setRequiresDeviceIdle(true);
//        //workmanager利用contentObserver监控传递进来的这个uri对应的内容是否发生变化,当且仅当它发生变化了
//        //我们的任务才会被触发执行，以下三个api是关联的
//        constraints.setContentUriTriggers(null);
//        //设置从content变化到被执行中间的延迟时间，如果在这期间。content发生了变化，延迟时间会被重新计算
        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerContentUpdateDelay(0);
//        //设置从content变化到被执行中间的最大延迟时间
        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerMaxContentDelay(0);

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                .setInputData(inputData)
//                .setConstraints(constraints)
//                //设置一个拦截器，在任务执行之前 可以做一次拦截，去修改入参的数据然后返回新的数据交由worker使用
//                .setInputMerger(null)
//                //当一个任务被调度失败后，所要采取的重试策略，可以通过BackoffPolicy来执行具体的策略
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
//                //任务被调度执行的延迟时间
//                .setInitialDelay(10, TimeUnit.SECONDS)
//                //设置该任务尝试执行的最大次数
//                .setInitialRunAttemptCount(2)
//                //设置这个任务开始执行的时间
//                //System.currentTimeMillis()
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
//                //指定该任务被调度的时间
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
//                //当一个任务执行状态编程finish时，又没有后续的观察者来消费这个结果，那么workamnager会在
//                //内存中保留一段时间的该任务的结果。超过这个时间，这个结果就会被存储到数据库中
//                //下次想要查询该任务的结果时，会触发workmanager的数据库查询操作，可以通过uuid来查询任务的状态
//                .keepResultsForAtLeast(10, TimeUnit.SECONDS)
                .build();
        return request;
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl", coverUploadUrl)
                .addParam("fileUrl", fileUploadUrl)
                .addParam("fileWidth", width)
                .addParam("fileHeight", height)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", mTagList == null ? 0 : mTagList.tagId)
                .addParam("tagTitle", mTagList == null ? "" : mTagList.title)
                .addParam("feedText", binding.inputView.getText().toString())
                .addParam("feedType", isVideo ? Feed.TYPE_VIDEO : Feed.TYPE_IMAGE_TEXT)
                .execute(new JSONCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse response) {
                        showToast("帖子发布成功，去沙发也看看吧");
                        finish();
                        dismissLoading();
                    }

                    @Override
                    public void onError(ApiResponse response) {
                        showToast(response.message);
                        dismissLoading();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CaptureActivity.REQ_CAPTURE && data != null) {
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        binding.actionAddFile.setVisibility(View.GONE);
        binding.fileContainer.setVisibility(View.VISIBLE);
        binding.cover.setImageUrl(filePath);
        binding.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        binding.cover.setOnClickListener(v -> PreviewActivity.startActivityForResult(PublishActivity.this, filePath, isVideo, null));
    }

    private LoadingDialog loadingDialog = null;

    private void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setLoadingText("正在发布...");
        }
        loadingDialog.show();
    }

    private void dismissLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        } else {
            runOnUiThread(() -> {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
            });
        }
    }

    private void showToast(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage("继续退出编辑的内容将会消失")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", (dialog, which) -> {
                    dialog.dismiss();
                    PublishActivity.this.finish();
                })
                .create()
                .show();
    }
}