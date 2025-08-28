@echo off
echo 开始构建Release版本APK...
cd /d "c:\Users\Administrator\Downloads\2\2"
call gradlew.bat assembleRelease
echo 构建完成！
pause