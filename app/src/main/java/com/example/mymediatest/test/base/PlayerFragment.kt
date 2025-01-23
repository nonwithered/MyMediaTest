package com.example.mymediatest.test.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import com.example.mymediatest.R
import com.example.shared.page.BaseFragment
import com.example.shared.utils.AutoLauncher
import com.example.shared.utils.TAG
import com.example.shared.utils.bind
import com.example.shared.utils.connect
import com.example.shared.utils.findView
import com.example.shared.utils.logI
import com.example.shared.utils.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay

abstract class PlayerFragment<T : Any> : BaseFragment() {

    private companion object {

        private const val PROGRESS_BAR_REFRESH_INTERVAL = 500L
    }

    @get:LayoutRes
    protected open val playerLayoutId: Int
        get() = 0

    final override val layoutId = R.layout.case_player_fragment

    protected val playerVM: PlayerVM by lazy {
        viewModel()
    }

    protected val playerView: T by lazy {
        @Suppress("UNCHECKED_CAST")
        view?.findView<View>(R.id.player_view)!! as T
    }

    private val playerState by lazy {
        view?.findView<View>(R.id.player_state)!!
    }

    private val playerLoad by lazy {
        view?.findView<View>(R.id.player_load)!!
    }

    private val playerProgressBar by lazy {
        view?.findView<PlayerProgressBar>(R.id.player_progress_bar)!!
    }

    private val playerContainer by lazy {
        view?.findView<View>(R.id.player_container)!!
    }

    private val actionContainer by lazy {
        view?.findView<View>(R.id.action_container)!!
    }

    private val launcherGetContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        TAG.logI { "GetContent $it" }
        playerVM.contentUri.value = it
    }.let {
        { it.launch("video/*") }
    }

    private val refreshProgress = AutoLauncher("$TAG-refreshProgress") { Dispatchers.Main.immediate + SupervisorJob() }

    init {
        refreshProgress.launch {
            while (true) {
                playerVM.currentPosition.value = currentPosition
                delay(PROGRESS_BAR_REFRESH_INTERVAL)
            }
        }
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
            TAG.logI { "state $it" }
            val image = when (it) {
                PlayerState.PLAYING -> R.drawable.player_pause
                PlayerState.PAUSED -> R.drawable.player_start
                PlayerState.IDLE -> R.drawable.player_idle
            }
            playerState.setBackgroundResource(image)
            when (it) {
                PlayerState.PLAYING -> {
                    refreshProgress.attach = true
                }
                PlayerState.PAUSED -> {
                    refreshProgress.attach = false
                }
                PlayerState.IDLE -> {
                    actionContainer.visibility = View.VISIBLE
                    playerVM.currentPosition.value = 0
                    playerVM.duration.value = 0
                    refreshProgress.attach = false
                }
            }
        }
        connect(playerProgressBar.duration, playerVM.duration)
        connect(playerProgressBar.currentPosition, playerVM.currentPosition)
        bind(playerProgressBar.isSeekDragging, playerVM.isSeekDragging)
        playerState.setOnClickListener {
            when (playerVM.state.value) {
                PlayerState.PLAYING -> playerVM.state.value = PlayerState.PAUSED
                PlayerState.PAUSED -> playerVM.state.value = PlayerState.PLAYING
                PlayerState.IDLE -> {}
            }
        }
        playerLoad.setOnClickListener {
            launcherGetContent()
        }
        playerContainer.setOnClickListener {
            actionContainer.visibility = when (actionContainer.visibility) {
                View.VISIBLE -> View.INVISIBLE
                else -> View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshProgress.attach = false
    }

    protected open val currentPosition: Long
        get() = 0
}
