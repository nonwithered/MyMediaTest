#include <jni.h>
#include <string>

#include <util_jni.h>

extern "C" JNIEXPORT void JNICALL
Java_com_example_ffmpeg_FFmpeg_registerNatives(
    JNIEnv* env,
    jobject /* this */
) {
    JNINativeMethod gMethods[] = {
    };
    PKG_NS::RegisterNativeMethods(
            *env,
            "com/example/ffmpeg/FFmpeg",
            gMethods,
            sizeof(gMethods) / sizeof(JNINativeMethod)
    );
}
