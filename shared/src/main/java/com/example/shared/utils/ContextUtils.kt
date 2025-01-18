package com.example.shared.utils

import android.app.Application
import com.example.shared.app.BaseApplication

val app: Application
    get() = BaseApplication.globalApplication
