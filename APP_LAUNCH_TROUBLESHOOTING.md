# 应用启动问题故障排除指南

## 问题描述
应用安装后无法进入主页，会自动退回到桌面。

## 可能原因及解决方案

### 1. 签名问题
#### 现象
- APK签名不正确或不完整
- 签名密钥库文件损坏或配置错误

#### 解决方案
1. 重新生成密钥库文件：
   ```bash
   # 生成调试密钥库
   keytool -genkey -v -keystore app/src/main/keystore/debug.keystore -storepass 123456 -alias debug -keypass 123456 -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" -storetype pkcs12
   
   # 生成发布密钥库
   keytool -genkey -v -keystore app/src/main/keystore/release.keystore -storepass 123456 -alias release -keypass 123456 -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Release,O=Android,C=US" -storetype pkcs12
   ```

2. 验证密钥库文件是否有效：
   ```bash
   keytool -list -keystore app/src/main/keystore/debug.keystore -storepass 123456
   keytool -list -keystore app/src/main/keystore/release.keystore -storepass 123456
   ```

### 2. 应用启动异常
#### 现象
- 应用启动时发生未捕获的异常
- MainActivity初始化失败

#### 解决方案
1. 检查设备上的日志：
   ```bash
   adb logcat | grep com.example.qjm
   ```
   
2. 在MainActivity的onCreate方法中添加更详细的日志记录：
   ```java
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Log.d(TAG, "onCreate: 开始创建Activity");
       
       try {
           setContentView(R.layout.activity_main);
           Log.d(TAG, "onCreate: 已设置布局");
           
           // ... 其他初始化代码 ...
           
       } catch (Exception e) {
           Log.e(TAG, "onCreate: 创建Activity时发生异常", e);
           Toast.makeText(this, "应用初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
       }
   }
   ```

### 3. 权限问题
#### 现象
- 应用缺少必要的运行时权限
- 权限请求处理不当导致应用崩溃

#### 解决方案
1. 确保在AndroidManifest.xml中声明了所有必要权限：
   ```xml
   <!-- 短信相关权限 -->
   <uses-permission android:name="android.permission.READ_SMS" />
   <uses-permission android:name="android.permission.RECEIVE_SMS" />
   
   <!-- 通知权限 (Android 13+) -->
   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
   
   <!-- 电话权限 -->
   <uses-permission android:name="android.permission.CALL_PHONE" />
   ```

2. 在MainActivity中正确处理权限请求：
   ```java
   private void checkAndRequestPermissions() {
       try {
           List<String> permissionsNeeded = new ArrayList<>();
           
           // 检查短信权限
           if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
               permissionsNeeded.add(Manifest.permission.READ_SMS);
           }
           
           if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
               permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
           }
           
           // 检查通知权限 (Android 13+)
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
               if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                   permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
               }
           }
           
           // 检查电话权限
           if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
               permissionsNeeded.add(Manifest.permission.CALL_PHONE);
           }
           
           // 如果有需要申请的权限，则请求权限
           if (!permissionsNeeded.isEmpty()) {
               ActivityCompat.requestPermissions(this, 
                   permissionsNeeded.toArray(new String[0]), 
                   PERMISSION_REQUEST_CODE);
           } else {
               Log.d(TAG, "所有权限已授予");
           }
       } catch (Exception e) {
           Log.e(TAG, "检查并请求权限时发生异常", e);
       }
   }
   ```

### 4. 设备兼容性问题
#### 现象
- 应用在特定设备或Android版本上无法正常运行
- 使用了不兼容的API

#### 解决方案
1. 检查应用的minSdk和targetSdk配置：
   ```gradle
   defaultConfig {
       minSdk 24
       targetSdk 34
   }
   ```

2. 确保使用了适当的兼容性库：
   ```gradle
   implementation 'androidx.appcompat:appcompat:1.6.1'
   implementation 'androidx.core:core:1.10.1'
   ```

## 调试步骤

### 1. 使用ADB查看详细日志
```bash
# 连接设备并查看日志
adb logcat | grep -i "com.example.qjm"

# 或者查看崩溃日志
adb logcat *:E | grep -i "com.example.qjm"
```

### 2. 重新安装应用
```bash
# 卸载现有应用
adb uninstall com.example.qjm

# 安装新APK
adb install app-release.apk
```

### 3. 检查APK完整性
```bash
# 验证APK签名
apksigner verify --verbose app-release.apk
```

## GitHub Actions构建优化

确保GitHub Actions工作流正确生成签名的APK：

```yaml
- name: Generate release keystore
  run: |
    mkdir -p ${{ github.workspace }}/app/src/main/keystore
    keytool -genkey -v -keystore ${{ github.workspace }}/app/src/main/keystore/release.keystore -storepass 123456 -alias release -keypass 123456 -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Release,O=Android,C=US" -storetype pkcs12

- name: Build Release APK
  run: ./gradlew assembleRelease
```

## 常见问题FAQ

### Q: 为什么应用安装后立即闪退？
A: 通常是因为应用启动时发生未捕获的异常。请使用adb logcat查看详细错误日志。

### Q: 如何验证APK签名是否正确？
A: 使用`apksigner verify --verbose your-app.apk`命令验证签名。

### Q: 权限问题如何排查？
A: 检查AndroidManifest.xml中的权限声明，并确保在运行时正确请求权限。

## 联系支持

如果以上方法都无法解决问题，请提供以下信息：
1. 设备型号和Android版本
2. 完整的adb logcat日志
3. 使用的APK版本信息