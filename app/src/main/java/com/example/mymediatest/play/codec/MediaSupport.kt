package com.example.mymediatest.play.codec

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.net.Uri
import com.example.mymediatest.play.support.AVCodecContext
import com.example.mymediatest.play.support.AVFormatContext
import com.example.mymediatest.play.support.AVFrame
import com.example.mymediatest.play.support.AVPacket
import com.example.mymediatest.play.support.AVStream
import com.example.mymediatest.play.support.AVSupport
import com.example.shared.utils.TimeStamp
import com.example.shared.utils.elseEmpty
import java.util.concurrent.TimeUnit

object MediaSupport : AVSupport<MediaSupport> {

    override fun open(
        context: Context,
        uri: Uri,
    ): AVFormatContext<MediaSupport> {
        return MediaFormatContext(
            context = context,
            uri = uri,
        )
    }

    override fun AVFormatContext<MediaSupport>.streams(): List<AVStream<MediaSupport>> {
        val formatContext = this as MediaFormatContext
        return formatContext.streams
    }

    override fun AVStream<MediaSupport>.decoder(): AVCodecContext<MediaSupport> {
        val stream = this as MediaStream
        return stream.decoder
    }

    override fun AVStream<MediaSupport>.duration(): TimeStamp {
        val stream = this as MediaStream
        return stream.info.durationUs!! to TimeUnit.MICROSECONDS
    }

    override fun AVStream<MediaSupport>.mime(): String {
        val stream = this as MediaStream
        return stream.info.mime.elseEmpty
    }

    override fun AVStream<MediaSupport>.pos(): TimeStamp {
        val stream = this as MediaStream
        return stream.posUs to TimeUnit.MICROSECONDS
    }

    override fun AVStream<MediaSupport>.sampleRate(): Int? {
        val stream = this as MediaStream
        return stream.info.sampleRate
    }

    override fun AVStream<MediaSupport>.channelCount(): Int? {
        val stream = this as MediaStream
        return stream.info.channelCount
    }

    override fun AVStream<MediaSupport>.pcmEncoding(): Int? {
        val stream = this as MediaStream
        return stream.info.pcmEncoding
    }

    override fun AVStream<MediaSupport>.width(): Int? {
        val stream = this as MediaStream
        return stream.info.width
    }

    override fun AVStream<MediaSupport>.height(): Int? {
        val stream = this as MediaStream
        return stream.info.height
    }

    override fun AVFormatContext<MediaSupport>.seek(t: TimeStamp) {
        val formatContext = this as MediaFormatContext
        formatContext.seek(t)
    }

    override fun AVFormatContext<MediaSupport>.read(stream: AVStream<MediaSupport>): AVPacket<MediaSupport>? {
        val formatContext = this as MediaFormatContext
        stream as MediaStream
        return formatContext.read(stream)
    }

    override fun AVCodecContext<MediaSupport>.send(packet: AVPacket<MediaSupport>): Boolean {
        val codecContext = this as MediaCodecContext
        packet as MediaPacket
        return codecContext.send(packet)
    }

    override fun AVCodecContext<MediaSupport>.receive(): AVFrame<MediaSupport>? {
        val codecContext = this as MediaCodecContext
        return codecContext.receive()
    }

    override fun AVPacket<MediaSupport>.pts(): TimeStamp {
        val packet = this as MediaPacket
        return packet.sampleTime to TimeUnit.MICROSECONDS
    }

    override fun AVPacket<MediaSupport>.breakable(): Boolean {
        val packet = this as MediaPacket
        return (packet.sampleFlags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) == 0
    }

    override fun AVFrame<MediaSupport>.pts(): TimeStamp {
        val frame = this as MediaFrame
        return frame.presentationTimeUs to TimeUnit.MICROSECONDS
    }

    override fun AVFrame<MediaSupport>.offset(): Int {
        val frame = this as MediaFrame
        return frame.offset
    }

    override fun AVFrame<MediaSupport>.bytes(): ByteArray {
        val frame = this as MediaFrame
        return frame.bytes
    }

    override fun AVFrame<MediaSupport>.consume(consumed: Int) {
        val frame = this as MediaFrame
        frame.offset += consumed
    }

    override fun AVFrame<MediaSupport>.eos(): Boolean {
        val frame = this as MediaFrame
        return (frame.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
    }
}
