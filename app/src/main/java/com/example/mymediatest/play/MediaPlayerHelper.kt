package com.example.mymediatest.play

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.shared.utils.runCatchingTyped
import java.io.IOException

/**
 * @see android.media.MediaPlayer
 */
class MediaPlayerHelper(
    context: Context,
) : CommonPlayerHelper(context),
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {

    private class PlayerImpl(
        private val mp: MediaPlayer,
    ): CommonPlayer {

        override val isPlaying: Boolean
            get() = mp.isPlaying

        override val duration: Long
            get() = mp.duration.toLong()

        override val currentPosition: Long
            get() = mp.currentPosition.toLong()

        override fun seekTo(pos: Int) {
            mp.seekTo(pos)
        }

        override fun start() {
            mp.start()
        }

        override fun pause() {
            mp.pause()
        }

        override fun close() {
            mp.reset()
            mp.release()
        }
    }

    override fun openVideo(
        uri: Uri,
        surface: Surface,
        audioAttributes: AudioAttributes,
    ): CommonPlayer? {
        val mp = MediaPlayer()
        var impl: PlayerImpl? = null

        val handleError: (Throwable) -> Unit = {
            onError(mp, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
        runCatchingTyped(
            IOException::class to handleError,
            IllegalArgumentException::class to handleError,
        ) {
            mp.setOnPreparedListener(this)
            mp.setOnVideoSizeChangedListener(this)
            mp.setOnCompletionListener(this)
            mp.setOnErrorListener(this)
            mp.setDataSource(context, uri)
            mp.setSurface(surface)
            mp.setAudioAttributes(audioAttributes)
            mp.setScreenOnWhilePlaying(true)
            mp.prepareAsync()
            impl = PlayerImpl(mp)
        }
        return impl
    }

    override fun onPrepared(mp: MediaPlayer) {
        super.onPrepared(mp.videoWidth to mp.videoHeight)
    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        super.onVideoSizeChanged(width to height)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        super.onCompletion()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        super.onError()
        return true
    }
}
