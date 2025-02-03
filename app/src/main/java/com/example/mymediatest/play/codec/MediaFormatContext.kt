package com.example.mymediatest.play.codec

import android.content.Context
import android.media.MediaExtractor
import android.net.Uri
import com.example.mymediatest.play.support.AVFormatContext
import com.example.mymediatest.utils.useTrack
import com.example.shared.utils.TimeStamp
import com.example.shared.utils.cross

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
        val (sampleSize, sampleTime, sampleFlags) = extractor.useTrack(streams.indexOf(stream)) {
            extractor.seekTo(stream.posUs, MediaExtractor.SEEK_TO_NEXT_SYNC)
            val sampleSize = extractor.readSampleData(buffer, 0)
            val result = sampleSize to extractor.sampleTime cross extractor.sampleFlags
            if (sampleSize < 0) {
                stream.posUs = Long.MAX_VALUE
            } else {
                extractor.advance()
                stream.posUs = extractor.sampleTime
            }
            result
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
