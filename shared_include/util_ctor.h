#ifndef MYMEDIATEST_UTIL_CTOR_H
#define MYMEDIATEST_UTIL_CTOR_H

#include "util_base.h"

#ifndef FORBID_COPY
#define FORBID_COPY(T) \
T(const T &) = delete; \
void operator=(const T &) = delete;
#endif // ifndef FORBID_COPY

#ifndef FORBID_MOVE
#define FORBID_MOVE(T) \
T(T &&) = delete; \
void operator=(T &&) = delete;
#endif // ifndef FORBID_COPY

#endif //MYMEDIATEST_UTIL_CTOR_H
