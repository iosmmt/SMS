package com.example.qjm;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    private PieChart companyChart;
    private PieChart stationChart;
    private TextView totalPickupText;
    private TextView pendingPickupText;
    private TextView collectedPickupText;
    private PickupInfoDatabase db;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // 初始化数据库
        db = PickupInfoDatabase.getInstance(this);

        // 初始化视图
        companyChart = findViewById(R.id.companyChart);
        stationChart = findViewById(R.id.stationChart);
        totalPickupText = findViewById(R.id.totalPickupText);
        pendingPickupText = findViewById(R.id.pendingPickupText);
        collectedPickupText = findViewById(R.id.collectedPickupText);
        topAppBar = findViewById(R.id.topAppBar);

        // 设置Toolbar
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // 加载统计数据
        loadStatistics();
    }

    // 加载统计数据
    private void loadStatistics() {
        new Thread(() -> {
            try {
                List<PickupInfo> allInfos = db.pickupInfoDao().getAllPickupInfos();
                
                // 统计总数
                int totalCount = allInfos.size();
                
                // 统计未取件和已取件数量
                int pendingCount = 0;
                int collectedCount = 0;
                
                // 统计快递公司和驿站
                Map<String, Integer> companyMap = new HashMap<>();
                Map<String, Integer> stationMap = new HashMap<>();
                
                for (PickupInfo info : allInfos) {
                    if (info.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
                        pendingCount++;
                    } else {
                        collectedCount++;
                    }
                    
                    // 统计快递公司
                    String company = info.getCompany();
                    if (company != null && !company.isEmpty()) {
                        companyMap.put(company, companyMap.getOrDefault(company, 0) + 1);
                    }
                    
                    // 统计驿站
                    String station = info.getStation();
                    if (station != null && !station.isEmpty()) {
                        stationMap.put(station, stationMap.getOrDefault(station, 0) + 1);
                    }
                }
                
                // 更新UI
                int finalPendingCount = pendingCount;
                int finalCollectedCount = collectedCount;
                int finalTotalCount = totalCount;
                runOnUiThread(() -> {
                    totalPickupText.setText("总取件数: " + finalTotalCount);
                    pendingPickupText.setText("待取件: " + finalPendingCount);
                    collectedPickupText.setText("已取件: " + finalCollectedCount);
                    
                    // 设置快递公司饼图
                    setupPieChart(companyChart, companyMap, "快递公司统计");
                    
                    // 设置驿站饼图
                    setupPieChart(stationChart, stationMap, "驿站统计");
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(StatisticsActivity.this, "加载统计数据失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // 设置饼图
    private void setupPieChart(PieChart chart, Map<String, Integer> dataMap, String title) {
        List<PieEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, title);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.getDescription().setEnabled(false);
        chart.setCenterText(title);
        chart.animateY(1000);
        chart.invalidate();
    }
}