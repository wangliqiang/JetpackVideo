package com.app.jetpackvideo.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.app.jetpackvideo.R;
import com.app.jetpackvideo.databinding.ActivityLayoutCaptureBinding;
import com.app.jetpackvideo.ui.detail.RecordView;
import com.app.jetpackvideo.utils.PixUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {

    private ActivityLayoutCaptureBinding binding;

    public static final int REQ_CAPTURE = 10001;
    private ArrayList<String> deniedPermission = new ArrayList<>();
    private static final int PERMISSION_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";

    private boolean takingPicture;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private Size resolution = new Size(1280, 720);
    private ImageCapture imageCapture;
    private PreviewView textureView;
    private VideoCapture videoCapture;
    private String outputFilePath;

    private ExecutorService cameraExecutor;

    public static void startActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_layout_capture);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        textureView = binding.textureView;

        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.recordView.setOnRecordListener(new RecordView.onRecordListener() {

            @Override
            public void onClick() {
                takingPicture = true;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                binding.captureTips.setVisibility(View.INVISIBLE);

                ImageCapture.Metadata metadata = new ImageCapture.Metadata();
                metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_BACK);
                ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file)
                        .setMetadata(metadata).build();

                imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.e("onImageSaved", "不知道为啥是null: " + outputFileResults.getSavedUri());
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showErrorToast(exception.getMessage());
                    }
                });

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".mp4");
                videoCapture.startRecording(file, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });

        binding.actionClose.setOnClickListener(v -> finish());
    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        String mineType = takingPicture ? "image/jpeg" : "video/mp4";
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mineType}, null);
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }

    private void bindCameraX() {
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                if (CameraUseCases.hasBackCamera(cameraProvider)) {
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                } else if (CameraUseCases.hasFrontCamera(cameraProvider)) {
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
                }
                bindPreview(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private void bindPreview(ProcessCameraProvider cameraProvider) {

        int screenAspectRatio = CameraUseCases.aspectRatio(PixUtils.getScreenWidth(), PixUtils.getScreenHeight());
        int rotation = textureView.getDisplay().getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        Preview preview = new Preview.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation
                .setTargetRotation(rotation)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // We request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits our use cases
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(resolution)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        videoCapture = new VideoCapture.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .setVideoFrameRate(25) // 视频帧率
                .setBitRate(3 * 1024 * 1024) // bit率
                .build();

        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview, imageCapture, videoCapture);

        preview.setSurfaceProvider(textureView.createSurfaceProvider());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
            intent.putExtra(RESULT_FILE_WIDTH, resolution.getWidth());
            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getHeight());
            intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            deniedPermission.clear();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermission.add(permission);
                }
            }

            if (deniedPermission.isEmpty()) {
                bindCameraX();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("必须的权限没有得到授权，功能将无法使用，请重新授权")
                        .setNegativeButton("不", (dialog, which) -> {
                            dialog.dismiss();
                            CaptureActivity.this.finish();
                        })
                        .setPositiveButton("好的", (dialog, which) -> {
                            String[] denied = new String[deniedPermission.size()];
                            ActivityCompat.requestPermissions(CaptureActivity.this, deniedPermission.toArray(denied), PERMISSION_CODE);
                        }).create().show();
            }
        }
    }

    private void showErrorToast(@NonNull String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        cameraExecutor.shutdown();
        super.onDestroy();
    }
}