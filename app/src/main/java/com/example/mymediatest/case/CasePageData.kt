package com.example.mymediatest.case

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.shared.bean.BundleProperties

class CasePageData(
    bundle: Bundle? = null,
): BundleProperties(bundle ?: Bundle()) {

    private var fragmentClassName: String? by "fragmentClassName".property()

    var fragmentClass: Class<out Fragment>?
        get() {
            val fragmentClassName = fragmentClassName ?: return null
            val clazz = runCatching {
                Class.forName(fragmentClassName)
            }.getOrNull()
            @Suppress("UNCHECKED_CAST")
            return clazz as? Class<out Fragment>
        }
        set(value) {
            fragmentClassName = value?.name
        }

    var pageName: String? by "pageName".property()

    var showActionBar: Boolean? by "showActionBar".property()

    var extras: Bundle? by "extras".property()
}
