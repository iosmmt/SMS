# 签名密钥说明

## 调试密钥

此目录包含用于调试的签名密钥，仅用于开发和测试目的。

## 生产环境签名

在Gitee CI/CD环境中，应使用Gitee的密钥管理功能来存储生产环境的签名信息：

1. 在Gitee项目设置中配置环境变量：
   - `KEYSTORE_FILE`: Base64编码的keystore文件
   - `KEYSTORE_PASSWORD`: keystore密码
   - `KEY_ALIAS`: 密钥别名
   - `KEY_PASSWORD`: 密钥密码

2. 在构建脚本中解码并使用这些环境变量

## 安全注意事项

- 不要将生产环境的keystore文件提交到代码仓库
- 使用.gitignore确保keystore文件被忽略
- 在Gitee CI/CD中使用密钥管理功能存储敏感信息