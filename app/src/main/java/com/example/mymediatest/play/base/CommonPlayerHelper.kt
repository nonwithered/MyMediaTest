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
import com.example.shared.utils.systemService
import kotlinx.coroutines.flow.MutableStateFlow

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

        fun openVideo(
            context: Context,
            uri: Uri,
            surface: Surface,
            audioAttributes: AudioAttributes,
            listener: Listener,
        ): Controller?
    }

    interface Listener {

        fun onPrepared(videoSize: Vec2<Int>)

        fun onVideoSizeChanged(videoSize: Vec2<Int>)

        fun onCompletion()

        fun onError(e: Throwable)
    }

    interface Controller : AutoCloseable {

        val isPlaying: Boolean

        val duration: Long

        val currentPosition: Long

        fun seekTo(pos: Int)

        fun start()

        fun pause()
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
            if (commonPlayer !== null && isValidState && hasValidSize) {
                seekWhenPrepared.takeIf { pos -> pos != 0 }?.let { pos -> seekTo(pos) }
                start()
            }
        }

    private var seekWhenPrepared = 0

    private var commonPlayer: Controller? = null

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
            this@CommonPlayerHelper.onPrepared(videoSize)
        }

        override fun onVideoSizeChanged(videoSize: Vec2<Int>) {
            this@CommonPlayerHelper.onVideoSizeChanged(videoSize)
        }

        override fun onCompletion() {
            this@CommonPlayerHelper.onCompletion()
        }

        override fun onError(e: Throwable) {
            this@CommonPlayerHelper.onError(e)
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

    private val isInPlaybackState: Boolean
        get() {
            commonPlayer ?: return false
            return when (_currentState.value) {
                State.ERROR, State.IDLE, State.PREPARING -> false
                else -> true
            }
        }

    private fun onPrepared(videoSize: Vec2<Int>) {
        TAG.logD { "onPrepared $videoSize" }
        _currentState.value = State.PREPARED
        this.videoSize = videoSize
        seekWhenPrepared.takeIf { pos -> pos != 0 }?.let { pos -> seekTo(pos) }
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
        commonPlayer = factory.openVideo(
            context = context,
            uri = uri,
            surface = surface,
            audioAttributes = audioAttributes,
            listener = listener,
        )?.also {
            _currentState.value = State.PREPARING
        }
    }

    private fun release(clearTargetState: Boolean) {
        val mp = commonPlayer ?: return
        mp.close()
        commonPlayer = null
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
            commonPlayer?.start()
            _currentState.value = State.PLAYING
        }
        targetState = State.PLAYING
    }

    fun pause() {
        if (isInPlaybackState) {
            val mp = commonPlayer!!
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
                return commonPlayer!!.duration
            }
            return -1
        }

    val currentPosition: Long
        get() {
            if (isInPlaybackState) {
                return commonPlayer!!.currentPosition
            }
            return 0
        }

    fun seekTo(pos: Int) {
        seekWhenPrepared = if (isInPlaybackState) {
            commonPlayer?.seekTo(pos)
            0
        } else {
            pos
        }
    }

    val isPlaying: Boolean
        get() {
            return isInPlaybackState && commonPlayer!!.isPlaying
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
