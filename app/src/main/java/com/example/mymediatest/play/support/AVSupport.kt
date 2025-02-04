package com.example.mymediatest.play.support

import android.content.Context
import android.net.Uri
import com.example.shared.utils.TimeStamp

interface AVSupport<T : AVSupport<T>> {

    fun open(
        context: Context,
        uri: Uri,
    ): AVFormatContext<T>

    fun AVFormatContext<T>.streams(): List<AVStream<T>>

    fun AVStream<T>.decoder(): AVCodecContext<T>

    fun AVStream<T>.duration(): TimeStamp

    fun AVStream<T>.mime(): String

    fun AVStream<T>.pos(): TimeStamp

    fun AVStream<T>.sampleRate(): Int?

    fun AVStream<T>.channelCount(): Int?

    fun AVStream<T>.pcmEncoding(): Int?

    fun AVStream<T>.width(): Int?

    fun AVStream<T>.height(): Int?

    fun AVFormatContext<T>.seek(t: TimeStamp)

    fun AVFormatContext<T>.read(stream: AVStream<T>): AVPacket<T>?

    fun AVCodecContext<T>.send(packet: AVPacket<T>): Boolean

    fun AVCodecContext<T>.receive(): AVFrame<T>?

    fun AVPacket<T>.pts(): TimeStamp

    fun AVPacket<T>.breakable(): Boolean

    fun AVFrame<T>.pts(): TimeStamp

    fun AVFrame<T>.offset(): Int

    fun AVFrame<T>.bytes(): ByteArray

    fun AVFrame<T>.consume(consumed: Int)

    fun AVFrame<T>.eos(): Boolean
}
