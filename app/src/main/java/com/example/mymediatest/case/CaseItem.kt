package com.example.mymediatest.case

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

interface CaseItem {

    val fragmentClass: KClass<out Fragment>

    val pageName: String
        get() = javaClass.simpleName

    val showActionBar: Boolean
        get() = true

    val extras: Bundle?
        get() = null
}

