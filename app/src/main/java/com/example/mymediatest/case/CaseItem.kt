package com.example.mymediatest.case

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

interface CaseItem {

    val fragmentClass: KClass<out Fragment>

    val pageName: String
        get() = fragmentClass.java.simpleName

    val showActionBar: Boolean
        get() = false

    val extras: Bundle?
        get() = null
}
