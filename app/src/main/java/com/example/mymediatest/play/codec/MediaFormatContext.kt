package com.example.mymediatest.play.codec

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.net.Uri
import com.example.mymediatest.play.codec.MediaSupport.mime
import com.example.mymediatest.play.support.AVFormatContext
import com.example.shared.utils.TAG
import com.example.shared.utils.TimeStamp
import com.example.shared.utils.logD

class MediaFormatContext(
    context: Context,
    uri: Uri,
) : AVFormatContext<MediaSupport> {

    private var lastTrackIndex = -1

    private val extractor = MediaExtractor().apply {
        setDataSource(context, uri, null)
    }

    val streams: List<MediaStream> = (0 until extractor.trackCount).map {
        MediaStream(
            format = extractor.getTrackFormat(it),
        )
    }

    override fun close() {
        streams.forEach {
            it.decoder.close()
        }
        extractor.release()
    }

    fun seek(t: TimeStamp) {
        val (time, unit) = t
        streams.forEach {
            it.posUs = unit.toMicros(time)
            it.needSeek = true
        }
    }

    fun read(stream: MediaStream): MediaPacket? {
        if (stream.posUs == Long.MAX_VALUE) {
            return null
        }
        val decoder = stream.decoder
        val (bufferIndex, buffer) = decoder.dequeueInputBuffer() ?: return null
        val trackIndex = streams.indexOf(stream)
        var sampleTime: Long
        if (trackIndex == lastTrackIndex && !stream.needSeek) {
            extractor.advance()
            sampleTime = extractor.sampleTime
        } else {
            if (lastTrackIndex >= 0) {
                extractor.unselectTrack(lastTrackIndex)
            }
            lastTrackIndex = trackIndex
            extractor.selectTrack(trackIndex)
            extractor.seekTo(stream.posUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            var count = 0
            while (true) {
                sampleTime = extractor.sampleTime
                if (sampleTime < 0 || sampleTime > stream.posUs) {
                    break
                }
                extractor.advance()
                count++
            }
            TAG.logD { "read $trackIndex ${stream.mime()} advance $count" }
        }
        stream.posUs = sampleTime
        val sampleSize = extractor.readSampleData(buffer, 0)
        if (sampleSize < 0) {
            stream.posUs = Long.MAX_VALUE
            return MediaPacket(
                bufferIndex = bufferIndex,
                buffer = buffer,
                sampleSize = 0,
                sampleTime = 0,
                sampleFlags = MediaCodec.BUFFER_FLAG_END_OF_STREAM,
            )
        }
        return MediaPacket(
            bufferIndex = bufferIndex,
            buffer = buffer,
            sampleSize = sampleSize,
            sampleTime = sampleTime,
            sampleFlags = extractor.sampleFlags,
        )
    }
}
