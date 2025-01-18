#ifndef MYMEDIATEST_UTIL_STRING_H
#define MYMEDIATEST_UTIL_STRING_H

#include <sstream>
#include <functional>

#include "util_base.h"

namespace PKG_NS {

std::string BuildString(
    std::function<void(std::stringstream &)> block
) {
    std::string s;
    {
        std::stringstream ss;
        block(ss);
        ss >> s;
    }
    return s;
}

} // namespace PKG_NS

#endif //MYMEDIATEST_UTIL_STRING_H
