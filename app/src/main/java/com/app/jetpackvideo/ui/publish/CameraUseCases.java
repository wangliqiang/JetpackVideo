package com.app.jetpackvideo.ui.publish;

import androidx.camera.core.AspectRatio;

public class CameraUseCases {

    private static double RATIO_4_3_VALUE = 4.0 / 3.0;
    private static double RATIO_16_9_VALUE = 16.0 / 9.0;

    public static int aspectRatio(int width, int height) {
        double previewRatio = Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
}
