package com.example.mymediatest.test

import android.os.Bundle
import android.view.View
import com.example.mymediatest.R
import com.example.mymediatest.player.MediaPlayerHelper
import com.example.mymediatest.test.base.PlayerFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.bind
import com.example.shared.utils.logD

open class Test002VideoSurfaceView : PlayerFragment<MediaPlayerHelper.MediaPlayerHelperHolder>() {

    private val TAG = javaClass.simpleName

    override val playerLayoutId: Int
        get() = R.layout.test_002_video_surface_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(playerVM.state) {
            onStateChange(it)
        }
        bind(playerVM.contentUri) {
            if (it === null) {
                playerVM.state.value = PlayerState.IDLE
            } else {
                playerView.helper.uri.value = it
            }
        }
        bind(playerVM.isSeekDragging) {
            if (it) {
                playerView.helper.pause()
            } else {
                onStateChange(playerVM.state.value)
            }
        }
        bind(playerVM.currentPosition) {
            if (!playerView.helper.isPlaying) {
                playerView.helper.seekTo(it.toInt())
            }
        }
        bind(playerView.helper.currentState) {
            TAG.logD { "currentState $it" }
            when (it) {
                MediaPlayerHelper.State.PREPARED -> {
                    playerVM.state.value = PlayerState.PAUSED
                    playerVM.currentPosition.value = 0
                    playerVM.duration.value = playerView.helper.duration.coerceAtLeast(0).toLong()
                }
                MediaPlayerHelper.State.PLAYBACK_COMPLETED -> {
                    playerVM.state.value = PlayerState.PAUSED
                    playerVM.currentPosition.value = 0
                }
                else -> {
                }
            }
        }
    }

    private fun onStateChange(state: PlayerState) {
        when (state) {
            PlayerState.PLAYING -> playerView.helper.start()
            PlayerState.PAUSED -> playerView.helper.pause()
            PlayerState.IDLE -> playerView.helper.suspend()
        }
    }

    override val currentPosition: Long
        get() = playerView.helper.currentPosition.toLong()
}
