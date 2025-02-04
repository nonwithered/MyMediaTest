package com.example.mymediatest.play.base

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.example.mymediatest.utils.VideoViewHelper
import com.example.shared.utils.TAG
import com.example.shared.utils.Vec2
import com.example.shared.utils.asConst
import com.example.shared.utils.logD
import com.example.shared.utils.logI
import com.example.shared.utils.mainScope
import com.example.shared.utils.systemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * @see android.widget.VideoView
 */
class CommonPlayerHelper(
    context: Context,
    private val factory: Factory,
) : BasePlayerHelper(context),
    SurfaceHolder.Callback2,
    TextureView.SurfaceTextureListener {

    fun interface Factory {

        fun createController(
            context: Context,
            uri: Uri,
            surface: Surface,
            listener: Listener,
        ): Controller
    }

    interface Listener {

        fun onPrepared(videoSize: Vec2<Int>)

        fun onVideoSizeChanged(videoSize: Vec2<Int>)

        fun onCompletion()

        fun onError(e: Throwable)
    }

    abstract class Controller(
        protected val context: Context,
        protected val uri: Uri,
        protected val surface: Surface,
        protected val listener: Listener,
    ) : AutoCloseable {

        abstract val isPlaying: Boolean

        abstract val duration: Long

        abstract val currentPosition: Long

        abstract fun seekTo(pos: Long)

        abstract fun start()

        abstract fun pause()

        abstract fun asRenderer(): GLRenderer?
    }

    enum class State {
        ERROR,
        IDLE,
        PREPARING,
        PREPARED,
        PLAYING,
        PAUSED,
        PLAYBACK_COMPLETED,
    }

    var uri: Uri? = null
        set(value) {
            TAG.logD { "uri set $value" }
            field = value
            seekWhenPrepared = 0
            openVideo()
            requestLayout()
            invalidate()
        }

    private val _currentState = MutableStateFlow(State.IDLE)
    val currentState = _currentState.asConst

    private var targetState = State.IDLE

    private var videoSize = 0 to 0
        set(value) {
            field = value
            val (videoWidth, videoHeight) = value
            if (videoWidth != 0 && videoHeight != 0) {
                val view = viewAdapter.view
                if (view is SurfaceView) {
                    view.holder.setFixedSize(videoWidth, videoHeight)
                }
                requestLayout()
            }
        }

    private var surface: Surface? = null
        set(value) {
            field = value
            TAG.logD { "surface set $value" }
            if (value === null) {
                release(true)
            } else {
                openVideo()
            }
        }

    private var surfaceSize = 0 to 0
        set(value) {
            field = value
            val isValidState = targetState == State.PLAYING
            val hasValidSize = videoSize == value
            if (controller !== null && isValidState && hasValidSize) {
                seekWhenPrepared.takeIf { pos -> pos != 0L }?.let { pos -> seekTo(pos) }
                start()
            }
        }

    private var seekWhenPrepared = 0L

    private var controller: Controller? = null

    private var audioFocusType = AudioManager.AUDIOFOCUS_GAIN
    private val audioManager = context.systemService<AudioManager>()
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
        .build()
    private val audioFocusRequest: AudioFocusRequest
        get() = AudioFocusRequest.Builder(audioFocusType)
            .setAudioAttributes(audioAttributes)
            .build()

    private val listener = object : Listener {

        override fun onPrepared(videoSize: Vec2<Int>) {
            mainScope.launch {
                this@CommonPlayerHelper.onPrepared(videoSize)
            }
        }

        override fun onVideoSizeChanged(videoSize: Vec2<Int>) {
            mainScope.launch {
                this@CommonPlayerHelper.onVideoSizeChanged(videoSize)
            }
        }

        override fun onCompletion() {
            mainScope.launch {
                this@CommonPlayerHelper.onCompletion()
            }
        }

        override fun onError(e: Throwable) {
            mainScope.launch {
                this@CommonPlayerHelper.onError(e)
            }
        }

    }

    override fun onInit(view: View) {
        super.onInit(view)
        when (view) {
            is SurfaceView -> {
                view.holder.addCallback(this)
            }
            is TextureView -> {
                view.surfaceTextureListener = this
            }
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        setMeasuredDimension: (width: Int, height: Int) -> Unit,
    ): Boolean {
        val (width, height) = VideoViewHelper.onMeasure(
            measureSpec = widthMeasureSpec to heightMeasureSpec,
            videoSize = videoSize,
        )
        setMeasuredDimension(width, height)
        return true
    }

    override fun asRenderer(): GLRenderer? {
        return controller?.asRenderer()
    }

    private val isInPlaybackState: Boolean
        get() {
            controller ?: return false
            return when (_currentState.value) {
                State.ERROR, State.IDLE, State.PREPARING -> false
                else -> true
            }
        }

    private fun onPrepared(videoSize: Vec2<Int>) {
        TAG.logD { "onPrepared $videoSize" }
        _currentState.value = State.PREPARED
        this.videoSize = videoSize
        seekWhenPrepared.takeIf { pos -> pos != 0L }?.let { pos -> seekTo(pos) }
        if (videoSize.first == 0 || videoSize.second == 0) {
            if (targetState == State.PLAYING) {
                start()
            }
        } else if (videoSize == surfaceSize) {
            if (targetState == State.PLAYING) {
                start()
            }
        }
    }

    private fun onVideoSizeChanged(videoSize: Vec2<Int>) {
        TAG.logD { "onVideoSizeChanged $videoSize" }
        this.videoSize = videoSize
    }

    private fun onCompletion() {
        TAG.logD { "onCompletion" }
        _currentState.value = State.PLAYBACK_COMPLETED
        targetState = State.PLAYBACK_COMPLETED
        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }

    private fun onError(e: Throwable) {
        TAG.logD(e) { "onError" }
        _currentState.value = State.ERROR
        targetState = State.ERROR
    }

    private fun openVideo() {
        val uri = uri ?: return
        val surface = surface ?: return
        TAG.logI { "openVideo $uri" }
        release(false)
        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.requestAudioFocus(audioFocusRequest)
        }
        controller = kotlin.runCatching {
            factory.createController(
                context,
                uri,
                surface,
                listener,
            )
        }.onFailure { e ->
            listener.onError(e)
        }.getOrNull()?.also {
            _currentState.value = State.PREPARING
        }
    }

    private fun release(clearTargetState: Boolean) {
        val mp = controller ?: return
        mp.close()
        controller = null
        _currentState.value = State.IDLE
        if (clearTargetState) {
            targetState = State.IDLE
        }
        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }

    fun suspend() {
        release(false)
    }

    fun start() {
        if (isInPlaybackState) {
            controller?.start()
            _currentState.value = State.PLAYING
        }
        targetState = State.PLAYING
    }

    fun pause() {
        if (isInPlaybackState) {
            val mp = controller!!
            if (mp.isPlaying) {
                mp.pause()
                _currentState.value = State.PAUSED
            }
        }
        targetState = State.PAUSED
    }

    val duration: Long
        get() {
            if (isInPlaybackState) {
                return controller!!.duration
            }
            return -1
        }

    val currentPosition: Long
        get() {
            if (isInPlaybackState) {
                return controller!!.currentPosition
            }
            return 0
        }

    fun seekTo(pos: Long) {
        seekWhenPrepared = if (isInPlaybackState) {
            controller?.seekTo(pos)
            0
        } else {
            pos
        }
    }

    val isPlaying: Boolean
        get() {
            return isInPlaybackState && controller!!.isPlaying
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surface = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceSize = width to height
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        surface = Surface(texture)
    }

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        surface = null
        return true
    }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
        surfaceSize = width to height
    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
    }
}
