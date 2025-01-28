# MyMediaTest

- 构建ffmpeg需要*nix环境的NDK，windows系统可以在WSL配置环境单独编ffmpeg这部分
- 必须在环境变量中定义ANDROID_SDK_HOME、ANDROID_NDK_HOME
- ndk16以上不再支持gcc而改为clang，所以暂时用15，https://github.com/android/ndk/wiki/Unsupported-Downloads
- 高版本的ffmpeg会依赖高版本的C++，所以当前暂时用n4.2.2
- 构建之前首先要在*nix环境执行./ffmpeg/config.sh，这将为ffmpeg生成makefile，然后执行./ffmpeg/compile.sh于是可以得到它的产物
