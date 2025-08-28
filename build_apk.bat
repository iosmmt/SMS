@echo off
echo 开始构建Debug版本APK...
cd /d "c:\Users\Administrator\Downloads\2\2"
call gradlew.bat assembleDebug
echo 构建完成！
pause