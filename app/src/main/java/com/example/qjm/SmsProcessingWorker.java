package com.example.qjm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.Gson;

// 明确导入所需类
import com.example.qjm.PickupInfo;
import com.example.qjm.PickupInfoDatabase;
import com.example.qjm.NotificationHelper;

public class SmsProcessingWorker extends Worker {
    private static final String TAG = "SmsProcessingWorker";
    private static final String PICKUP_INFO_KEY = "PICKUP_INFO_JSON";

    public SmsProcessingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // 从共享偏好设置中获取取件信息
            Context context = getApplicationContext();
            if (context == null) {
                Log.e(TAG, "ApplicationContext为null");
                return Result.failure();
            }
            
            android.content.SharedPreferences sharedPref = context.getSharedPreferences("PICKUP_INFO", Context.MODE_PRIVATE);
            if (sharedPref == null) {
                Log.e(TAG, "无法获取SharedPreferences");
                return Result.failure();
            }
            
            String pickupInfoJson = sharedPref.getString(PICKUP_INFO_KEY, null);

            if (pickupInfoJson != null && !pickupInfoJson.isEmpty()) {
                Log.d(TAG, "获取到取件信息JSON: " + pickupInfoJson);

                // 解析JSON为PickupInfo对象
                try {
                    PickupInfo pickupInfo = new Gson().fromJson(pickupInfoJson, PickupInfo.class);
                    if (pickupInfo == null) {
                        Log.e(TAG, "解析JSON结果为null");
                        return Result.failure();
                    }
                    
                    Log.d(TAG, "解析成功，取件码: " + pickupInfo.getCode());

                    // 保存到数据库
                    try {
                        PickupInfoDatabase db = PickupInfoDatabase.getInstance(context);
                        if (db != null) {
                            db.pickupInfoDao().insert(pickupInfo);
                            Log.d(TAG, "保存到数据库成功");
                        } else {
                            Log.e(TAG, "数据库实例为null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "保存到数据库失败", e);
                        // 数据库操作失败仍然返回成功，避免任务重试
                        return Result.success();
                    }

                    // 显示通知需要在主线程
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            NotificationHelper.showNotification(context, pickupInfo);
                            Log.d(TAG, "通知显示成功");
                        } catch (Exception e) {
                            Log.e(TAG, "显示通知失败", e);
                        }
                    });

                    // 发送广播通知主界面更新
                    try {
                        Intent updateIntent = new Intent("com.example.qjm.UPDATE_PICKUP_LIST");
                        updateIntent.setPackage(context.getPackageName()); // 限制广播范围
                        context.sendBroadcast(updateIntent);
                        Log.d(TAG, "广播发送成功");
                    } catch (Exception e) {
                        Log.e(TAG, "发送广播失败", e);
                    }

                    // 清理共享偏好设置
                    try {
                        sharedPref.edit().remove(PICKUP_INFO_KEY).apply();
                        Log.d(TAG, "共享偏好设置清理成功");
                    } catch (Exception e) {
                        Log.e(TAG, "清理共享偏好设置失败", e);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析JSON失败", e);
                    return Result.failure();
                }
            } else {
                Log.d(TAG, "未获取到取件信息JSON");
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "处理后台任务时发生异常", e);
            return Result.retry();
        }
    }
}