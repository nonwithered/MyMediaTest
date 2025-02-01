package com.example.mymediatest.play

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import com.example.mymediatest.play.base.CommonPlayerHelper

/**
 * @see android.media.MediaPlayer
 */
class MediaPlayerHelperController(
    context: Context,
    uri: Uri,
    surface: Surface,
    listener: CommonPlayerHelper.Listener,
) : CommonPlayerHelper.Controller(
    context = context,
    uri = uri,
    surface = surface,
    listener = listener,
) {

    private val mp = MediaPlayer().apply {
        setOnPreparedListener { _ ->
            listener.onPrepared(videoWidth to videoHeight)
        }
        setOnVideoSizeChangedListener { _, width, height ->
            listener.onVideoSizeChanged(width to height)
        }
        setOnCompletionListener { _ ->
            listener.onCompletion()
        }
        setOnErrorListener { _, what, extra ->
            listener.onError(RuntimeException("OnErrorListener what=$what extra=$extra"))
            true
        }
        setDataSource(context, uri)
        setSurface(surface)
        setScreenOnWhilePlaying(true)
        prepareAsync()
    }

    override val isPlaying: Boolean
        get() = mp.isPlaying

    override val duration: Long
        get() = mp.duration.toLong()

    override val currentPosition: Long
        get() = mp.currentPosition.toLong()

    override fun seekTo(pos: Long) {
        mp.seekTo(pos.toInt())
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
