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

/**
 * @see androidx.media3.exoplayer.ExoPlayerImplInternal.BUFFERING_MAXIMUM_INTERVAL_MS
 */
const val BUFFERING_MAXIMUM_INTERVAL_MS = 10L
