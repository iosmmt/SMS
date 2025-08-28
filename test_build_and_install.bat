@echo off
echo 正在清理项目...
call gradlew clean

echo.
echo 正在生成调试密钥库...
mkdir app\src\main\keystore 2>nul
keytool -genkey -v -keystore app/src/main/keystore/debug.keystore -storepass 123456 -alias debug -keypass 123456 -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" -storetype pkcs12

echo.
echo 正在构建Debug APK...
call gradlew assembleDebug

echo.
echo 正在安装APK到设备...
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo.
echo 构建和安装完成！
echo 如果应用仍然无法启动，请运行以下命令查看日志：
echo adb logcat | findstr com.example.qjm
pause