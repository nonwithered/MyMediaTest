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
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.mymediatest.play.ExoPlayerHelper
import com.example.mymediatest.test.base.PlayerParamsFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.TAG
import com.example.shared.utils.bind
import com.example.shared.utils.elseZero
import com.example.shared.utils.logD

class Test003ExoPlayer : PlayerParamsFragment<Test003ExoPlayer.Params, View>(), Player.Listener {

    interface PlayerFactory {

        fun createPlayer(context: Context, view: View): Player
    }

    enum class ViewType(
        @LayoutRes
        val playerLayoutId: Int,
    ) : PlayerFactory {

        DEFAULT(
            R.layout.common_exo_player_view,
        ) {

            override fun createPlayer(context: Context, view: View): Player {
                view as PlayerView
                return ExoPlayer.Builder(context).build().also {
                        it.playWhenReady = false
                        view.player = it
                    }
            }
        },

        SURFACE(
            R.layout.common_player_surface_view,
        ) {

            override fun createPlayer(context: Context, view: View): Player {
                view as BasePlayerHelper.Holder
                return ExoPlayerHelper(context).also {
                    view.player = it
                }
            }
        },

        TEXTURE(
            R.layout.common_player_texture_view,
        ) {

            override fun createPlayer(context: Context, view: View): Player {
                view as BasePlayerHelper.Holder
                return ExoPlayerHelper(context).also {
                    view.player = it
                }
            }
        },
    }

    class Params(
        bundle: Bundle = Bundle(),
    ) : BaseParams(bundle) {

        var viewType: ViewType by ViewType::class.adapt()
    }

    internal class ParamsBuilder : BaseParamsBuilder<Params>(Params()) {

        init {
            caseParams::viewType.adapt()
        }
    }

    override val playerLayoutId: Int
        get() = params.viewType.playerLayoutId

    private lateinit var player: Player

    private var waitPrepared = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = params.viewType.createPlayer(requireContext(), playerView)
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
