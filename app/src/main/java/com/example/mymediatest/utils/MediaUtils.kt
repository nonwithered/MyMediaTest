package com.example.mymediatest.utils

import android.media.MediaExtractor
import com.example.shared.utils.asCloseable

inline fun <R> MediaExtractor.useTrack(index: Int, block: () -> R): R {
    return {
        unselectTrack(index)
    }.asCloseable.use {
        selectTrack(index)
        block()
    }
}

val String.isAudio: Boolean
    get() = startsWith("audio/")

val String.isVideo: Boolean
    get() = startsWith("video/")
