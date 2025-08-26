# 生成签名密钥说明

## 生成调试密钥库

在项目根目录下执行以下命令生成调试密钥库：

```bash
keytool -genkey -v -keystore app/src/main/keystore/debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US" -noprompt
```

## 生成发布密钥库

在项目根目录下执行以下命令生成发布密钥库：

```bash
keytool -genkey -v -keystore app/src/main/keystore/release.keystore -alias releasekey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Release Key,O=Android,C=US" -noprompt
```

## 配置local.properties文件

复制[local.properties.example](file:///c%3A/Users/Administrator/Downloads/2/2/local.properties.example)文件为[local.properties](file:///c%3A/Users/Administrator/Downloads/2/2/local.properties)，并根据需要修改签名配置：

```properties
# Release版本签名配置
STORE_FILE=../src/main/keystore/release.keystore
STORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password

# 调试版本签名配置（可选，默认使用调试密钥）
DEBUG_STORE_FILE=../src/main/keystore/debug.keystore
DEBUG_STORE_PASSWORD=android
DEBUG_KEY_ALIAS=androiddebugkey
DEBUG_KEY_PASSWORD=android
```

## 注意事项

1. 不要将[local.properties](file:///c%3A/Users/Administrator/Downloads/2/2/local.properties)文件提交到版本控制系统中，因为它包含敏感信息
2. 在CI/CD环境中，应该使用环境变量来配置签名信息
3. 发布密钥库文件应该妥善保管，避免泄露