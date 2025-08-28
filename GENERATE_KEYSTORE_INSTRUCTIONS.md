# 密钥库生成说明

## 本地生成密钥库

为了在本地构建和签名APK，您需要生成调试和发布密钥库。

### 自动生成密钥库

项目提供了自动生成密钥库的脚本：

- Windows: [generate_keystore.bat](file:///c%3A/Users/Administrator/Downloads/2/2/generate_keystore.bat)

只需双击运行该脚本，它会自动生成所需的密钥库文件。

### 手动生成密钥库

您也可以手动使用以下命令生成密钥库：

#### 生成调试密钥库
```bash
keytool -genkey -v -keystore app/src/main/keystore/debug.keystore -storepass 123456 -alias debug -keypass 123456 -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" -storetype pkcs12
```

#### 生成发布密钥库
```bash
keytool -genkey -v -keystore app/src/main/keystore/release.keystore -storepass 123456 -alias release -keypass 123456 -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Release,O=Android,C=US" -storetype pkcs12
```

### GitHub Actions中的密钥库生成

GitHub Actions工作流已配置为在构建过程中自动生成密钥库，无需手动操作。

### 目录结构

密钥库文件应位于以下位置：
```
app/
└── src/
    └── main/
        └── keystore/
            ├── debug.keystore
            └── release.keystore
```

### 注意事项

1. 请勿将密钥库文件提交到版本控制系统中（除了debug.keystore）
2. 发布密钥库应妥善保管，避免泄露
3. 如果需要重新生成密钥库，请确保更新相应的配置文件