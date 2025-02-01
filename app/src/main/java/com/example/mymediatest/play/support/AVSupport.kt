package com.example.mymediatest.play.support

import android.content.Context
import android.net.Uri
import com.example.mymediatest.play.codec.MediaSupport
import com.example.shared.utils.TimeStamp

interface AVSupport<T : AVSupport<T>> {

    fun open(
        context: Context,
        uri: Uri,
    ): AVFormatContext<T>

    fun streams(formatContext: AVFormatContext<T>): List<AVStream<T>>

    fun decoder(stream: AVStream<T>): AVCodecContext<T>

    fun dts(packet: AVPacket<T>): TimeStamp

    fun pts(frame: AVFrame<T>): TimeStamp

    fun seek(formatContext: AVFormatContext<T>, t: TimeStamp)

    suspend fun read(formatContext: AVFormatContext<T>, stream: AVStream<MediaSupport>): AVPacket<MediaSupport>?

    fun send(codecContext: AVCodecContext<T>, packet: AVPacket<T>)

    suspend fun receive(codecContext: AVCodecContext<T>): AVFrame<T>
}
