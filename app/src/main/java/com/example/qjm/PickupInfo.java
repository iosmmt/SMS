package com.example.qjm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "pickup_info")
public class PickupInfo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String code; // 取件码
    private String company; // 快递公司
    private String address; // 地址
    private String station; // 驿站
    private String courierPhone; // 快递员电话
    private long timestamp; // 时间戳
    private int status; // 状态：0未取件，1已取件

    public static final int STATUS_UNCOLLECTED = 0;
    public static final int STATUS_COLLECTED = 1;

    // 构造函数
    public PickupInfo() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getCourierPhone() {
        return courierPhone;
    }

    public void setCourierPhone(String courierPhone) {
        this.courierPhone = courierPhone;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // 获取格式化的时间
    public String getFormattedTime() {
        try {
            Date date = new Date(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        } catch (Exception e) {
            // 如果格式化失败，返回默认字符串
            return "未知时间";
        }
    }
}