package com.example.mymediatest.test

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.mymediatest.R

class Test004ExoPlayerView : Test000ExoPlayer<PlayerView>() {

    override val playerLayoutId: Int
        get() = R.layout.common_exo_player_view

    override fun createPlayer(context: Context): Player {
        return ExoPlayer.Builder(requireContext())
            .build()
            .also {
                it.playWhenReady = false
                playerView.player = it
            }
    }
}
