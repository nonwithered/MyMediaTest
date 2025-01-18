package com.example.shared.page

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected val context: Context
        get() = this

    protected open val layoutId: Int
        @LayoutRes get() = 0

    protected open fun createView(): View? = null

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createView()?.let { content ->
            setContentView(content)
        } ?: setContentView(layoutId)
        onViewCreated(savedInstanceState)
    }

    protected open fun onViewCreated(savedInstanceState: Bundle?) {
    }
}
