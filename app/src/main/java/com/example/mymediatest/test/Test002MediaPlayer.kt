package com.example.mymediatest.test

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.example.mymediatest.R
import com.example.mymediatest.player.BasePlayerHelper
import com.example.mymediatest.player.wrapper.CommonPlayerHelper
import com.example.mymediatest.player.wrapper.MediaCodecPlayerHelper
import com.example.mymediatest.player.wrapper.MediaPlayerHelper
import com.example.mymediatest.test.base.PlayerParamsFragment
import com.example.mymediatest.test.base.PlayerState
import com.example.shared.utils.TAG
import com.example.shared.utils.bind
import com.example.shared.utils.logD
import com.example.shared.utils.newInstanceSafe
import kotlin.reflect.KClass

class Test002MediaPlayer : PlayerParamsFragment<Test002MediaPlayer.Params, BasePlayerHelper.Holder>() {

    enum class ViewType(
        @LayoutRes
        val playerLayoutId: Int,
    ) {

        SURFACE(
            R.layout.common_player_surface_view,
        ),

        TEXTURE(
            R.layout.common_player_texture_view,
        ),
    }

    enum class PlayerType(

        val clazz: KClass<out CommonPlayerHelper>,
    ) {

        MEDIA_CODEC(
            MediaCodecPlayerHelper::class,
        ),

        MEDIA_PLAYER(
            MediaPlayerHelper::class,
        ),
    }
    
    class Params(
        bundle: Bundle = Bundle(),
    ) : BaseParams(bundle) {

        var viewType: ViewType by ViewType::class.adapt()

        var playerType: PlayerType by PlayerType::class.adapt()
    }

    internal class ParamsBuilder : BaseParamsBuilder<Params>(Params()) {

        init {
            caseParams::viewType.adapt()
            caseParams::playerType.adapt()
        }
    }
    
    override val playerLayoutId: Int
        get() = params.viewType.playerLayoutId
    
    private val player: CommonPlayerHelper by lazy {
        params.playerType.clazz.newInstanceSafe(Context::class to requireContext()).getOrThrow()
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
                CommonPlayerHelper.State.PREPARED -> {
                    playerVM.state.value = PlayerState.PAUSED
                    playerVM.currentPosition.value = 0
                    playerVM.duration.value = player.duration.coerceAtLeast(0)
                }
                CommonPlayerHelper.State.PLAYBACK_COMPLETED -> {
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
        get() = player.currentPosition
}
