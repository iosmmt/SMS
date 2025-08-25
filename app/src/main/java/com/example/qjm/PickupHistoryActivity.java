package com.example.qjm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class PickupHistoryActivity extends AppCompatActivity {
    private RecyclerView historyRecyclerView;
    private PickupHistoryAdapter adapter;
    private List<PickupInfo> historyList;
    private PickupInfoDatabase db;
    private Button clearHistoryButton;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_history);

        // 初始化数据库
        db = PickupInfoDatabase.getInstance(this);

        // 初始化视图
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        clearHistoryButton = findViewById(R.id.clearHistoryButton);
        topAppBar = findViewById(R.id.topAppBar);

        // 设置Toolbar
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // 设置RecyclerView
        historyList = new ArrayList<>();
        adapter = new PickupHistoryAdapter(historyList, new PickupHistoryAdapter.OnItemClickListener() {
            @Override
            public void onCopyCodeClick(int position) {
                copyCodeToClipboard(position);
            }

            @Override
            public void onCallCourierClick(int position) {
                callCourier(position);
            }
        });
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);

        // 加载历史记录
        loadHistory();

        // 设置清除历史按钮监听
        clearHistoryButton.setOnClickListener(v -> clearHistory());
    }

    // 加载历史记录
    private void loadHistory() {
        new Thread(() -> {
            try {
                List<PickupInfo> history = db.pickupInfoDao().getAllPickupInfos();
                historyList.clear();
                historyList.addAll(history);
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(PickupHistoryActivity.this, "加载历史记录失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // 清除历史记录
    private void clearHistory() {
        new Thread(() -> {
            try {
                // 这里我们只清除已取件的记录，保留未取件的记录
                List<PickupInfo> allInfos = db.pickupInfoDao().getAllPickupInfos();
                for (PickupInfo info : allInfos) {
                    if (info.getStatus() == PickupInfo.STATUS_COLLECTED) {
                        db.pickupInfoDao().delete(info);
                    }
                }
                
                runOnUiThread(() -> {
                    loadHistory(); // 重新加载历史记录
                    Toast.makeText(PickupHistoryActivity.this, "已清除已取件的历史记录", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(PickupHistoryActivity.this, "清除历史记录失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // 复制取件码到剪贴板
    private void copyCodeToClipboard(int position) {
        PickupInfo info = historyList.get(position);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("取件码", info.getCode());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "取件码已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    // 联系快递员
    private void callCourier(int position) {
        PickupInfo info = historyList.get(position);
        if (info.getCourierPhone() != null && !info.getCourierPhone().isEmpty()) {
            // 在实际应用中，这里应该启动拨号界面
            Toast.makeText(this, "快递员电话: " + info.getCourierPhone(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "未找到快递员电话信息", Toast.LENGTH_SHORT).show();
        }
    }
}