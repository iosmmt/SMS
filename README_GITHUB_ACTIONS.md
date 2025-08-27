# GitHub Actions 自动构建 APK 指南

## 概述

本项目配置了 GitHub Actions 来自动构建 Android APK 文件。每次推送到 `main` 分支或创建 Pull Request 时，都会自动触发构建流程。

## 工作流文件

项目包含以下 GitHub Actions 工作流文件：

1. `.github/workflows/android-build.yml` - 主要的构建和测试工作流
2. `.github/workflows/release-build.yml` - 发布版本构建工作流
3. `.github/workflows/build-apk.yml` - 简单的 APK 构建工作流

## 自动构建流程

### 1. 每次推送时的构建

- 当代码推送到 `main` 分支时，会自动触发构建流程
- 构建包括：
  - 代码检查
  - 运行单元测试
  - 构建 Debug 和 Release APK
  - 上传构建产物作为 Artifacts

### 2. 发布版本构建

- 当创建以 `v` 开头的标签时（如 `v1.0.0`），会触发发布构建
- 构建 Release APK 并创建 GitHub Release

## 如何查看构建结果

1. 访问项目的 "Actions" 选项卡
2. 选择相应的工作流运行
3. 查看构建日志和状态
4. 在工作流运行页面底部的 "Artifacts" 部分下载构建的 APK 文件

## 下载 APK

构建成功后，您可以在以下位置下载 APK：

1. **Actions 选项卡**：
   - 进入项目的 GitHub 页面
   - 点击 "Actions" 选项卡
   - 选择最新的工作流运行
   - 在页面底部的 "Artifacts" 部分下载 APK

2. **Releases 页面**（仅适用于发布版本）：
   - 进入项目的 GitHub 页面
   - 点击 "Releases" 选项卡
   - 下载最新的发布版本

## 本地构建

如果您想在本地构建 APK，可以使用以下命令：

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

构建的 APK 文件将位于 `app/build/outputs/apk/` 目录中。

## 故障排除

如果构建失败，请检查以下几点：

1. 确保所有代码更改都已提交并推送
2. 检查 GitHub Actions 构建日志中的错误信息
3. 确保项目的 Gradle 配置正确
4. 确保所有依赖项都可访问

## 配置说明

### Java 版本

项目配置使用 JDK 17 进行构建，这与最新的 Android 开发要求一致。

### Android SDK 版本

- compileSdk: 34 (Android 14)
- targetSdk: 34 (Android 14)
- minSdk: 24 (Android 7.0)

## 自定义构建

如果您需要自定义构建流程，可以修改 `.github/workflows/` 目录中的工作流文件。