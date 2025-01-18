package com.example.mymediatest.test

import android.os.Bundle
import android.view.View
import android.widget.VideoView
import com.example.mymediatest.R
import com.example.mymediatest.case.CaseItem
import com.example.mymediatest.player.PlayerFragment

object Test001VideoView : CaseItem {

    override val fragmentClass = Page::class

    class Page : PlayerFragment<VideoView>() {

        override val playerLayoutId: Int
            get() = R.layout.test_001_video_view

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        }
    }
}
