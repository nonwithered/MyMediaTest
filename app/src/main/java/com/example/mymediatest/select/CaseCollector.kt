package com.example.mymediatest.select

import androidx.fragment.app.Fragment
import com.example.mymediatest.select.CaseSelectActivity.*
import com.example.mymediatest.test.*
import kotlin.reflect.KClass

object CaseCollector {

    val items = listOf(
        Test001VideoView::class,
        Test002VideoSurfaceView::class,
        Test003VideoTextureView::class,
        Test004ExoPlayerView::class,
        Test005ExoSurfaceView::class,
        Test006ExoTextureView::class,
    ).map {
        it.asCaseItem
    }

    private val KClass<out Fragment>.asCaseItem: CaseItem
        get() = object : CaseItem {
            override val fragmentClass = this@asCaseItem
        }
}
