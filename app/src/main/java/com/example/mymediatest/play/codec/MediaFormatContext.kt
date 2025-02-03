package com.example.mymediatest.play.codec

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.net.Uri
import com.example.mymediatest.play.support.AVFormatContext
import com.example.mymediatest.utils.useTrack
import com.example.shared.utils.TAG
import com.example.shared.utils.TimeStamp
import com.example.shared.utils.cross
import com.example.shared.utils.logD

class MediaFormatContext(
    context: Context,
    uri: Uri,
) : AVFormatContext<MediaSupport> {

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
        }
    }

    fun read(stream: MediaStream): MediaPacket? {
        if (stream.posUs == Long.MAX_VALUE) {
            return null
        }
        val decoder = stream.decoder
        val (bufferIndex, buffer) = decoder.dequeueInputBuffer() ?: return null
        val trackIndex = streams.indexOf(stream)
        val (sampleSize, sampleTime, sampleFlags) = extractor.useTrack(trackIndex) {
            extractor.seekTo(stream.posUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            var sampleTime: Long
            var count = 0
            while (true) {
                sampleTime = extractor.sampleTime
                if (sampleTime < 0 || sampleTime > stream.posUs) {
                    break
                }
                extractor.advance()
                count++
            }
            TAG.logD { "read $trackIndex advance $count" }
            stream.posUs = sampleTime
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) {
                stream.posUs = Long.MAX_VALUE
                0 to 0L cross MediaCodec.BUFFER_FLAG_END_OF_STREAM
            } else {
                sampleSize to sampleTime cross extractor.sampleFlags
            }
        }
        return MediaPacket(
            bufferIndex = bufferIndex,
            buffer = buffer,
            sampleSize = sampleSize,
            sampleTime = sampleTime,
            sampleFlags = sampleFlags,
        )
    }
}
