package com.example.mymediatest.test

import android.os.Bundle
import android.view.View
import android.widget.VideoView
import com.example.mymediatest.R
import com.example.mymediatest.test.base.PlayerFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.bind

class Test001VideoView : PlayerFragment<VideoView>() {

    override val playerLayoutId: Int
        get() = R.layout.test_001_video_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(playerVM.state) {
            onStateChange(it)
        }
        bind(playerVM.contentUri) {
            if (it === null) {
                playerVM.state.value = PlayerState.IDLE
            } else {
                playerView.setVideoURI(it)
            }
        }
        bind(playerVM.isSeekDraging) {
            if (it) {
                playerView.pause()
            } else {
                onStateChange(playerVM.state.value)
            }
        }
        bind(playerVM.currentPosition) {
            if (!playerView.isPlaying) {
                playerView.seekTo(it.toInt())
            }
        }
        playerView.setOnPreparedListener {
            playerVM.state.value = PlayerState.PAUSED
            playerVM.duration.value = playerView.duration.coerceAtLeast(0).toLong()
        }
        playerView.setOnCompletionListener {
            playerVM.state.value = PlayerState.PAUSED
        }
    }

    private fun onStateChange(state: PlayerState) {
        when (state) {
            PlayerState.PLAYING -> playerView.start()
            PlayerState.PAUSED -> playerView.pause()
            PlayerState.IDLE -> playerView.suspend()
        }
    }

    override val currentPosition: Long
        get() = playerView.currentPosition.toLong()
}
