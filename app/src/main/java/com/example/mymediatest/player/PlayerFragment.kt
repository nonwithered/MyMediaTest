package com.example.mymediatest.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.example.mymediatest.R
import com.example.shared.page.BaseFragment
import com.example.shared.utils.bind
import com.example.shared.utils.findView
import com.example.shared.utils.viewModel

abstract class PlayerFragment<T : View> : BaseFragment() {

    @get:LayoutRes
    protected open val playerLayoutId: Int
        get() = 0

    final override val layoutId = R.layout.base_player_fragment

    protected val playerVM: PlayerVM by lazy {
        viewModel()
    }

    protected val playerView: T by lazy {
        view?.findViewById(R.id.player_view)!!
    }

    private val playerState by lazy {
        view?.findView<View>(R.id.player_state)!!
    }

    private val playerLoad by lazy {
        view?.findView<View>(R.id.player_load)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        val stub: ViewStub = v.findView(R.id.player_stub)!!
        if (playerLayoutId != 0) {
            stub.layoutResource = playerLayoutId
            stub.inflate()
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(playerVM.state) {
            val image = when (it) {
                PlayerState.PLAYING -> R.drawable.player_pause
                PlayerState.PAUSED -> R.drawable.player_start
                PlayerState.IDLE -> R.drawable.player_idle
            }
            playerState.setBackgroundResource(image)
        }
        playerState.setOnClickListener {
            when (playerVM.state.value) {
                PlayerState.PLAYING -> playerVM.state.value = PlayerState.PAUSED
                PlayerState.PAUSED -> playerVM.state.value = PlayerState.PLAYING
                PlayerState.IDLE -> {}
            }
        }
    }
}
