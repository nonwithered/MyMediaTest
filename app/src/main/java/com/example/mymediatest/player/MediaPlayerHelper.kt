package com.example.mymediatest.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.Surface
import android.view.View
import android.widget.MediaController
import com.example.shared.utils.TAG
import com.example.shared.utils.asConst
import com.example.shared.utils.autoViewScope
import com.example.shared.utils.logD
import com.example.shared.utils.logI
import com.example.shared.utils.runCatchingTyped
import com.example.shared.utils.systemService
import com.example.shared.utils.withOwnerCollect
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/**
 * @see android.widget.VideoView
 */
class MediaPlayerHelper(
    context: Context,
) :
    BasePlayer(context),
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

    private val _currentState = MutableStateFlow(State.IDLE)
    val currentState = _currentState.asConst

    private var targetState = State.IDLE

    override var surface: Surface? = null
        set(value) {
            field = value
            TAG.logD { "surface set $value" }
            if (value === null) {
                release(true)
            } else {
                openVideo()
            }
        }

    override var surfaceSize
        get() = super.surfaceSize
        set(value) {
            super.surfaceSize = value
            val isValidState = targetState == State.PLAYING
            val hasValidSize = videoSize.value == value
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
        view.autoViewScope.withOwnerCollect(this, uri) { it, owner ->
            owner.TAG.logD { "uri get $it" }
            owner.seekWhenPrepared = 0
            owner.openVideo()
            owner.view.requestLayout()
            owner.view.invalidate()
        }
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
        videoSize.value = mp.videoWidth to mp.videoHeight
        seekWhenPrepared.takeIf { pos -> pos != 0 }?.let { pos -> seekTo(pos) }
        if (videoSize.value.first == 0 || videoSize.value.second == 0) {
            if (targetState == State.PLAYING) {
                start()
            }
        } else if (videoSize.value == surfaceSize) {
            if (targetState == State.PLAYING) {
                start()
            }
        }
    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        videoSize.value = mp.videoWidth to mp.videoHeight
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
        val uri = uri.value ?: return
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
}
