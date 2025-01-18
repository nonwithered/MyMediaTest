#ifndef MYMEDIATEST_UTIL_JNI_REF_H
#define MYMEDIATEST_UTIL_JNI_REF_H

#include <utility>
#include <new>

#include <jni.h>

#include "util_ctor.h"

namespace PKG_NS {

template<typename T>
class LocalRef {

public:
    LocalRef(JNIEnv &env, T ref) : env_(env), ref_(ref) {
    }
    LocalRef(const LocalRef &that) : LocalRef(that.env_, that.env_.NewLocalRef(that.ref_)) {
    }
    LocalRef(LocalRef &&that) : LocalRef(that.env_, that.ref_) {
        that.ref_ = nullptr;
    }
    ~LocalRef() {
        if (ref_) {
            env_.DeleteLocalRef(ref_);
        }
        ref_ = nullptr;
    }
    void operator=(const LocalRef &that) {
        this->~LocalRef();
        new (this) LocalRef(that);
    }
    void operator=(LocalRef &&that) {
        this->~LocalRef();
        new (this) LocalRef(std::move(that));
    }
    T operator*() {
        return ref_;
    };

private:
    JNIEnv &env_;
    T ref_;
};

template<typename T>
class GlobalRef {

public:
    GlobalRef(JNIEnv &env, T ref) : env_(env), ref_(ref) {
    }
    GlobalRef(const GlobalRef &that) : GlobalRef(that.env_, that.env_.NewGlobalRef(that.ref_)) {
    }
    GlobalRef(GlobalRef &&that) : GlobalRef(that.env_, that.ref_) {
        that.ref_ = nullptr;
    }
    ~GlobalRef() {
        if (ref_) {
            env_.DeleteGlobalRef(ref_);
        }
        ref_ = nullptr;
    }
    void operator=(const GlobalRef &that) {
        this->~GlobalRef();
        new (this) GlobalRef(that);
    }
    void operator=(GlobalRef &&that) {
        this->~GlobalRef();
        new (this) GlobalRef(std::move(that));
    }
    T operator*() {
        return ref_;
    };

private:
    JNIEnv &env_;
    T ref_;
};

} // namespace PKG_NS

#endif //MYMEDIATEST_UTIL_JNI_REF_H
