#ifndef MYMEDIATEST_UTIL_JNI_H
#define MYMEDIATEST_UTIL_JNI_H

#include <utility>
#include <new>

#include "util_jni_ref.h"
#include "util_string.h"

#define NATIVE_METHOD(className, functionName, signature) \
JNINativeMethod { \
    .name = #functionName, \
    .signature = signature, \
    .fnPtr = (void *) (className ## _ ## functionName), \
}

namespace PKG_NS {

void EnvFatalError(
    JNIEnv &env,
    std::function<void(std::stringstream &)> block
) {
    std::string msg = BuildString(std::move(block));
    env.FatalError(msg.data());
}

void RegisterNativeMethods(
    JNIEnv &env,
    const char *className,
    const JNINativeMethod *gMethods,
    int numMethods
) {
    auto cls = LocalRef(env, env.FindClass(className));
    if (!*cls) {
        EnvFatalError(env, [&](auto &ss) {
            ss << "Native registration unable to find class " << className;
        });
    }
    if (env.RegisterNatives(*cls, gMethods, numMethods) < 0) {
        EnvFatalError(env, [&](auto &ss) {
            ss << "RegisterNatives failed for " << className;
        });
    }
}

} // namespace PKG_NS

#endif //MYMEDIATEST_UTIL_JNI_H
