package com.example.mymediatest.play.codec

import android.media.MediaCodec
import android.media.MediaFormat
import com.example.mymediatest.play.support.AVStream
import com.example.shared.bean.MediaFormatProperties

class MediaStream(
    format: MediaFormat,
) : AVStream<MediaSupport> {

    @Volatile
    var posUs = -1L

    class TrackInfo(
        val format: MediaFormat,
    ) : MediaFormatProperties(format) {

        val mime: String? by MediaFormat.KEY_MIME.property()
        val durationUs: Long? by MediaFormat.KEY_DURATION.property()
        var sampleRate: Int? by MediaFormat.KEY_SAMPLE_RATE.property()
    }

    val info = TrackInfo(format)

    val decoder = MediaCodecContext(
        format = info.format,
        codec = MediaCodec.createDecoderByType(info.mime!!),
    )
}
