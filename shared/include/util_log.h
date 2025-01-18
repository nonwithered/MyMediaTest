#ifndef MYMEDIATEST_UTIL_LOG_H
#define MYMEDIATEST_UTIL_LOG_H

#include <android/log.h>

#include "util_base.h"

#ifndef ALOG
#define ALOG(priority, tag, fmt...) __android_log_print(ANDROID_##priority, tag, fmt)
#endif

#ifndef ALOGV
    #define ALOGV(tag, ...) ((void) ALOG(LOG_VERBOSE, tag, __VA_ARGS__))
#endif

#ifndef ALOGD
    #define ALOGD(tag, ...) ((void) ALOG(LOG_DEBUG, tag, __VA_ARGS__))
#endif

#ifndef ALOGI
    #define ALOGI(tag, ...) ((void) ALOG(LOG_INFO, tag, __VA_ARGS__))
#endif

#ifndef ALOGW
    #define ALOGW(tag, ...) ((void) ALOG(LOG_WARN, tag, __VA_ARGS__))
#endif

#ifndef ALOGE
    #define ALOGE(tag, ...) ((void) ALOG(LOG_ERROR, tag, __VA_ARGS__))
#endif

#ifndef ALOGF
    #define ALOGF(tag, ...) ((void) ALOG(LOG_FATAL, tag, __VA_ARGS__))
#endif

#endif //MYMEDIATEST_UTIL_LOG_H
