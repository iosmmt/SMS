package com.example.qjm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    // 获取短信数据
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            try {
                                SmsMessage smsMessage;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    String format = bundle.getString("format");
                                    smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                                } else {
                                    smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                                }

                                String messageBody = smsMessage.getMessageBody();
                                String sender = smsMessage.getOriginatingAddress();
                                long timestamp = smsMessage.getTimestampMillis();

                                // 解析短信内容
                                PickupInfo pickupInfo = parseSmsMessage(context, messageBody, sender, timestamp);
                                if (pickupInfo != null) {
                                    // 保存取件信息到共享偏好设置，供Worker使用
                                    android.content.SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("PICKUP_INFO", Context.MODE_PRIVATE);
                                    android.content.SharedPreferences.Editor editor = sharedPref.edit();
                                    String pickupInfoJson = new com.google.gson.Gson().toJson(pickupInfo);
                                    editor.putString("PICKUP_INFO_JSON", pickupInfoJson);
                                    editor.apply();

                                    // 使用WorkManager处理后台任务
                                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SmsProcessingWorker.class)
                                            .build();
                                    WorkManager.getInstance(context.getApplicationContext()).enqueue(workRequest);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "处理单条短信时发生异常", e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理短信接收时发生异常", e);
        }
    }

    // 解析短信内容提取取件信息
    private PickupInfo parseSmsMessage(Context context, String messageBody, String sender, long timestamp) {
        try {
            // 使用RuleManager处理短信内容，优先应用用户自定义规则
            RuleManager ruleManager = RuleManager.getInstance(context); // 修复：传递有效的context
            Map<String, String> extractedInfo = ruleManager.extractInfo(messageBody);
            
            String code = extractedInfo.get(Rule.TYPE_CODE);
            String company = extractedInfo.get(Rule.TYPE_COMPANY);
            String address = extractedInfo.get(Rule.TYPE_ADDRESS);
            String station = extractedInfo.get(Rule.TYPE_STATION);

            // 如果没有提取到快递公司，使用默认解析方法
            if (company == null || company.isEmpty()) {
                company = parseCompany(messageBody, sender);
            }
            
            // 如果没有提取到地址，使用默认解析方法
            if (address == null || address.isEmpty()) {
                address = parseAddress(messageBody);
            }
            
            // 如果没有提取到驿站，使用默认解析方法
            if (station == null || station.isEmpty()) {
                station = parseStation(messageBody);
            }
            
            // 提取快递员电话
            String courierPhone = parseCourierPhone(messageBody);

            if (code != null && !code.isEmpty()) {
                PickupInfo info = new PickupInfo();
                info.setCode(code);
                info.setCompany(company != null ? company : "未知快递");
                info.setAddress(address != null ? address : "未知地址");
                info.setStation(station != null ? station : "未知驿站");
                info.setCourierPhone(courierPhone);
                info.setTimestamp(timestamp);
                info.setStatus(PickupInfo.STATUS_UNCOLLECTED);
                return info;
            }
        } catch (Exception e) {
            Log.e(TAG, "解析短信内容时发生异常", e);
        }

        return null;
    }

    // 解析快递公司
    private String parseCompany(String messageBody, String sender) {
        try {
            // 简单实现，可根据实际需求扩展
            if (messageBody.contains("顺丰") || (sender != null && sender.contains("SF"))) {
                return "顺丰速运";
            } else if (messageBody.contains("中通")) {
                return "中通快递";
            } else if (messageBody.contains("圆通")) {
                return "圆通快递";
            } else if (messageBody.contains("申通")) {
                return "申通快递";
            } else if (messageBody.contains("韵达")) {
                return "韵达快递";
            } else if (messageBody.contains("百世")) {
                return "百世快递";
            } else if (messageBody.contains("京东")) {
                return "京东物流";
            } else if (messageBody.contains("极兔")) {
                return "极兔速递";
            } else if (messageBody.contains("德邦")) {
                return "德邦快递";
            } else if (messageBody.contains("EMS")) {
                return "EMS";
            } else {
                return "未知快递";
            }
        } catch (Exception e) {
            Log.e(TAG, "解析快递公司时发生异常", e);
            return "未知快递";
        }
    }

    // 解析地址
    private String parseAddress(String messageBody) {
        try {
            // 简单实现，可根据实际需求扩展
            Pattern addressPattern = Pattern.compile("(地址|取件地点)[:：]\\s*([^\\s]+)");
            Matcher addressMatcher = addressPattern.matcher(messageBody);
            if (addressMatcher.find()) {
                return addressMatcher.group(2);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析地址时发生异常", e);
        }
        return "未知地址";
    }

    // 解析驿站
    private String parseStation(String messageBody) {
        try {
            // 简单实现，可根据实际需求扩展
            Pattern stationPattern = Pattern.compile("(菜鸟驿站|妈妈驿站|快递驿站|代收点)[^\\s]*");
            Matcher stationMatcher = stationPattern.matcher(messageBody);
            if (stationMatcher.find()) {
                return stationMatcher.group();
            }
        } catch (Exception e) {
            Log.e(TAG, "解析驿站时发生异常", e);
        }
        return "未知驿站";
    }
    
    // 解析快递员电话
    private String parseCourierPhone(String messageBody) {
        try {
            // 匹配11位手机号码
            Pattern phonePattern = Pattern.compile("1[3-9]\\d{9}");
            Matcher phoneMatcher = phonePattern.matcher(messageBody);
            if (phoneMatcher.find()) {
                return phoneMatcher.group();
            }
        } catch (Exception e) {
            Log.e(TAG, "解析快递员电话时发生异常", e);
        }
        return null;
    }
}