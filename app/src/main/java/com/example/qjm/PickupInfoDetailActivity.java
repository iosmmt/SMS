package com.example.qjm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class PickupInfoDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PICKUP_INFO_ID = "pickup_info_id";
    
    private PickupInfo pickupInfo;
    private PickupInfoDatabase db;
    
    private MaterialToolbar topAppBar;
    private TextView codeTextView;
    private TextView companyTextView;
    private TextView stationTextView;
    private TextView addressTextView;
    private TextView timeTextView;
    private TextView statusTextView;
    private TextView courierPhoneTextView;
    private Button copyCodeButton;
    private Button markCollectedButton;
    private Button deleteButton;
    private Button callCourierButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_info_detail);
        
        // 初始化数据库
        db = PickupInfoDatabase.getInstance(this);
        
        // 初始化视图
        initViews();
        
        // 获取传递的取件信息ID
        Intent intent = getIntent();
        int pickupInfoId = intent.getIntExtra(EXTRA_PICKUP_INFO_ID, -1);
        
        if (pickupInfoId != -1) {
            loadPickupInfo(pickupInfoId);
        } else {
            Toast.makeText(this, "无效的取件信息", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        codeTextView = findViewById(R.id.codeTextView);
        companyTextView = findViewById(R.id.companyTextView);
        stationTextView = findViewById(R.id.stationTextView);
        addressTextView = findViewById(R.id.addressTextView);
        timeTextView = findViewById(R.id.timeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        courierPhoneTextView = findViewById(R.id.courierPhoneTextView);
        copyCodeButton = findViewById(R.id.copyCodeButton);
        markCollectedButton = findViewById(R.id.markCollectedButton);
        deleteButton = findViewById(R.id.deleteButton);
        callCourierButton = findViewById(R.id.callCourierButton);
        
        // 设置Toolbar
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> finish());
        
        // 设置按钮点击事件
        copyCodeButton.setOnClickListener(v -> copyCodeToClipboard());
        markCollectedButton.setOnClickListener(v -> toggleCollectedStatus());
        deleteButton.setOnClickListener(v -> deletePickupInfo());
        callCourierButton.setOnClickListener(v -> callCourier());
    }
    
    private void loadPickupInfo(int id) {
        new Thread(() -> {
            pickupInfo = db.pickupInfoDao().getPickupInfoById(id);
            runOnUiThread(this::updateUI);
        }).start();
    }
    
    private void updateUI() {
        if (pickupInfo == null) {
            Toast.makeText(this, "未找到取件信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        codeTextView.setText("取件码: " + pickupInfo.getCode());
        companyTextView.setText("快递公司: " + (pickupInfo.getCompany() != null ? pickupInfo.getCompany() : "未知"));
        stationTextView.setText("驿站: " + (pickupInfo.getStation() != null ? pickupInfo.getStation() : "未知"));
        addressTextView.setText("地址: " + (pickupInfo.getAddress() != null ? pickupInfo.getAddress() : "未知"));
        timeTextView.setText("时间: " + pickupInfo.getFormattedTime());
        statusTextView.setText("状态: " + (pickupInfo.getStatus() == PickupInfo.STATUS_UNCOLLECTED ? "未取件" : "已取件"));
        courierPhoneTextView.setText("快递员电话: " + (pickupInfo.getCourierPhone() != null ? pickupInfo.getCourierPhone() : "未知"));
        
        // 根据状态设置按钮文本
        if (pickupInfo.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
            markCollectedButton.setText("标记已取件");
        } else {
            markCollectedButton.setText("标记未取件");
        }
    }
    
    private void copyCodeToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("取件码", pickupInfo.getCode());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "取件码已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
    
    private void toggleCollectedStatus() {
        if (pickupInfo.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
            pickupInfo.setStatus(PickupInfo.STATUS_COLLECTED);
        } else {
            pickupInfo.setStatus(PickupInfo.STATUS_UNCOLLECTED);
        }
        
        new Thread(() -> {
            db.pickupInfoDao().update(pickupInfo);
            runOnUiThread(() -> {
                updateUI();
                Toast.makeText(this, 
                    pickupInfo.getStatus() == PickupInfo.STATUS_COLLECTED ? 
                    "已标记为已取件" : "已标记为未取件", 
                    Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
    
    private void deletePickupInfo() {
        new Thread(() -> {
            db.pickupInfoDao().delete(pickupInfo);
            runOnUiThread(() -> {
                Toast.makeText(this, "已删除取件信息", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
    
    private void callCourier() {
        if (pickupInfo.getCourierPhone() != null && !pickupInfo.getCourierPhone().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + pickupInfo.getCourierPhone()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "未提供快递员电话", Toast.LENGTH_SHORT).show();
        }
    }
}