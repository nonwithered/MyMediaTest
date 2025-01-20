package com.example.mymediatest.test

import android.os.Bundle
import android.view.View
import com.example.mymediatest.R
import com.example.mymediatest.player.BasePlayerHelper
import com.example.mymediatest.player.MediaPlayerHelper
import com.example.mymediatest.test.base.PlayerFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.bind
import com.example.shared.utils.logD

open class Test002VideoSurfaceView : PlayerFragment<BasePlayerHelper.Holder>() {

    override val playerLayoutId: Int
        get() = R.layout.common_player_surface_view

    private val helper by lazy {
        MediaPlayerHelper(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView.helper = helper
        bind(playerVM.state) {
            onStateChange(it)
        }
        bind(playerVM.contentUri) {
            TAG.logD { "contentUri get $it" }
            helper.uri.value = it
            if (it === null) {
                playerVM.state.value = PlayerState.IDLE
            }
        }
        bind(playerVM.isSeekDragging) {
            if (it) {
                helper.pause()
            } else {
                onStateChange(playerVM.state.value)
            }
        }
        bind(playerVM.currentPosition) {
            if (!helper.isPlaying) {
                helper.seekTo(it.toInt())
            }
        }
        bind(helper.currentState) {
            TAG.logD { "currentState get $it" }
            when (it) {
                MediaPlayerHelper.State.PREPARED -> {
                    playerVM.state.value = PlayerState.PAUSED
                    playerVM.currentPosition.value = 0
                    playerVM.duration.value = helper.duration.coerceAtLeast(0).toLong()
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
            PlayerState.PLAYING -> helper.start()
            PlayerState.PAUSED -> helper.pause()
            PlayerState.IDLE -> helper.suspend()
        }
    }

    override val currentPosition: Long
        get() = helper.currentPosition.toLong()
}
