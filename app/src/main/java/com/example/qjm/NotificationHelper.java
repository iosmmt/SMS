package com.example.qjm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "PICKUP_CODE_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    // 初始化通知渠道
    public static void createNotificationChannel(Context context) {
        try {
            if (context == null) {
                Log.e(TAG, "Context为null，无法创建通知渠道");
                return;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "取件码通知";
                String description = "新的取件码通知";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                } else {
                    Log.e(TAG, "无法获取NotificationManager");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "创建通知渠道时发生异常", e);
        }
    }

    // 显示通知
    public static void showNotification(Context context, PickupInfo pickupInfo) {
        try {
            if (context == null || pickupInfo == null) {
                Log.e(TAG, "Context或PickupInfo为null，无法显示通知");
                return;
            }
            
            // 检查是否有通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有权限，不显示通知
                    Log.w(TAG, "没有通知权限，无法显示通知");
                    return;
                }
            }

            // 创建通知渠道（如果需要）
            createNotificationChannel(context);

            // 创建意图，点击通知后打开应用
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // 构建通知内容
            String contentText = String.format("%s 取件码: %s\n%s %s",
                    pickupInfo.getCompany() != null ? pickupInfo.getCompany() : "未知快递",
                    pickupInfo.getCode() != null ? pickupInfo.getCode() : "未知取件码",
                    pickupInfo.getStation() != null ? pickupInfo.getStation() : "未知驿站",
                    pickupInfo.getAddress() != null ? pickupInfo.getAddress() : "未知地址");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("新的取件码")
                    .setContentText(contentText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // 显示通知
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            } else {
                Log.e(TAG, "无法获取NotificationManagerCompat");
            }
        } catch (Exception e) {
            Log.e(TAG, "显示通知时发生异常", e);
        }
    }
}