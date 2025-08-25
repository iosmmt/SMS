package com.example.qjm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionActivity extends AppCompatActivity {
    private Button grantPermissionButton;
    private Button skipButton;
    private TextView permissionDescriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // 初始化视图
        grantPermissionButton = findViewById(R.id.grantPermissionButton);
        skipButton = findViewById(R.id.skipButton);
        permissionDescriptionText = findViewById(R.id.permissionDescriptionText);

        // 设置权限说明文本
        String permissionDescription = "取件码助手需要以下权限才能正常工作：\n\n" +
                "1. 短信读取权限：用于自动读取包含取件码的短信\n" +
                "2. 短信接收权限：用于实时接收新短信\n" +
                "3. 通知权限：用于发送取件提醒通知\n\n" +
                "请授予这些权限以确保应用能正常工作。";
        permissionDescriptionText.setText(permissionDescription);

        // 设置授予权限按钮监听
        grantPermissionButton.setOnClickListener(v -> {
            // 跳转到应用设置页面
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        // 设置跳过按钮监听
        skipButton.setOnClickListener(v -> {
            // 返回主界面
            Intent mainIntent = new Intent(PermissionActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }
}