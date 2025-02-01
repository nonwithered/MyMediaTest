package com.example.mymediatest.play.codec

import android.content.Context
import android.net.Uri
import com.example.mymediatest.play.support.AVCodecContext
import com.example.mymediatest.play.support.AVFormatContext
import com.example.mymediatest.play.support.AVFrame
import com.example.mymediatest.play.support.AVPacket
import com.example.mymediatest.play.support.AVStream
import com.example.mymediatest.play.support.AVSupport
import com.example.shared.utils.TimeStamp
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

    override fun streams(formatContext: AVFormatContext<MediaSupport>): List<AVStream<MediaSupport>> {
        formatContext as MediaFormatContext
        return formatContext.streams
    }

    override fun decoder(stream: AVStream<MediaSupport>): AVCodecContext<MediaSupport> {
        stream as MediaStream
        return stream.decoder
    }

    override fun dts(packet: AVPacket<MediaSupport>): TimeStamp {
        packet as MediaPacket
        return packet.sampleTime to TimeUnit.MICROSECONDS
    }

    override fun pts(frame: AVFrame<MediaSupport>): TimeStamp {
        frame as MediaFrame
        return frame.presentationTimeUs to TimeUnit.MICROSECONDS
    }

    override fun seek(formatContext: AVFormatContext<MediaSupport>, t: TimeStamp) {
        formatContext as MediaFormatContext
        formatContext.seek(t)
    }

    override suspend fun read(formatContext: AVFormatContext<MediaSupport>, stream: AVStream<MediaSupport>): AVPacket<MediaSupport>? {
        formatContext as MediaFormatContext
        stream as MediaStream
        return formatContext.read(stream)
    }

    override fun send(
        codecContext: AVCodecContext<MediaSupport>,
        packet: AVPacket<MediaSupport>,
    ) {
        codecContext as MediaCodecContext
        packet as MediaPacket
        codecContext.send(packet)
    }

    override suspend fun receive(codecContext: AVCodecContext<MediaSupport>): AVFrame<MediaSupport> {
        codecContext as MediaCodecContext
        return codecContext.receive()
    }
}
