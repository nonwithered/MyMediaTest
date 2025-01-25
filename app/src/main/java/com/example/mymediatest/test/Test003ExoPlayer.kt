package com.example.mymediatest.test

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.mymediatest.R
import com.example.mymediatest.player.BasePlayer
import com.example.mymediatest.player.ExoPlayerHelper
import com.example.mymediatest.select.CaseParamsFragment
import com.example.mymediatest.test.base.PlayerFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.TAG
import com.example.shared.utils.bind
import com.example.shared.utils.elseZero
import com.example.shared.utils.getValue
import com.example.shared.utils.logD

class Test003ExoPlayer : PlayerFragment<View>(), Player.Listener {

    internal enum class Type(
        @LayoutRes
        val playerLayoutId: Int,
        val createPlayer: (Context, View) -> Player,
    ) {
        RAW(
            R.layout.common_exo_player_view,
            { context, view ->
                ExoPlayer.Builder(context)
                    .build()
                    .also {
                        it.playWhenReady = false
                        (view as PlayerView).player = it
                    }
            },
        ),
        SURFACE(
            R.layout.common_player_surface_view,
            { context, view ->
                ExoPlayerHelper(context).also {
                    (view as BasePlayer.Holder).player = it
                }
            },
        ),
        TEXTURE(
            R.layout.common_player_texture_view,
            { context, view ->
                ExoPlayerHelper(context).also {
                    (view as BasePlayer.Holder).player = it
                }
            },
        ),
    }

    internal class Params(
        bundle: Bundle = Bundle(),
    ) : BaseParams(bundle) {

        var type: Type by Type::class.adapt()
    }

    internal class ParamsBuilder : CaseParamsFragment<Params>(Params()) {

        init {
            option(
                *Type.entries.toTypedArray()
            ) {
                caseParams.type = it
            }
        }
    }

    private val params by {
        Params(pageData.paramsExtras!!)
    }

    override val playerLayoutId: Int
        get() = params.type.playerLayoutId

    private lateinit var player: Player

    private var waitPrepared = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = params.type.createPlayer(requireContext(), playerView)
        defer {
            player.release()
        }
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
