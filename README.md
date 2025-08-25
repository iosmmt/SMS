# 取件码 (Pickup Code)

一个基于Android平台的取件码管理应用。

## 项目简介

这是一个Android应用项目，旨在帮助用户管理快递取件码。应用基于Android最新技术构建，使用AndroidX库支持。

## 功能特点

- 自动读取短信并提取快递取件信息
- 支持自定义规则和正则表达式规则
- 管理快递取件码
- 简洁直观的用户界面
- 基于Material Design设计规范
- 支持取件历史记录和数据统计
- 一键复制取件码和联系快递员功能

## 技术栈

- Android SDK 34
- AndroidX库
- Material Design组件
- Room持久化库
- WorkManager后台任务处理
- Gson JSON解析
- MPAndroidChart数据可视化
- Java 11

## 构建要求

- Android Studio
- JDK 8 或更高版本
- Android SDK 34

## 本地构建项目

使用以下命令构建项目：

```bash
./gradlew build
```

或者在Windows上：

```bash
gradlew.bat build
```

## 安装部署

可以通过以下命令安装到设备：

```bash
./gradlew installDebug
```

## Gitee自动构建

本项目已配置Gitee CI/CD自动构建功能：

1. 每当代码推送到`master`或`main`分支时，会自动触发构建流程
2. 构建包括Debug和Release版本的APK
3. 构建产物会自动发布到Gitee Releases

### 配置说明

- CI/CD配置文件位于：`.workflow/android-build.yml`
- 构建环境：Gitee提供的Android构建环境
- 构建工具：Gradle
- JDK版本：根据Gitee环境自动选择
- Android SDK：根据Gitee环境自动配置

### 构建步骤

1. 环境检查和依赖下载
2. 项目编译和单元测试
3. Debug APK构建
4. Release APK构建（包含签名配置）
5. 构建产物发布

## 使用说明

1. 安装应用后，授予必要的短信和通知权限
2. 应用会自动监听短信并提取取件信息
3. 可以通过添加规则功能自定义取件码提取规则
4. 支持标记取件状态、删除记录、查看历史等功能