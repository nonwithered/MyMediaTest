package com.example.mymediatest.play.codec

import com.example.mymediatest.play.support.AVFrame

class MediaFrame(
    val bytes: ByteArray,
    var offset: Int,
    val presentationTimeUs: Long,
    val flags: Int,
) : AVFrame<MediaSupport>
