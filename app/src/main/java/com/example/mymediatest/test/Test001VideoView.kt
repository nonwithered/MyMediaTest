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
        get() = R.layout.common_video_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(playerVM.state) {
            onStateChange(it)
        }
        bind(playerVM.contentUri) {
            playerView.setVideoURI(it)
            if (it === null) {
                playerVM.state.value = PlayerState.IDLE
            }
        }
        bind(playerVM.isSeekDragging) {
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
            playerVM.currentPosition.value = 0
            playerVM.duration.value = playerView.duration.coerceAtLeast(0).toLong()
        }
        playerView.setOnCompletionListener {
            playerVM.state.value = PlayerState.PAUSED
            playerVM.currentPosition.value = 0
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
