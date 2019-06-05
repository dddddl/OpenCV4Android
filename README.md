**环境配置**

- AndroidStudio 3.4.1
- 打开SDK Tools，勾选LLDB CMake NDK，系统自动下载配置好ndk环境
- 在<https://developer.android.google.cn/ndk/downloads/older_releases.html#ndk-14b-downloads>下载 android-ndk-r14b 版本的NDK
- 打开Project Structure 修改项目NDK地址，指向r14b版本

**代码**

- MatUtil  Mat像素操作
- ImageProcess 图像操作
- FeatureMatchUtil 基本特征检测
- haar_detect.cpp 人脸检测，人脸美化相关代码
- photo_fix.cpp 照片修复相关代码