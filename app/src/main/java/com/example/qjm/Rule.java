package com.example.qjm;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rule")
public class Rule {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name; // 规则名称
    private String prefix; // 前文内容
    private String suffix; // 后文内容
    private String infoType; // 提取的信息类型：code, company, address, station
    private boolean enabled = true; // 规则是否启用，默认启用

    public static final String TYPE_CODE = "code"; // 取件码
    public static final String TYPE_COMPANY = "company"; // 快递公司
    public static final String TYPE_ADDRESS = "address"; // 地址
    public static final String TYPE_STATION = "station"; // 驿站

    // 构造函数
    public Rule() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getInfoType() {
        return infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}