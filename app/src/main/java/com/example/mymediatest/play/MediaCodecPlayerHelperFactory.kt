package com.example.mymediatest.play

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.view.Surface
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.shared.bean.MediaFormatProperties
import com.example.mymediatest.utils.useTrack
import com.example.shared.utils.TAG
import com.example.shared.utils.elseEmpty
import com.example.shared.utils.elseFalse
import com.example.shared.utils.elseZero
import com.example.shared.utils.logD
import com.example.shared.utils.logE
import com.example.shared.utils.mainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

object MediaCodecPlayerHelperFactory : CommonPlayerHelper.Factory {

    private class TrackFormat(
        val index: Int,
        format: MediaFormat = MediaFormat(),
    ) : MediaFormatProperties(format) {

        var mime: String? by MediaFormat.KEY_MIME.property()
        var codecsString: String? by MediaFormat.KEY_CODECS_STRING.property()
        var lowLatency: Int? by MediaFormat.KEY_LOW_LATENCY.property()
        var language: String? by MediaFormat.KEY_LANGUAGE.property()
        var captionServiceNumber: Int? by MediaFormat.KEY_CAPTION_SERVICE_NUMBER.property()
        var sampleRate: Int? by MediaFormat.KEY_SAMPLE_RATE.property()
        var channelCount: Int? by MediaFormat.KEY_CHANNEL_COUNT.property()
        var width: Int? by MediaFormat.KEY_WIDTH.property()
        var height: Int? by MediaFormat.KEY_HEIGHT.property()
        var maxWidth: Int? by MediaFormat.KEY_MAX_WIDTH.property()
        var maxHeight: Int? by MediaFormat.KEY_MAX_HEIGHT.property()
        var maxInputSize: Int? by MediaFormat.KEY_MAX_INPUT_SIZE.property()
        var bitrate: Int? by MediaFormat.KEY_BIT_RATE.property()
        var colorFormat: Int? by MediaFormat.KEY_COLOR_FORMAT.property()
        var frameRate: Int? by MediaFormat.KEY_FRAME_RATE.property()
        var tileWidth: Int? by MediaFormat.KEY_TILE_WIDTH.property()
        var tileHeight: Int? by MediaFormat.KEY_TILE_HEIGHT.property()
        var durationUs: Long? by MediaFormat.KEY_DURATION.property()
    }

    private class TrackDecoder(
        val format: TrackFormat,
        val codec: MediaCodec,
    ) : AutoCloseable {

        val buffer = ConcurrentLinkedQueue<SampleData>()
        var nextTime = 0L

        init {
            val fmt = format.asMediaFormat()
            codec.configure(fmt, null, null, 0)
            codec.start()
        }

        override fun close() {
            codec.stop()
            codec.release()
        }
    }

    private class MediaDecoder(
        val extractor: MediaExtractor,
        val track: List<TrackDecoder>,
    ) : AutoCloseable {

        override fun close() {
            track.forEach {
                it.close()
            }
            extractor.release()
        }
    }

    private class DecodeWorker(
        val decoder: MediaDecoder,
        val listener: CommonPlayerHelper.Listener,
    ) : Runnable {

        private val timeoutUs = TimeUnit.SECONDS.toMicros(1)

        @Volatile
        var shouldExit = false

        override fun run() = try {
            mainScope.launch {
                listener.onPrepared(0 to 0)
            }
            TAG.logD { "runLoop begin ${decoder.track.size}" }
            while (!shouldExit) {
                val nonStop = runOnce()
                if (!nonStop) {
                    TAG.logD { "runLoop stop" }
                    break
                }
            }
            TAG.logD { "runLoop shouldExit" }
        } catch (e: Throwable) {
            TAG.logE(e) { "runLoop catch" }
        } finally {
            TAG.logD { "runLoop end" }
            decoder.close()
        }

        private fun runOnce(): Boolean {
            TAG.logD { "runOnce ${decoder.track.size}" }
            var nonStop = false
            decoder.track.forEachIndexed { index, it ->
                TAG.logD { "runOnce forEach $index ${it.format.mime}" }
                nonStop = decodeTrack(decoder.extractor, it) || nonStop
            }
            return nonStop
        }

        private fun decodeTrack(
            extractor: MediaExtractor,
            trackDecoder: TrackDecoder,
        ): Boolean {
            val index = trackDecoder.format.index
            val codec = trackDecoder.codec
            TAG.logD { "decodeTrack $index $index ${trackDecoder.format.mime}" }
            val inputIndex = codec.dequeueInputBuffer(timeoutUs)
            TAG.logD { "decodeTrack $index inputIndex $inputIndex" }
            if (inputIndex < 0) {
                return true
            }
            val inputBuffer = codec.getInputBuffer(inputIndex)!!
            extractor.useTrack(index) {
                extractor.seekTo(trackDecoder.nextTime, MediaExtractor.SEEK_TO_NEXT_SYNC)
                TAG.logD { "decodeTrack $index sampleTime ${extractor.sampleTime} ${extractor.sampleFlags}" }
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    return false
                } else {
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, extractor.sampleFlags)
                }
            }
            val bufferInfo = MediaCodec.BufferInfo()
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            TAG.logD { "decodeTrack $index outputIndex $outputIndex" }
            if (outputIndex < 0) {
                return true
            }
            val outputBuffer = codec.getOutputBuffer(outputIndex)!!
            val bytes = ByteArray(bufferInfo.size)
            outputBuffer.get(bytes)
            codec.releaseOutputBuffer(outputIndex, false)

