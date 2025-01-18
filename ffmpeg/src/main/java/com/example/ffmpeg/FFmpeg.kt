package com.example.ffmpeg

import androidx.annotation.Keep

@Keep
object FFmpeg  {

    init {
        System.loadLibrary("ffmpeg")
    }
    init {
        registerNatives()
    }

    external fun registerNatives()
}
