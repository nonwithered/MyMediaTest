package com.example.mymediatest.play.codec

import android.media.MediaCodec
import android.media.MediaFormat
import com.example.mymediatest.play.support.AVCodecContext
import com.example.shared.utils.Tuple2
import com.example.shared.utils.Tuple3
import com.example.shared.utils.cross
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

    fun flush() {
        codec.flush()
    }

    fun dequeueInputBuffer(): Tuple2<Int, ByteBuffer>? {
        val bufferIndex = codec.dequeueInputBuffer(0)
        if (bufferIndex < 0) {
            return null
        }
        val buffer = codec.getInputBuffer(bufferIndex) ?: return null
        return bufferIndex to buffer
    }

    private fun dequeueOutputBuffer(): Tuple3<Int, ByteBuffer, MediaCodec.BufferInfo>? {
        val bufferInfo = MediaCodec.BufferInfo()
        val bufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
        if (bufferIndex < 0) {
            return null
        }
        val buffer = codec.getOutputBuffer(bufferIndex) ?: return null
        return bufferIndex to buffer cross bufferInfo
    }

    fun send(packet: MediaPacket): Boolean {
        codec.queueInputBuffer(
            packet.bufferIndex,
            0,
            packet.sampleSize,
            packet.sampleTime,
            packet.sampleFlags,
        )
        return (packet.sampleFlags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
    }

    fun receive(): MediaFrame? {
        val (bufferIndex, buffer, bufferInfo) = dequeueOutputBuffer() ?: return null
        val bytes = ByteArray(bufferInfo.size)
        buffer.get(bytes)
        codec.releaseOutputBuffer(bufferIndex, false)
        return MediaFrame(
            bytes = bytes,
            offset = bufferInfo.offset,
            presentationTimeUs = bufferInfo.presentationTimeUs,
            flags = bufferInfo.flags,
        )
    }
}
