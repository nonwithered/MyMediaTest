package com.example.mymediatest.play

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.os.HandlerThread
import android.os.SystemClock
import android.view.Surface
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.mymediatest.play.support.AVFrame
import com.example.mymediatest.play.support.AVStream
import com.example.mymediatest.play.support.AVSupport
import com.example.mymediatest.utils.isAudio
import com.example.mymediatest.utils.isVideo
import com.example.shared.utils.TAG
import com.example.shared.utils.asCloseable
import com.example.shared.utils.asCoroutineScope
import com.example.shared.utils.dispose
import com.example.shared.utils.forEach
import com.example.shared.utils.getValue
import com.example.shared.utils.logD
import com.example.shared.utils.setValue
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class CodecPlayerHelperController<T : AVSupport<T>>(
    context: Context,
    uri: Uri,
    surface: Surface,
    listener: CommonPlayerHelper.Listener,
    support: AVSupport<T>,
) : CommonPlayerHelper.Controller(
    context = context,
    uri = uri,
    surface = surface,
    listener = listener,
), AVSupport<T> by support {

    private interface Renderer : AutoCloseable {

        fun onRender(bytes: ByteArray, offset: Int): Int

        fun onStart()

        fun onPause()
    }

    private companion object {

        private const val MAX_BUFFER_SIZE = 10

        private const val SEEK_PREVIOUS_S = 1L

        /**
         * @see androidx.media3.exoplayer.ExoPlayerImplInternal.BUFFERING_MAXIMUM_INTERVAL_MS
         */
        const val BUFFERING_MAXIMUM_INTERVAL_MS = 10L
    }

    private val formatContext = open(
        context,
        uri,
    )

    private val decodeScope = HandlerThread("$TAG-decode").also {
        it.start()
    }.asCoroutineScope

    private val playScope = HandlerThread("$TAG-play").also {
        it.start()
    }.asCoroutineScope

    private val streams: List<AVStream<T>>
        get() = formatContext.streams()

    private val bufferChannels = streams.map {
        Channel<AVFrame<T>?>(
            capacity = MAX_BUFFER_SIZE,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )
    }

    private val renderes = streams.map {
        createRenderer(it)
    }

    private var playTask: AutoCloseable? = null

    override val isPlaying: Boolean
        get() = playTask !== null

    override val duration: Long = streams.map {
        it.duration()
    }.maxOf { (time, unit) ->
        unit.toMillis(time)
    }

    private val _currentPosition = AtomicLong(0)

    override var currentPosition: Long by _currentPosition

    private var decodeTask = decodeScope.launch {
        listener.onPrepared(0 to 0)
        performDecode()
    }

    private val elapsedRealtime: Long
        get() = SystemClock.elapsedRealtime()

    override fun seekTo(pos: Long) {
        decodeTask.cancel()
        val posMs = pos - TimeUnit.SECONDS.toMillis(SEEK_PREVIOUS_S)
        currentPosition = posMs
        formatContext.seek(posMs to TimeUnit.MILLISECONDS)
        decodeTask = decodeScope.launch {
            performDecode()
        }
    }

    override fun start() {
        if (isPlaying) {
            return
        }
        renderes.forEach { 
            it?.onStart()
        }
        playTask = playScope.launch {
            performPlay(elapsedRealtime - currentPosition.coerceIn(0L, duration))
        }::dispose.asCloseable
    }

    override fun pause() {
        playTask?.let {
            it.close()
            playTask = null
        }
        renderes.forEach {
            it?.onPause()
        }
    }

    override fun close() {
        playScope.cancel()
        decodeScope.cancel()
        formatContext.close()
        renderes.forEach {
            it?.close()
        }
    }

    private fun createRenderer(stream: AVStream<T>): Renderer? {
        val mime = stream.mime()
        return when {
            mime.isAudio -> AudioRender(
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    stream.sampleRate()!!,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioTrack.getMinBufferSize(stream.sampleRate()!!, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
                    AudioTrack.MODE_STREAM,
                )
            )
            mime.isVideo -> null // TODO
            else -> null
        }
    }

    private suspend fun performDecode() {
        coroutineScope {
            streams.forEachIndexed { index, stream ->
                val bufferChannel = bufferChannels[index]
                launch {
                    TAG.logD { "performDecode $index launch" }
                    while (true) {
                        if (!performDecode(stream, bufferChannel, index)) {
                            break
                        }
                    }
                }
            }
        }
    }

    private suspend fun performDecode(stream: AVStream<T>, bufferChannel: Channel<AVFrame<T>?>, index: Int): Boolean {
        val mime = stream.mime()
        val packet = formatContext.read(stream)
        TAG.logD { "performDecode $index read $mime" }
        if (packet === null) {
            bufferChannel.send(null)
            TAG.logD { "performDecode $index cancel" }
            return false
        }
        stream.decoder().send(packet)
        TAG.logD { "performDecode $index send $mime" }
        val buffer = stream.decoder().receive()
        TAG.logD { "performDecode $index receive $mime" }
        bufferChannel.send(buffer)
        return true
    }

    private suspend fun performPlay(startTimeMs: Long) {
        coroutineScope {
            streams.forEachIndexed { index, _ ->
                val renderer = renderes[index] ?: return@forEachIndexed
                val bufferChannel = bufferChannels[index]
                launch {
                    TAG.logD { "performPlay $index launch" }
                    bufferChannel.forEach {
                        performPlay(renderer, it, startTimeMs, index)
                    }
                }
            }
        }
        listener.onCompletion()
        currentPosition = 0
    }

    private suspend fun performPlay(renderer: Renderer, buffer: AVFrame<T>?, startTimeMs: Long, index: Int): Boolean {
        if (buffer === null) {
            TAG.logD { "performPlay $index cancel" }
            return false
        }
        val bytes = buffer.bytes()
        while (true) {
            val currentPosMs = currentPosition
            val ptsMs = buffer.ptsMs
            while (true) {
                val realPos = elapsedRealtime - startTimeMs
                if (realPos >= ptsMs) {
                    break
                }
                delay(ptsMs - realPos)
            }
            val offset = buffer.offset()
            val consumed = renderer.onRender(bytes, offset)
            if (offset + consumed >= bytes.size) {
                TAG.logD { "performPlay $index onRender $ptsMs" }
                if (renderer is AudioRender) {
                    _currentPosition.compareAndSet(currentPosMs, ptsMs)
                }
                break
            }
            buffer.consume(consumed)
        }
        return true
    }

    private val AVFrame<T>.ptsMs: Long
        get() {
            val (time, unit) = pts()
            return unit.toMillis(time)
        }

    private class AudioRender(
        private val audioTrack: AudioTrack,
    ): Renderer {

        override fun onRender(bytes: ByteArray, offset: Int): Int {
            return audioTrack.write(bytes, offset, bytes.size - offset)
        }

        override fun onStart() {
            audioTrack.play()
        }

        override fun onPause() {
            audioTrack.pause()
        }

        override fun close() {
            audioTrack.stop()
            audioTrack.release()
        }
    }
}
