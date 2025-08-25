package com.example.qjm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CheckBox;

public class AddRuleActivity extends AppCompatActivity {
    private static final String TAG = "AddRuleActivity";
    private EditText ruleNameEditText;
    private EditText prefixEditText;
    private EditText suffixEditText;
    private Spinner infoTypeSpinner;
    private Button saveButton;
    private Button testButton;
    private EditText testSmsEditText;
    private EditText testResultEditText;
    private RuleManager ruleManager;
    private CheckBox enabledCheckBox;
    private EditText regexEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_add_rule);

            // 初始化视图
            ruleNameEditText = findViewById(R.id.ruleNameEditText);
            prefixEditText = findViewById(R.id.prefixEditText);
            suffixEditText = findViewById(R.id.suffixEditText);
            infoTypeSpinner = findViewById(R.id.infoTypeSpinner);
            saveButton = findViewById(R.id.saveButton);
            testButton = findViewById(R.id.testButton);
            testSmsEditText = findViewById(R.id.testSmsEditText);
            testResultEditText = findViewById(R.id.testResultEditText);
            enabledCheckBox = findViewById(R.id.enabledCheckBox);
            regexEditText = findViewById(R.id.regexEditText);

            // 初始化规则管理器
            ruleManager = RuleManager.getInstance(this);

            // 设置保存按钮监听
            saveButton.setOnClickListener(v -> saveRule());

            // 设置测试按钮监听
            testButton.setOnClickListener(v -> testRule());
        } catch (Exception e) {
            Log.e(TAG, "onCreate: 初始化Activity时发生异常", e);
            Toast.makeText(this, "界面初始化失败", Toast.LENGTH_LONG).show();
        }
    }

    // 保存规则
    private void saveRule() {
        try {
            String name = ruleNameEditText.getText().toString().trim();
            String prefix = prefixEditText.getText().toString().trim();
            String suffix = suffixEditText.getText().toString().trim();
            String infoType = infoTypeSpinner.getSelectedItem() != null ? 
                infoTypeSpinner.getSelectedItem().toString() : "";
            boolean enabled = enabledCheckBox.isChecked();
            String regex = regexEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "请填写规则名称", Toast.LENGTH_SHORT).show();
                return;
            }

            // 转换信息类型为常量值
            if (infoType.equals("取件码")) {
                infoType = Rule.TYPE_CODE;
            } else if (infoType.equals("快递公司")) {
                infoType = Rule.TYPE_COMPANY;
            } else if (infoType.equals("地址")) {
                infoType = Rule.TYPE_ADDRESS;
            } else if (infoType.equals("驿站")) {
                infoType = Rule.TYPE_STATION;
            }

            // 创建规则对象
            Rule rule = new Rule();
            rule.setName(name);
            
            // 如果填写了正则表达式，则保存为正则表达式规则
            if (!regex.isEmpty()) {
                rule.setPrefix(regex); // 将正则表达式存储在prefix字段中
                rule.setSuffix(""); // 清空suffix字段
            } else {
                // 否则保存为前后缀规则
                rule.setPrefix(prefix);
                rule.setSuffix(suffix);
            }
            
            rule.setInfoType(infoType);
            rule.setEnabled(enabled);

            // 保存规则
            ruleManager.saveRule(rule);

            Toast.makeText(this, "规则保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "保存规则时发生异常", e);
            Toast.makeText(this, "保存规则失败", Toast.LENGTH_LONG).show();
        }
    }

    // 测试规则
    private void testRule() {
        try {
            String prefix = prefixEditText.getText().toString().trim();
            String suffix = suffixEditText.getText().toString().trim();
            String smsContent = testSmsEditText.getText().toString().trim();
            String infoType = infoTypeSpinner.getSelectedItem() != null ? 
                infoTypeSpinner.getSelectedItem().toString() : "";
            String regex = regexEditText.getText().toString().trim();

            if (smsContent.isEmpty()) {
                Toast.makeText(this, "请填写测试短信内容", Toast.LENGTH_SHORT).show();
                return;
            }

            // 转换信息类型为常量值
            if (infoType.equals("取件码")) {
                infoType = Rule.TYPE_CODE;
            } else if (infoType.equals("快递公司")) {
                infoType = Rule.TYPE_COMPANY;
            } else if (infoType.equals("地址")) {
                infoType = Rule.TYPE_ADDRESS;
            } else if (infoType.equals("驿站")) {
                infoType = Rule.TYPE_STATION;
            }

            String result = null;
            
            // 如果填写了正则表达式，则测试正则表达式规则
            if (!regex.isEmpty()) {
                result = ruleManager.testRegexRule(smsContent, regex);
            } else if (!prefix.isEmpty() || !suffix.isEmpty()) {
                // 否则测试前后缀规则
                // 创建临时规则对象进行测试
                Rule testRule = new Rule();
                testRule.setPrefix(prefix);
                testRule.setSuffix(suffix);
                testRule.setInfoType(infoType);

                // 测试规则
                result = ruleManager.testRule(smsContent, testRule);
            } else {
                Toast.makeText(this, "请填写正则表达式或前后缀内容", Toast.LENGTH_SHORT).show();
                return;
            }

            if (result != null && !result.isEmpty()) {
                testResultEditText.setText("提取结果: " + result);
            } else {
                testResultEditText.setText("未提取到任何信息");
            }
        } catch (Exception e) {
            Log.e(TAG, "测试规则时发生异常", e);
            Toast.makeText(this, "测试规则失败", Toast.LENGTH_LONG).show();
            testResultEditText.setText("测试失败: " + e.getMessage());
        }
    }
}