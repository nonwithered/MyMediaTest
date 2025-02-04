package com.example.mymediatest.play

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.opengl.GLES20
import android.os.HandlerThread
import android.os.SystemClock
import android.view.Surface
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.mymediatest.play.support.AVFrame
import com.example.mymediatest.play.support.AVPacket
import com.example.mymediatest.play.support.AVStream
import com.example.mymediatest.play.support.AVSupport
import com.example.mymediatest.utils.isAudio
import com.example.mymediatest.utils.isVideo
import com.example.shared.utils.TAG
import com.example.shared.utils.Tuple2
import com.example.shared.utils.asCoroutineScope
import com.example.shared.utils.forEach
import com.example.shared.utils.getValue
import com.example.shared.utils.logD
import com.example.shared.utils.onDispose
import com.example.shared.utils.setValue
import com.example.shared.view.gl.checkGlError
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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

    private object EmptyRenderer : Renderer {

        override fun onRender(bytes: ByteArray, offset: Int): Int {
            return bytes.size - offset
        }

        override fun onStart() {
        }

        override fun onPause() {
        }

        override fun close() {
        }
    }

    private companion object {

        private const val MAX_BUFFER_SIZE = 10

        private const val SEEK_PREVIOUS_S = 1L

        /**
         * @see androidx.media3.exoplayer.ExoPlayerImplInternal.BUFFERING_MAXIMUM_INTERVAL_MS
         */
        const val BUFFERING_MAXIMUM_INTERVAL_MS = 10L
    }

    private data class FrameBuffer<T : AVSupport<T>>(
        val frame: AVFrame<T>?,
    )

    private val formatContext = open(
        context = context,
        uri = uri,
    )

    private val decodeScope = HandlerThread("$TAG-decode").also {
        it.start()
    }.asCoroutineScope

    private val playScope = HandlerThread("$TAG-play").also {
        it.start()
    }.asCoroutineScope

    private val streams = formatContext.streams()

    private val syncStreamIndex = streams.indexOfFirst {
        it.mime().isAudio
    }

    private val viewStreamIndex = streams.indexOfFirst {
        it.mime().isVideo
    }

    private val bufferChannels = streams.map {
        Channel<FrameBuffer<T>>(
            capacity = MAX_BUFFER_SIZE,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )
    }

    private val renders = streams.map {
        createRenderer(it)
    }

    private var playTask: Job? = null
    private var playPauseTask: Job? = null
    private var playStartTask: Job? = null

    override val isPlaying: Boolean
        get() = playTask !== null

    override val duration: Long = streams.map {
        it.duration()
    }.maxOf { (time, unit) ->
        unit.toMillis(time)
    }

    private val _playPosition = AtomicLong(0)

    private var playPosition: Long by _playPosition

    override val currentPosition: Long
        get() = playPosition.coerceAtLeast(0)

    private var decodeTask = decodeScope.launch {
        performDecode()
    }

    private val elapsedRealtime: Long
        get() = SystemClock.elapsedRealtime()

    init {
        val size = if (viewStreamIndex < 0) {
            0 to 0
        } else {
            val stream = streams[viewStreamIndex]
            stream.width()!! to stream.height()!!
        }
        listener.onPrepared(size)
    }

    override fun seekTo(pos: Long) {
        TAG.logD { "seekTo $pos" }

        val decodeJob = decodeTask
        decodeJob.cancel()

        val posMs = pos - TimeUnit.SECONDS.toMillis(SEEK_PREVIOUS_S)
        val oldPosMs = _playPosition.getAndSet(posMs)

        val syncJob = decodeScope.launch {
            decodeJob.join()
            formatContext.seek(posMs to TimeUnit.MILLISECONDS)
        }

        decodeTask = decodeScope.launch {
            syncJob.join()
            performDecode()
        }

        val playJob = playTask
        if (playJob !== null && oldPosMs > posMs) {
            playJob.cancel()
            val startTimeMs = elapsedRealtime - playPosition.coerceIn(0L, duration)
            playTask = playScope.launch {
                playJob.join()
                syncJob.join()
                performPlay(startTimeMs)
            }
        }
    }

    override fun start() {
        if (isPlaying) {
            return
        }
        performStart()
    }

    private fun performStart() {
        TAG.logD { "performStart" }
        val playPauseJob = playPauseTask
        val playStartJob = playScope.launch {
            playPauseJob?.join()
            renders.forEach {
                it.onStart()
            }
        }.also {
            playStartTask = it
        }
        val startTimeMs = elapsedRealtime - playPosition.coerceIn(0L, duration)
        playTask = playScope.launch {
            playStartJob.join()
            performPlay(startTimeMs)
        }
    }

    override fun pause() {
        if (!isPlaying) {
            return
        }
        performPause()
    }

    private fun performPause() {
        TAG.logD { "performPause" }
        val playStartJob = playStartTask
        val playJob = playTask?.also {
            it.cancel()
            playTask = null
        }
        playPauseTask = playScope.launch {
            playStartJob?.join()
            playJob?.join()
            renders.forEach {
                it.onPause()
            }
        }
    }

    override fun close() {
        if (isPlaying) {
            performPause()
        }
        val closePlayJob = playScope.launch {
            playPauseTask?.join()
            renders.forEach {
                it.close()
            }
        }
        playScope.cancel()
        val closeDecodeJob = decodeScope.launch {
            closePlayJob.join()
            formatContext.close()
        }
        decodeScope.cancel()
        runBlocking {
            closeDecodeJob.join()
        }
    }

    override fun asRenderer(): BasePlayerHelper.GLRenderer? {
        return renders.getOrNull(viewStreamIndex) as? BasePlayerHelper.GLRenderer
    }

    private fun createRenderer(stream: AVStream<T>): Renderer {
        val mime = stream.mime()
        val channelConfig = when (stream.channelCount()) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            else -> AudioFormat.CHANNEL_OUT_STEREO
        }
        val audioFormat = stream.pcmEncoding() ?: AudioFormat.ENCODING_PCM_16BIT
        return when {
            mime.isAudio -> AudioRender(
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    stream.sampleRate()!!,
                    channelConfig,
                    audioFormat,
                    AudioTrack.getMinBufferSize(stream.sampleRate()!!, channelConfig, audioFormat),
                    AudioTrack.MODE_STREAM,
                )
            )
            mime.isVideo -> VideoRender()
            else -> EmptyRenderer
        }
    }

    private suspend fun performDecode() {
        coroutineScope {
            streams.forEachIndexed { index, stream ->
                val bufferChannel = bufferChannels[index]
                launch {
                    val monitor = launch {
                        onDispose {
                            TAG.logD { "performDecode $index onDispose" }
                        }
                    }
                    TAG.logD { "performDecode $index launch" }
                    val eosStateRef = AtomicBoolean(false)
                    val lastPtsRef = AtomicLong(Long.MIN_VALUE)
                    val lastTimeRef = AtomicLong(Long.MIN_VALUE)
                    while (true) {
                        val end = performDecode(
                            index = index,
                            stream = stream,
                            bufferChannel = bufferChannel,
                            eosStateRef = eosStateRef,
                            lastPtsRef = lastPtsRef,
                            lastTimeRef = lastTimeRef,
                        )
                        if (end) {
                            break
                        }
                        yield()
                    }
                    TAG.logD { "performDecode $index break" }
                    monitor.cancel()
                }
            }
        }
    }

    private suspend fun performDecode(
        index: Int,
        stream: AVStream<T>,
        bufferChannel: Channel<FrameBuffer<T>>,
        eosStateRef: AtomicBoolean,
        lastPtsRef: AtomicLong,
        lastTimeRef: AtomicLong,
    ): Boolean {
        var eosState by eosStateRef
        var lastPts by lastPtsRef
        var lastTime by lastTimeRef
        val mime = stream.mime()
        var breakable = true
        while (!eosState) {
            val packet = formatContext.read(stream)
            val ptsMs = packet?.ptsMs
            TAG.logD { "performDecode $index packet read $mime $ptsMs $breakable" }
            if (packet === null) {
                if (breakable) {
                    break
                } else {
                    continue
                }
            }
            breakable = packet.breakable()
            val eos = stream.decoder().send(packet)
            if (eos) {
                eosState = true
                TAG.logD { "performDecode $index packet send eos $mime $ptsMs" }
            } else {
                TAG.logD { "performDecode $index packet send $mime $ptsMs" }
            }
        }
        while (true) {
            val curTime = elapsedRealtime
            val frame = stream.decoder().receive()
            val ptsMs = frame?.ptsMs
            TAG.logD { "performDecode $index frame receive $mime $ptsMs $eosState $lastTime $curTime" }
            val eos = if (ptsMs === null) {
                eosState && lastTime != Long.MIN_VALUE && lastTime + 100 < curTime
            } else {
                frame.eos() || ptsMs <= lastPts
            }
            if (eos) {
                bufferChannel.send(FrameBuffer(
                    frame = null,
                ))
                TAG.logD { "performDecode $index frame eos $mime $ptsMs" }
                return true
            }
            if (ptsMs === null) {
                break
            }
            lastPts = ptsMs
            lastTime = curTime
            bufferChannel.send(FrameBuffer(
                frame = frame,
            ))
        }
        return false
    }

    private suspend fun performPlay(startTimeMs: Long) {
        coroutineScope {
            streams.forEachIndexed { index, _ ->
                val renderer = renders[index]
                val bufferChannel = bufferChannels[index]
                launch {
                    val monitor = launch {
                        onDispose {
                            TAG.logD { "performPlay $index onDispose" }
                        }
                    }
                    TAG.logD { "performPlay $index launch" }
                    var startState = true
                    bufferChannel.forEach { buffer ->
                        val frame = buffer.frame
                        if (startState && frame === null) {
                            return@forEach true
                        }
                        if (startState && frame !== null && frame.ptsMs > elapsedRealtime - startTimeMs + 100) {
                            return@forEach true
                        }
                        startState = false
                        val end = performPlay(
                            index = index,
                            buffer = buffer,
                            renderer = renderer,
                            startTimeMs = startTimeMs,
                        )
                        yield()
                        !end
                    }
                    TAG.logD { "performPlay $index break" }
                    monitor.cancel()
                }
            }
        }
        listener.onCompletion()
        playPosition = 0
    }

    private suspend fun performPlay(
        index: Int,
        buffer: FrameBuffer<T>,
        renderer: Renderer,
        startTimeMs: Long,
    ): Boolean {
        val frame = buffer.frame
        if (frame === null) {
            TAG.logD { "performPlay $index render eos" }
            return true
        }
        val ptsMs = frame.ptsMs
        TAG.logD { "performPlay $index render frame $ptsMs" }
        while (true) {
            val currentPosMs = playPosition
            while (true) {
                val realPos = elapsedRealtime - startTimeMs
                if (realPos >= ptsMs) {
                    break
                }
                val delayMs = ptsMs - realPos
                TAG.logD { "performPlay $index render delay $delayMs" }
                delay(delayMs)
            }
            val offset = frame.offset()
            val bytes = frame.bytes()
            val consumed = renderer.onRender(bytes, offset)
            TAG.logD { "performPlay $index render consumed $ptsMs $offset ${bytes.size} $consumed" }
            if (offset + consumed >= bytes.size) {
                if (index == syncStreamIndex) {
                    _playPosition.compareAndSet(currentPosMs, ptsMs)
                }
                break
            }
            frame.consume(consumed)
        }
        return false
    }

    private val AVPacket<T>.ptsMs: Long
        get() {
            val (time, unit) = pts()
            return unit.toMillis(time)
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

    private class VideoRender : Renderer, BasePlayerHelper.GLRenderer {

        @Volatile
        private var frame: Tuple2<ByteArray, Int>? = null

        @Volatile
        private var isPlaying = false

        override fun onRender(bytes: ByteArray, offset: Int): Int {
            frame = bytes to offset
            return bytes.size - offset
        }

        override fun onStart() {
            isPlaying = true
        }

        override fun onPause() {
            isPlaying = false
        }

        override fun close() {
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFuncSeparate(
                GLES20.GL_SRC_ALPHA,
                GLES20.GL_ONE_MINUS_SRC_ALPHA,
                GLES20.GL_ONE,
                GLES20.GL_ONE_MINUS_SRC_ALPHA,
            )
            checkGlError()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            checkGlError()
        }

        override fun onDrawFrame(gl: GL10?) {
            if (!isPlaying) {
                return
            }
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            checkGlError()
        }
    }
}
