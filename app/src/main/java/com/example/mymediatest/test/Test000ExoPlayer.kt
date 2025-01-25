package com.example.mymediatest.test

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.mymediatest.test.base.PlayerFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.TAG
import com.example.shared.utils.bind
import com.example.shared.utils.elseZero
import com.example.shared.utils.logD

abstract class Test000ExoPlayer<V : Any> : PlayerFragment<V>(), Player.Listener {

    protected abstract fun createPlayer(context: Context): Player

    private lateinit var player: Player

    private var waitPrepared = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = createPlayer(requireContext())
        bind(playerVM.state) {
            onStateChange(it)
        }
        bind(playerVM.contentUri) {
            waitPrepared = true
            if (it === null) {
                playerVM.state.value = PlayerState.IDLE
            } else {
                player.setMediaItem(MediaItem.fromUri(it))
                player.prepare()
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
                player.seekTo(it)
            }
        }
        player.addListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        TAG.logD { "onPlaybackStateChanged $playbackState" }
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_READY -> {
                if (waitPrepared) {
                    waitPrepared = false
                    playerVM.state.value = PlayerState.PAUSED
                    playerVM.currentPosition.value = 0
                    playerVM.duration.value = player.duration.coerceAtLeast(0)
                }
            }
            Player.STATE_ENDED -> {
                playerVM.state.value = PlayerState.PAUSED
                playerVM.currentPosition.value = 0
            }
            else -> {
            }
        }
    }

    private fun onStateChange(state: PlayerState) {
        when (state) {
            PlayerState.PLAYING -> player.play()
            PlayerState.PAUSED -> player.pause()
            PlayerState.IDLE -> player.stop()
        }
    }

    override val currentPosition: Long
        get() = player.currentPosition.elseZero
}
