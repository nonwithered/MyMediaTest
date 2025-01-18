#ifndef MYMEDIATEST_UTIL_DEFER_H
#define MYMEDIATEST_UTIL_DEFER_H

#include <functional>

#include "util_ctor.h"

namespace PKG_NS {

class Defer {

public:
    Defer(std::function<void()> &&block) : block_(std::move(block)) {
    }
    ~Defer() {
        if (block_) {
            block_();
        }
        block_ = nullptr;
    }

private:
    std::function<void()> block_;

    FORBID_COPY(Defer)
    FORBID_MOVE(Defer)
};

} // namespace PKG_NS

#endif //MYMEDIATEST_UTIL_DEFER_H
