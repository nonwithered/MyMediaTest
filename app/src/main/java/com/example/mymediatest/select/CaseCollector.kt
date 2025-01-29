package com.example.mymediatest.select

import androidx.fragment.app.Fragment
import com.example.mymediatest.select.CaseSelectActivity.*
import com.example.mymediatest.test.*
import kotlin.reflect.KClass

object CaseCollector {

    val items = listOf(
        Test001VideoView::class.asCaseItem,
        Test002MediaPlayer::class asCaseItem Test002MediaPlayer.ParamsBuilder::class,
        Test003ExoPlayer::class asCaseItem Test003ExoPlayer.ParamsBuilder::class,
    )

    private val KClass<out Fragment>.asCaseItem: CaseItem
        get() = object : CaseItem {
            override val fragmentClass = this@asCaseItem
        }

    private infix fun KClass<out Fragment>.asCaseItem(builder: KClass<out Fragment>): CaseItem {
        return object : CaseItem {
            override val fragmentClass = this@asCaseItem
            override val builderClass = builder
        }
    }
}
