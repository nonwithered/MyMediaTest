package com.example.mymediatest.play.codec

import com.example.mymediatest.play.support.AVPacket
import java.nio.ByteBuffer

class MediaPacket(
    val bufferIndex: Int,
    val buffer: ByteBuffer,
    val sampleSize: Int,
    val sampleTime: Long,
    val sampleFlags: Int,
) : AVPacket<MediaSupport>
