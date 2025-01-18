package com.example.mymediatest.test

import android.widget.VideoView
import com.example.mymediatest.case.CaseItem
import com.example.mymediatest.player.PlayerFragment

object Test001VideoView : CaseItem {

    override val fragmentClass = Page::class

    class Page : PlayerFragment<VideoView>()
}
