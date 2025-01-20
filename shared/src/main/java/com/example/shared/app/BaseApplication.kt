package com.example.shared.app

import android.app.Application
import android.content.Context

open class BaseApplication : Application() {

    companion object {

        lateinit var globalApplication: Application
            private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        globalApplication = this
    }
}
