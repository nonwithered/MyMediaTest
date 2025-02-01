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
object MediaPlayerHelperFactory : CommonPlayerHelper.Factory {

    private class PlayerImpl(
        private val mp: MediaPlayer,
    ): CommonPlayerHelper.Controller {

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
        context: Context,
        uri: Uri,
        surface: Surface,
        audioAttributes: AudioAttributes,
        listener: CommonPlayerHelper.Listener,
    ): CommonPlayerHelper.Controller? {
        return runCatchingTyped(
            IOException::class to listener::onError,
            IllegalArgumentException::class to listener::onError,
        ) {
            val mp = MediaPlayer()
            mp.setOnPreparedListener { _ ->
                listener.onPrepared(mp.videoWidth to mp.videoHeight)
            }
            mp.setOnVideoSizeChangedListener { _, width, height ->
                listener.onVideoSizeChanged(width to height)
            }
            mp.setOnCompletionListener { _ ->
                listener.onCompletion()
            }
            mp.setOnErrorListener { _, what, extra ->
                listener.onError(RuntimeException("OnErrorListener what=$what extra=$extra"))
                true
            }
            mp.setDataSource(context, uri)
            mp.setSurface(surface)
            mp.setAudioAttributes(audioAttributes)
            mp.setScreenOnWhilePlaying(true)
            mp.prepareAsync()
            PlayerImpl(mp)
        }.getOrNull()
    }
}
