package com.example.ffmpeg

import androidx.annotation.Keep

@Keep
object LibFFmpeg  {

    init {
        System.loadLibrary("ffmpeg")
    }
    init {
        registerNatives()
    }

    external fun registerNatives()

    external fun foo(): Int
    external fun bar(): Int
}
