package com.example.ffmpeg

import androidx.annotation.Keep

@Keep
object LibFFmpeg  {

    init {
        System.loadLibrary("ffmpeg")
    }

    external fun registerNatives()
}
