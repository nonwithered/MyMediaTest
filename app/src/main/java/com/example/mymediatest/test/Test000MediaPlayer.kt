package com.example.mymediatest.test

import android.os.Bundle
import android.view.View
import com.example.mymediatest.player.BasePlayer
import com.example.mymediatest.player.MediaPlayerHelper
import com.example.mymediatest.test.base.PlayerFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.TAG
import com.example.shared.utils.bind
import com.example.shared.utils.logD

abstract class Test000MediaPlayer : PlayerFragment<BasePlayer.Holder>() {

    private val player by lazy {
        MediaPlayerHelper(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView.player = player
        bind(playerVM.state) {
            onStateChange(it)
        }
        bind(playerVM.contentUri) {
            TAG.logD { "contentUri get $it" }
            player.uri = it
            if (it === null) {
                playerVM.state.value = PlayerState.IDLE
            }
        }
        bind(playerVM.isSeekDragging) {
            if (it) {
                player.pause()
            } else {
                onStateChange(playerVM.state.value)
            }
        }
        bind(playerVM.currentPosition) {
            if (!player.isPlaying) {
                player.seekTo(it.toInt())
            }
        }
        bind(player.currentState) {
            TAG.logD { "currentState get $it" }
            when (it) {
                MediaPlayerHelper.State.PREPARED -> {
                    playerVM.state.value = PlayerState.PAUSED
                    playerVM.currentPosition.value = 0
                    playerVM.duration.value = player.duration.coerceAtLeast(0).toLong()
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
            PlayerState.PLAYING -> player.start()
            PlayerState.PAUSED -> player.pause()
            PlayerState.IDLE -> player.suspend()
        }
    }

    override val currentPosition: Long
        get() = player.currentPosition.toLong()
}
