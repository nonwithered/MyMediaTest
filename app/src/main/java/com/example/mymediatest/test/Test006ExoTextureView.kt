package com.example.mymediatest.test

import android.content.Context
import androidx.media3.common.Player
import com.example.mymediatest.R
import com.example.mymediatest.player.BasePlayer
import com.example.mymediatest.player.ExoPlayerHelper

class Test006ExoTextureView : Test000ExoPlayer<BasePlayer.Holder>() {

    override val playerLayoutId: Int
        get() = R.layout.common_player_texture_view

    override fun createPlayer(context: Context): Player {
        return ExoPlayerHelper(requireContext()).also {
            playerView.player = it
        }
    }
}
