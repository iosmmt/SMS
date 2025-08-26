@echo off
echo Generating keystore files...

cd /d "%~dp0"

echo Creating keystore directory if not exists...
if not exist app\src\main\keystore mkdir app\src\main\keystore

echo Generating debug keystore...
keytool -genkey -v -keystore app/src/main/keystore/debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US" -noprompt

echo Generating release keystore...
keytool -genkey -v -keystore app/src/main/keystore/release.keystore -alias releasekey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Release Key,O=Android,C=US" -noprompt

echo Keystore generation completed!
pause