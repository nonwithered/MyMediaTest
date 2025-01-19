package com.example.mymediatest.test.collector

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.example.mymediatest.case.CaseCollector
import com.example.mymediatest.case.CaseItem
import com.example.mymediatest.test.*
import kotlin.reflect.KClass

@Keep
class TestCaseCollector : CaseCollector {

    override val items = listOf(
        Test001VideoView::class,
        Test002VideoSurfaceView::class,
        Test003VideoTextureView::class,
        Test004ExoPlayerView::class,
    ).map {
        it.asCaseItem
    }

    private val KClass<out Fragment>.asCaseItem: CaseItem
        get() = object : CaseItem {
            override val fragmentClass = this@asCaseItem
        }
}
