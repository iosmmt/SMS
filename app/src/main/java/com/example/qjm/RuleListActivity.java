package com.example.qjm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RuleListActivity extends AppCompatActivity implements RuleListAdapter.OnItemClickListener {
    private RecyclerView ruleRecyclerView;
    private RuleListAdapter adapter;
    private List<Rule> ruleList;
    private PickupInfoDatabase db;
    private Button addRuleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_list);

        // 初始化数据库
        db = PickupInfoDatabase.getInstance(this);

        // 初始化视图
        ruleRecyclerView = findViewById(R.id.ruleRecyclerView);
        addRuleButton = findViewById(R.id.addRuleButton);

        // 设置RecyclerView
        ruleList = new ArrayList<>();
        adapter = new RuleListAdapter(ruleList);
        adapter.setOnItemClickListener(this);
        ruleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ruleRecyclerView.setAdapter(adapter);

        // 加载规则
        loadRules();

        // 设置添加规则按钮监听
        addRuleButton.setOnClickListener(v -> {
            // 打开添加规则页面
            Intent intent = new Intent(RuleListActivity.this, AddRuleActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载规则列表
        loadRules();
    }

    // 加载规则
    private void loadRules() {
        new Thread(() -> {
            try {
                List<Rule> rules = db.ruleDao().getAllRules();
                ruleList.clear();
                ruleList.addAll(rules);
                runOnUiThread(() -> {
                    adapter.updateList(ruleList);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(RuleListActivity.this, "加载规则失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onEditClick(int position) {
        Rule rule = ruleList.get(position);
        // 打开编辑规则页面
        Intent intent = new Intent(RuleListActivity.this, AddRuleActivity.class);
        // 可以通过intent传递规则信息用于编辑
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        Rule rule = ruleList.get(position);
        new Thread(() -> {
            db.ruleDao().delete(rule);
            runOnUiThread(() -> {
                ruleList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(RuleListActivity.this, "规则已删除", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    public void onToggleEnabledClick(int position) {
        Rule rule = ruleList.get(position);
        rule.setEnabled(!rule.isEnabled());
        new Thread(() -> {
            db.ruleDao().update(rule);
            runOnUiThread(() -> {
                adapter.notifyItemChanged(position);
                Toast.makeText(RuleListActivity.this, 
                    rule.isEnabled() ? "规则已启用" : "规则已禁用", 
                    Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}