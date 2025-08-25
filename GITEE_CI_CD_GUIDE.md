# Gitee CI/CD自动构建APK使用指南

## 概述

本项目已配置Gitee CI/CD自动构建功能，可以自动构建Android APK并发布构建产物。

## 配置文件说明

- CI/CD配置文件：`.workflow/android-build.yml`
- 签名配置目录：`app/src/main/keystore/`
- 本地配置模板：`local.properties.example`

## 在Gitee上启用自动构建

### 1. 确保项目已推送到Gitee

将项目代码推送到Gitee仓库：

```bash
git remote add origin https://gitee.com/your-username/your-repo.git
git push -u origin master
```

### 2. 配置Gitee CI/CD

1. 登录Gitee账号
2. 进入项目页面
3. 点击左侧菜单的「CI/CD」
4. 系统会自动检测到`.workflow/android-build.yml`配置文件
5. 点击「启用流水线」按钮

### 3. 配置生产环境签名（可选但推荐）

为了构建生产环境的APK，需要配置签名信息：

1. 在项目页面点击「设置」
2. 选择「流水线」->「变量管理」
3. 添加以下环境变量：
   - `KEYSTORE_BASE64`: Base64编码的keystore文件内容
   - `KEYSTORE_PASSWORD`: keystore密码
   - `KEY_ALIAS`: 密钥别名
   - `KEY_PASSWORD`: 密钥密码

生成Base64编码的keystore文件：
```bash
# Linux/Mac
base64 -i your-release-key.keystore -o keystore.base64

# Windows (PowerShell)
certutil -encode your-release-key.keystore keystore.base64
```

### 4. 触发构建

构建可以通过以下方式触发：

1. 推送代码到`master`或`main`分支
2. 创建Pull Request到`master`或`main`分支
3. 在Gitee项目页面手动触发构建

### 5. 查看构建结果

1. 在项目页面点击「CI/CD」->「构建历史」
2. 查看构建状态和日志
3. 构建成功后，可以在「构建产物」中下载APK文件

## 构建流程说明

### 构建阶段

1. **准备阶段**
   - 检查构建环境
   - 显示Java和Gradle版本信息

2. **构建阶段**
   - 清理项目
   - 下载依赖
   - 编译源代码
   - 运行单元测试
   - 构建Debug APK

3. **发布阶段**
   - 配置签名信息（调试或生产）
   - 构建Release APK
   - 发布APK构建产物
   - 生成构建报告

## 签名配置说明

### 调试签名

- 默认使用项目中的调试密钥
- 适用于开发和测试环境
- 不适用于生产发布

### 生产签名

- 使用Gitee环境变量配置的签名信息
- 适用于生产环境发布
- 更加安全，避免将密钥提交到代码仓库

## 常见问题

### 1. 构建失败

- 检查构建日志，查看具体错误信息
- 确保项目代码没有语法错误
- 确保依赖项可以正常下载

### 2. 签名配置问题

- 检查环境变量是否正确配置
- 确保keystore文件内容正确Base64编码
- 验证密码和别名是否正确

### 3. 构建时间过长

- 检查依赖项下载是否正常
- 考虑使用Gitee的依赖缓存功能
- 优化构建脚本

## 安全建议

1. 不要将生产环境的keystore文件提交到代码仓库
2. 使用Gitee的密钥管理功能存储敏感信息
3. 定期更新签名密钥
4. 限制对构建配置的访问权限

## 参考文档

- [Gitee CI/CD官方文档](https://gitee.com/help/categories/24)
- [Gitee Android Build Online](https://gitee.com/help/articles/4249)
- [Gitee Go 快速入门](https://gitee.com/help/articles/4293)