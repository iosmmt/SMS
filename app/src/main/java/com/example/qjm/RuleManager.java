package com.example.qjm;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleManager {
    private static final String TAG = "RuleManager";
    private static RuleManager instance;
    private List<Rule> allRules;
    private Map<String, List<Rule>> rulesByType; // 按信息类型分组的规则
    private Context context;

    // 默认正则规则
    private static final String DEFAULT_CODE_PATTERN = "([A-Z0-9]+[-]?){1,3}[A-Z0-9]*"; // 支持3-9位数字或数字字母混合模式，以及带符号的取件码
    private static final String DEFAULT_STATION_PATTERN = "(菜鸟驿站|妈妈驿站|快递驿站|代收点)[^\\s]*"; // 驿站常用名称
    private static final String DEFAULT_ADDRESS_PATTERN = "(地址[:：]\\s*([^\\s]+))|(取件地点[:：]\\s*([^\\s]+))"; // 地址信息
    private static final String DEFAULT_COMPANY_PATTERN = "(顺丰|中通|圆通|申通|韵达|百世|京东|极兔|德邦|EMS)"; // 快递公司
    
    private RuleManager(Context context) {
        this.context = context;
        this.allRules = new ArrayList<>();
        this.rulesByType = new HashMap<>();
        loadRules();
    }

    public static synchronized RuleManager getInstance(Context context) {
        if (instance == null) {
            instance = new RuleManager(context);
        }
        return instance;
    }

    // 加载所有规则
    private void loadRules() {
        try {
            new Thread(() -> {
                try {
                    PickupInfoDatabase db = PickupInfoDatabase.getInstance(context);
                    allRules = db.ruleDao().getAllRules();

                    // 按信息类型分组
                    rulesByType.clear();
                    for (Rule rule : allRules) {
                        // 只处理启用的规则
                        if (rule.isEnabled()) {
                            String type = rule.getInfoType();
                            if (!rulesByType.containsKey(type)) {
                                rulesByType.put(type, new ArrayList<>());
                            }
                            rulesByType.get(type).add(rule);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "加载规则时发生异常", e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "启动加载规则线程时发生异常", e);
        }
    }

    // 获取所有规则
    public List<Rule> getAllRules() {
        return new ArrayList<>(allRules);
    }

    // 保存规则
    public void saveRule(Rule rule) {
        try {
            new Thread(() -> {
                try {
                    PickupInfoDatabase db = PickupInfoDatabase.getInstance(context);
                    if (rule.getId() == 0) {
                        db.ruleDao().insert(rule);
                    } else {
                        db.ruleDao().update(rule);
                    }
                    // 重新加载规则
                    loadRules();
                } catch (Exception e) {
                    Log.e(TAG, "保存规则时发生异常", e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "启动保存规则线程时发生异常", e);
        }
    }

    // 删除规则
    public void deleteRule(Rule rule) {
        try {
            new Thread(() -> {
                try {
                    PickupInfoDatabase db = PickupInfoDatabase.getInstance(context);
                    db.ruleDao().delete(rule);
                    // 重新加载规则
                    loadRules();
                } catch (Exception e) {
                    Log.e(TAG, "删除规则时发生异常", e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "启动删除规则线程时发生异常", e);
        }
    }

    // 应用规则提取信息
    public Map<String, String> extractInfo(String smsContent) {
        Map<String, String> result = new HashMap<>();
        
        try {
            // 首先尝试使用用户自定义规则
            for (String type : rulesByType.keySet()) {
                List<Rule> rules = rulesByType.get(type);
                for (Rule rule : rules) {
                    String value = null;
                    
                    // 检查是否为正则表达式规则（通过前缀是否包含特殊正则字符判断）
                    if (rule.getPrefix() != null && (rule.getPrefix().contains(".*") || 
                        rule.getPrefix() != null && rule.getPrefix().contains("[") ||
                        rule.getPrefix() != null && rule.getPrefix().contains("("))) {
                        // 使用正则表达式规则
                        value = extractByRegex(smsContent, rule.getPrefix());
                    } else if (rule.getPrefix() != null && rule.getSuffix() != null) {
                        // 使用前后缀规则
                        value = extractByRule(smsContent, rule);
                    }
                    
                    if (value != null && !value.isEmpty()) {
                        result.put(type, value);
                        break; // 对每种类型只取第一个匹配的规则结果
                    }
                }
            }

            // 使用默认正则表达式提取信息（作为备选方案）
            if (!result.containsKey(Rule.TYPE_CODE)) {
                String code = extractByRegex(smsContent, DEFAULT_CODE_PATTERN);
                if (code != null) {
                    result.put(Rule.TYPE_CODE, code);
                }
            }
            
            if (!result.containsKey(Rule.TYPE_STATION)) {
                String station = extractByRegex(smsContent, DEFAULT_STATION_PATTERN);
                if (station != null) {
                    result.put(Rule.TYPE_STATION, station);
                }
            }
            
            if (!result.containsKey(Rule.TYPE_ADDRESS)) {
                String address = extractByRegex(smsContent, DEFAULT_ADDRESS_PATTERN);
                if (address != null) {
                    // 提取实际地址内容（第二个捕获组）
                    Pattern pattern = Pattern.compile(DEFAULT_ADDRESS_PATTERN);
                    Matcher matcher = pattern.matcher(address);
                    if (matcher.find()) {
                        if (matcher.group(2) != null) {
                            result.put(Rule.TYPE_ADDRESS, matcher.group(2));
                        } else if (matcher.group(4) != null) {
                            result.put(Rule.TYPE_ADDRESS, matcher.group(4));
                        }
                    }
                }
            }
            
            if (!result.containsKey(Rule.TYPE_COMPANY)) {
                String company = extractByRegex(smsContent, DEFAULT_COMPANY_PATTERN);
                if (company != null) {
                    result.put(Rule.TYPE_COMPANY, company);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "应用规则提取信息时发生异常", e);
        }

        return result;
    }

    // 使用单条规则提取信息（前后缀模式）
    private String extractByRule(String content, Rule rule) {
        try {
            if (content == null || rule == null || rule.getPrefix() == null || rule.getSuffix() == null) {
                return null;
            }
            
            String patternStr = Pattern.quote(rule.getPrefix()) + "(.*?)" + Pattern.quote(rule.getSuffix());
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            Log.e(TAG, "使用前后缀规则提取信息时发生异常", e);
        }
        return null;
    }

    // 使用正则表达式提取信息
    private String extractByRegex(String content, String regex) {
        try {
            if (content == null || regex == null) {
                return null;
            }
            
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception e) {
            Log.e(TAG, "使用正则表达式提取信息时发生异常", e);
        }
        return null;
    }

    // 测试规则
    public String testRule(String smsContent, Rule rule) {
        try {
            // 检查是否为正则表达式规则
            if (rule.getPrefix() != null && (rule.getPrefix().contains(".*") || 
                rule.getPrefix() != null && rule.getPrefix().contains("[") ||
                rule.getPrefix() != null && rule.getPrefix().contains("("))) {
                return extractByRegex(smsContent, rule.getPrefix());
            } else {
                return extractByRule(smsContent, rule);
            }
        } catch (Exception e) {
            Log.e(TAG, "测试规则时发生异常", e);
            return null;
        }
    }
    
    // 测试正则表达式规则
    public String testRegexRule(String smsContent, String regex) {
        try {
            return extractByRegex(smsContent, regex);
        } catch (Exception e) {
            Log.e(TAG, "测试正则表达式规则时发生异常", e);
            return null;
        }
    }
}