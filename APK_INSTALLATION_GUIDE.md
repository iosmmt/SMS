# APK安装和故障排除指南

## 常见问题及解决方案

### 1. APK无法安装

#### 问题原因：
- APK未正确签名
- 设备上已安装相同包名的应用
- APK文件损坏

#### 解决方案：
1. 确保使用正确的签名密钥构建APK
2. 卸载设备上已存在的相同应用
3. 重新构建APK

### 2. 应用闪退

#### 问题原因：
- 权限未正确授予
- 广播接收器注册问题
- 资源文件问题
- 数据库初始化失败

#### 解决方案：
1. 确保授予所有必要权限（短信、通知等）
2. 检查AndroidManifest.xml中的权限声明
3. 确保资源文件完整且格式正确
4. 清除应用数据后重新安装

## 构建步骤

### 1. 生成签名密钥
项目已包含生成签名密钥的脚本：
- [generate_keystore.bat](file:///c%3A/Users/Administrator/Downloads/2/2/generate_keystore.bat)：自动生成调试和发布密钥库

### 2. 构建APK
使用以下方法之一构建APK：

#### 方法一：使用预定义脚本
- [build_apk.bat](file:///c%3A/Users/Administrator/Downloads/2/2/build_apk.bat)：构建Debug版本
- [build_release_apk.bat](file:///c%3A/Users/Administrator/Downloads/2/2/build_release_apk.bat)：构建Release版本

#### 方法二：使用命令行
```bash
# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease
```

## 安装步骤

### 1. 启用未知来源安装
在Android设备上：
1. 打开设置
2. 找到"应用和通知"或"安全"
3. 启用"未知来源"或"安装未知应用"选项

### 2. 安装APK
1. 将生成的APK文件传输到设备
2. 在文件管理器中找到APK文件
3. 点击安装

### 3. 授予权限
安装完成后，首次运行应用时会提示授予权限：
1. 短信权限：用于读取取件码短信
2. 通知权限：用于发送取件提醒
3. 电话权限：用于联系快递员功能

## 故障排除

### 1. 检查日志
如果应用闪退，可以通过以下方式查看日志：
```bash
# 使用adb查看日志
adb logcat | grep com.example.qjm
```

### 2. 清除数据
如果应用无法正常启动：
1. 打开设备设置
2. 找到应用管理
3. 找到"取件码"应用
4. 点击"存储"
5. 点击"清除数据"和"清除缓存"

### 3. 重新安装
如果问题持续存在：
1. 卸载现有应用
2. 重新构建APK
3. 重新安装

## 联系支持

如果以上方法都无法解决问题，请提供以下信息：
1. 设备型号和Android版本
2. 应用版本
3. 错误日志（如果可能）
4. 问题重现步骤