            val nextTime = extractor.useTrack(index) {
                extractor.seekTo(trackDecoder.nextTime, MediaExtractor.SEEK_TO_NEXT_SYNC)
                extractor.advance()
                extractor.sampleTime
            }
            TAG.logD { "decodeTrack $index nextTime $nextTime" }
            trackDecoder.buffer.offer(
                SampleData(
                    sampleTime = nextTime,
                    bytes = bytes,
                )
            )
            trackDecoder.nextTime = nextTime
            TAG.logD { "decodeTrack $index bufferInfo ${bufferInfo.size} ${bufferInfo.presentationTimeUs} ${bufferInfo.flags}" }
            return true
        }
    }

    private class SampleData(
        val sampleTime: Long,
        val bytes: ByteArray,
    )

    private class PlayerImpl(
        private val decodeWorker: DecodeWorker,
    ) : CommonPlayerHelper.Controller {

        private val audioTrack: AudioTrack

        init {
            Thread(decodeWorker).start()
            val music = findAudioTrack()!!
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                music.format.sampleRate!!,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(music.format.sampleRate!!, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
                AudioTrack.MODE_STREAM,
            )
        }

        private var render: RenderThread? = null

        override val isPlaying: Boolean
            get() = render?.isResume.elseFalse

        override val duration: Long
            get() = TimeUnit.MICROSECONDS.toMillis(decodeWorker.decoder.track.maxOf { it.format.durationUs.elseZero })

        override val currentPosition: Long
            get() = TimeUnit.MICROSECONDS.toMillis(render?.currentPosition.elseZero)

        override fun seekTo(pos: Int) {
            val position = TimeUnit.MILLISECONDS.toMicros(pos.toLong())
            render?.currentPosition = position
        }

        override fun start() {
            audioTrack.play()
            render?.run {
                isResume = true
                isFinished = false
                return
            }
            render = RenderThread(findAudioTrack()!!, audioTrack)
            Thread(render!!).start()
        }

        override fun pause() {
            audioTrack.pause()
            render?.isResume = false
        }

        override fun close() {
            decodeWorker.shouldExit = true
            render?.shouldExit = true
            audioTrack.stop()
            audioTrack.release()
        }

        private fun findAudioTrack(): TrackDecoder? {
            return decodeWorker.decoder.track.firstOrNull {
                it.format.mime.elseEmpty.startsWith("audio/")
            }
        }

        private inner class RenderThread(
            val decodeTrack: TrackDecoder,
            val audioTrack: AudioTrack,
        ) : Runnable {

            @Volatile
            var isResume = true

            @Volatile
            var currentPosition = 0L

            @Volatile
            var shouldExit = false

            @Volatile
            var isFinished = false

            override fun run() {
                while (!shouldExit) {
                    val pos = TimeUnit.MICROSECONDS.toMillis(currentPosition)
                    if (pos >= duration) {
                        if (!isFinished) {
                            isFinished = true
                            mainScope.launch {
                                decodeWorker.listener.onCompletion()
                            }
                        }
                        continue
                    }
                    if (!isResume) {
                        continue
                    }
                    runOnce()
                }
            }

            private fun runOnce() {
                var last: SampleData? = null
                for (it in decodeTrack.buffer) {
                    if (currentPosition <= 0) {
                        last = it
                        break
                    }
                    if ((last === null || last.sampleTime <= currentPosition) && it.sampleTime > currentPosition) {
                        last = it
                        break
                    }
                    last = it
                }
                last ?: return
                currentPosition = last.sampleTime // TODO: fix seek
                val bytes = last.bytes
                TAG.logD { "audioTrack write $currentPosition ${bytes.size}" }
                audioTrack.write(bytes, 0, bytes.size)
            }
        }
    }

    override fun openVideo(
        context: Context,
        uri: Uri,
        surface: Surface,
        audioAttributes: AudioAttributes,
        listener: CommonPlayerHelper.Listener,
    ): CommonPlayerHelper.Controller {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        val track = (0 until extractor.trackCount).map {
            val format = TrackFormat(
                index = it,
                format = extractor.getTrackFormat(it),
            )
            TAG.logD { "openVideo $it ${format.mime}" }
            format
        }.map {
            TrackDecoder(
                format = it,
                codec = MediaCodec.createDecoderByType(it.mime!!),
            )
        }
        val decoder = MediaDecoder(
            extractor = extractor,
            track = track,
        )
        val player = PlayerImpl(
            decodeWorker = DecodeWorker(decoder, listener),
        )
        return player
    }
}
