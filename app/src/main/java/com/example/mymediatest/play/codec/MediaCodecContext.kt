package com.example.mymediatest.play.codec

import android.media.MediaCodec
import android.media.MediaFormat
import com.example.mymediatest.play.support.AVCodecContext
import com.example.shared.utils.Tuple2
import com.example.shared.utils.Tuple3
import com.example.shared.utils.cross
import kotlinx.coroutines.yield
import java.nio.ByteBuffer

class MediaCodecContext(
    format: MediaFormat,
    private val codec: MediaCodec,
) : AVCodecContext<MediaSupport>,
    AutoCloseable {

    init {
        codec.configure(format, null, null, 0)
        codec.start()
    }

    override fun close() {
        codec.stop()
        codec.release()
    }

    suspend fun dequeueInputBuffer(): Tuple2<Int, ByteBuffer> {
        var bufferIndex: Int
        while (true) {
            bufferIndex = codec.dequeueInputBuffer(0)
            if (bufferIndex >= 0) {
                break
            }
            yield()
        }
        return bufferIndex to codec.getInputBuffer(bufferIndex)!!
    }

    private suspend fun dequeueOutputBuffer(): Tuple3<Int, ByteBuffer, MediaCodec.BufferInfo> {
        var bufferIndex: Int
        val bufferInfo = MediaCodec.BufferInfo()
        while (true) {
            bufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            if (bufferIndex >= 0) {
                break
            }
            yield()
        }
        return bufferIndex to codec.getOutputBuffer(bufferIndex)!! cross bufferInfo
    }

    fun eos(bufferIndex: Int) {
        codec.queueInputBuffer(
            bufferIndex,
            0,
            0,
            0,
            MediaCodec.BUFFER_FLAG_END_OF_STREAM,
        )
    }

    fun send(packet: MediaPacket) {
        codec.queueInputBuffer(
            packet.bufferIndex,
            0,
            packet.sampleSize,
            packet.sampleTime,
            packet.sampleFlags,
        )
    }

    suspend fun receive(): MediaFrame {
        val (bufferIndex, buffer, bufferInfo) = dequeueOutputBuffer()
        val bytes = ByteArray(bufferInfo.size)
        buffer.get(bytes)
        codec.releaseOutputBuffer(bufferIndex, false)
        return MediaFrame(
            bytes = bytes,
            offset = bufferInfo.offset,
            presentationTimeUs = bufferInfo.presentationTimeUs,
        )
    }
}
