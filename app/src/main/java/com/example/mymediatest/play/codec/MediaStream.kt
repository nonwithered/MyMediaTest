package com.example.mymediatest.play.codec

import android.media.MediaCodec
import android.media.MediaFormat
import com.example.mymediatest.play.support.AVStream
import com.example.shared.bean.MediaFormatProperties

class MediaStream(
    format: MediaFormat,
) : AVStream<MediaSupport> {

    var posUs = -1L

    var needSeek = false

    class TrackInfo(
        val format: MediaFormat,
    ) : MediaFormatProperties(format) {

        val mime: String? by MediaFormat.KEY_MIME.property()
        val durationUs: Long? by MediaFormat.KEY_DURATION.property()
        var sampleRate: Int? by MediaFormat.KEY_SAMPLE_RATE.property()
        var channelCount: Int? by MediaFormat.KEY_CHANNEL_COUNT.property()
        var pcmEncoding: Int? by MediaFormat.KEY_PCM_ENCODING.property()
    }

    val info = TrackInfo(format)

    val decoder = MediaCodecContext(
        format = info.format,
        codec = MediaCodec.createDecoderByType(info.mime!!),
    )
}
