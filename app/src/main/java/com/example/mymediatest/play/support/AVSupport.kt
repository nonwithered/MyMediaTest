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

    fun AVFormatContext<T>.seek(t: TimeStamp)

    suspend fun AVFormatContext<T>.read(stream: AVStream<T>): AVPacket<T>?

    fun AVCodecContext<T>.send(packet: AVPacket<T>)

    suspend fun AVCodecContext<T>.receive(): AVFrame<T>

    fun AVFrame<T>.pts(): TimeStamp

    fun AVFrame<T>.offset(): Int

    fun AVFrame<T>.bytes(): ByteArray

    fun AVFrame<T>.consume(consumed: Int)
}
