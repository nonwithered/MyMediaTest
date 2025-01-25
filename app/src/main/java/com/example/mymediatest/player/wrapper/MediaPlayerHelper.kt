package com.example.mymediatest.player.wrapper

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.MediaController
import com.example.mymediatest.player.BasePlayer
import com.example.mymediatest.player.utils.VideoViewHelper
import com.example.shared.utils.TAG
import com.example.shared.utils.asConst
import com.example.shared.utils.logD
import com.example.shared.utils.logI
import com.example.shared.utils.runCatchingTyped
import com.example.shared.utils.systemService
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/**
 * @see android.widget.VideoView
 */
class MediaPlayerHelper(
    context: Context,
) : BasePlayer(context),
    SurfaceHolder.Callback2,
    TextureView.SurfaceTextureListener,
    MediaController.MediaPlayerControl,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener {

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
            if (mediaPlayer !== null && isValidState && hasValidSize) {
                seekWhenPrepared.takeIf { pos -> pos != 0 }?.let { pos -> seekTo(pos) }
                start()
            }
        }

    private var seekWhenPrepared = 0
    private var currentBufferPercentage = 0

    private var mediaPlayer: MediaPlayer? = null

    private var audioSession = 0
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
            mediaPlayer ?: return false
            return when (_currentState.value) {
                State.ERROR, State.IDLE, State.PREPARING -> false
                else -> true
            }
        }

    override fun onPrepared(mp: MediaPlayer) {
        _currentState.value = State.PREPARED
        videoSize = mp.videoWidth to mp.videoHeight
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

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        videoSize = mp.videoWidth to mp.videoHeight
    }

    override fun onCompletion(mp: MediaPlayer?) {
        _currentState.value = State.PLAYBACK_COMPLETED
        targetState = State.PLAYBACK_COMPLETED
        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        _currentState.value = State.ERROR
        targetState = State.ERROR
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        currentBufferPercentage = percent
    }

    private fun openVideo() {
        val uri = uri ?: return
        val surface = surface ?: return
        TAG.logI { "openVideo $uri" }
        release(false)
        if (audioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            audioManager.requestAudioFocus(audioFocusRequest)
        }
        val mp = MediaPlayer()
        mediaPlayer = mp

        val handleError: (Throwable) -> Unit = {
            onError(mp, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
        runCatchingTyped(
            IOException::class to handleError,
            IllegalArgumentException::class to handleError,
        ) {
            if (audioSession != 0) {
                mp.audioSessionId = audioSession
            } else {
                audioSession = mp.audioSessionId
            }
            mp.setOnPreparedListener(this)
            mp.setOnVideoSizeChangedListener(this)
            mp.setOnCompletionListener(this)
            mp.setOnErrorListener(this)
            mp.setOnInfoListener(this)
            mp.setOnBufferingUpdateListener(this)
            currentBufferPercentage = 0
            mp.setDataSource(context, uri)
            mp.setSurface(surface)
            mp.setAudioAttributes(audioAttributes)
            mp.setScreenOnWhilePlaying(true)
            mp.prepareAsync()
            _currentState.value = State.PREPARING
        }
    }

    private fun release(clearTargetState: Boolean) {
        val mp = mediaPlayer ?: return
        mp.reset()
        mp.release()
        mediaPlayer = null
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

    override fun start() {
        if (isInPlaybackState) {
            mediaPlayer?.start()
            _currentState.value = State.PLAYING
        }
        targetState = State.PLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            val mp = mediaPlayer!!
            if (mp.isPlaying) {
                mp.pause()
                _currentState.value = State.PAUSED
            }
        }
        targetState = State.PAUSED
    }

    override fun getDuration(): Int {
        if (isInPlaybackState) {
            return mediaPlayer!!.duration
        }
        return -1
    }

    override fun getCurrentPosition(): Int {
        if (isInPlaybackState) {
            return mediaPlayer!!.currentPosition
        }
        return 0
    }

    override fun seekTo(pos: Int) {
        seekWhenPrepared = if (isInPlaybackState) {
            mediaPlayer?.seekTo(pos)
            0
        } else {
            pos
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && mediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        if (mediaPlayer !== null) {
            return currentBufferPercentage
        }
        return 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        if (audioSession == 0) {
            val mp = MediaPlayer()
            try {
                audioSession = mp.audioSessionId
            } finally {
                mp.release()
            }
        }
        return audioSession
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
