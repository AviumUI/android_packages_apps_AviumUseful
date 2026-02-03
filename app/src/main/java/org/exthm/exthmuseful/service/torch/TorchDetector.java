/*
 * Copyright (C) 2025-2026 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exthm.exthmuseful.service.torch;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class TorchDetector {
    private static final String TAG = "TorchDetector";

    private final Context context;
    private CameraManager cameraManager;
    private String cameraId;
    private CameraManager.TorchCallback torchCallback;
    private final Handler handler;
    private final TorchStateListener listener;
    private boolean isDetecting = false;

    public TorchDetector(Context context, TorchStateListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public boolean startDetection() {
        if (isDetecting) {
            Log.w(TAG, "手电筒检测已启动。");
            return true;
        }

        // 检查 CAMERA 权限
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "未授予 CAMERA 权限，无法启动检测。");
            return false;
        }

        // 检查设备是否有闪光灯
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(TAG, "设备没有闪光灯功能。");
            return false;
        }

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            Log.e(TAG, "无法获取 CameraManager。");
            return false;
        }

        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                Log.e(TAG, "未找到摄像头。");
                return false;
            }
            // 查找合适的 cameraId (通常是后置带闪光灯的)
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (hasFlash != null && hasFlash) {
                    if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        this.cameraId = id; // 优先选择后置摄像头
                        break;
                    }
                    if (this.cameraId == null) { // 如果没有后置，则选择第一个带闪光灯的
                        this.cameraId = id;
                    }
                }
            }

            if (this.cameraId == null) {
                Log.e(TAG, "未找到带闪光灯的摄像头。");
                return false;
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "访问摄像头列表或特性时出错", e);
            return false;
        }

        torchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeUnavailable(@NonNull String cameraId) {
                super.onTorchModeUnavailable(cameraId);
                if (TorchDetector.this.cameraId != null && TorchDetector.this.cameraId.equals(cameraId)) {
                    Log.w(TAG, "摄像头 " + cameraId + " 的手电筒模式不可用。");
                    if (listener != null) {
                        listener.onTorchUnavailable(cameraId);
                    }
                }
            }

            @Override
            public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                if (TorchDetector.this.cameraId != null && TorchDetector.this.cameraId.equals(cameraId)) {
                    Log.i(TAG, "摄像头 " + cameraId + " 的手电筒模式已更改: " + (enabled ? "开启" : "关闭"));
                    if (listener != null) {
                        listener.onTorchStateChanged(enabled);
                    }
                }
            }
        };

        cameraManager.registerTorchCallback(torchCallback, handler);
        isDetecting = true;
        Log.i(TAG, "已为摄像头 " + cameraId + " 注册 TorchCallback。");
        return true;
    }

    public void stopDetection() {
        if (!isDetecting) {
            Log.w(TAG, "检测未激活或已停止。");
            return;
        }
        if (cameraManager != null && torchCallback != null) {
            try {
                cameraManager.unregisterTorchCallback(torchCallback);
                Log.i(TAG, "已反注册 TorchCallback。");
            } catch (Exception e) { // CameraAccessException 或 IllegalArgumentException
                Log.e(TAG, "反注册 TorchCallback 时出错", e);
            }
        }
        torchCallback = null;
        isDetecting = false;
    }

    public boolean isDetecting() {
        return isDetecting;
    }

    public String getCameraId() {
        return cameraId;
    }

    // 控制手电筒的方法
    public void setTorchMode(boolean enable) {
        if (cameraManager == null || cameraId == null) {
            Log.e(TAG, "CameraManager 或 CameraId 未初始化，无法设置手电筒模式。");
            // 尝试重新初始化（简化版，仅用于直接控制）
            if (!isDetecting) {
                if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return;
                cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                if (cameraManager == null) return;
                try {
                    for (String id : cameraManager.getCameraIdList()) {
                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                        Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if (hasFlash != null && hasFlash) {
                            this.cameraId = id;
                            break;
                        }
                    }
                } catch (CameraAccessException e) {
                    Log.e(TAG, "为 setTorchMode 重新初始化时出错", e);
                    return;
                }
                if (this.cameraId == null) {
                    Log.e(TAG, "为 setTorchMode 未找到带闪光灯的摄像头。");
                    return;
                }
            } else if (this.cameraId == null) {
                Log.e(TAG, "即使在检测期间 CameraId 也为 null，无法设置手电筒模式。");
                return;
            }
        }


        try {
            cameraManager.setTorchMode(cameraId, enable);
            Log.d(TAG, "已将摄像头 " + cameraId + " 的手电筒模式设置为 " + enable);
        } catch (CameraAccessException e) {
            Log.e(TAG, "无法为摄像头 " + cameraId + " 设置手电筒模式", e);
        } catch (IllegalArgumentException e) { // 例如 cameraId 不再有效
            Log.e(TAG, "设置手电筒模式时出错 (可能 cameraId 不再有效): " + cameraId, e);
        }
    }
}