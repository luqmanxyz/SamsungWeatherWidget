# Samsung Weather Widget

三星 One UI 风格 4×2 天气桌面小组件。无需安装 Android Studio，可直接使用 GitHub Actions 云端编译 APK。

## 最省空间的编译方法：GitHub Actions

1. 在 GitHub 新建一个空仓库，例如 `SamsungWeatherWidget`。
2. 将本 ZIP **解压后的所有文件**上传到仓库根目录。根目录应直接看到：
   - `app/`
   - `.github/`
   - `build.gradle.kts`
   - `settings.gradle.kts`
3. 打开仓库的 **Actions** 页面。
4. 选择 **Build Android APK**。
5. 点击 **Run workflow**。
6. 等待构建完成后，打开本次运行页面。
7. 在 **Artifacts** 下载 `SamsungWeatherWidget-debug-apk`。
8. 解压下载的 Artifact，即得到 `app-debug.apk`。

> 第一次 push 到 main/master 也会自动触发一次编译。

## 使用 ADB 安装到三星手机

只需要 Android Platform Tools（ADB），不需要 Android Studio。

手机开启：开发者选项 → USB 调试，然后连接电脑：

```bash
adb devices
adb install -r app-debug.apk
```

如需卸载：

```bash
adb uninstall com.example.samsungweatherwidget
```

## 添加 Widget

1. 安装 APK 后先打开 `Samsung Weather Widget` App。
2. 设置位置并保存。
3. 三星桌面长按空白处 → **小组件**。
4. 找到 **Samsung Weather Widget**，添加 4×2 Widget。

## 工程参数

- Android Gradle Plugin: 8.7.3
- Cloud Gradle: 8.9
- JDK: 17
- compileSdk / targetSdk: 35
- minSdk: 26

天气数据来自 Open-Meteo，无需 API Key。


## One UI Glass B variant
This package uses a transparent widget canvas with a minimalist semi-transparent dark glass card. The old mountain illustration is not used in the widget layout.
