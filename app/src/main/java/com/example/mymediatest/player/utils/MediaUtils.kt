package com.example.mymediatest.player.utils

import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.shared.utils.Vec2
import com.example.shared.utils.app

val Uri.type: String?
    get() = app.contentResolver.getType(this)

private val MediaMetadataRetriever.size: Vec2<Int>?
    get() = runCatching {
        val width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
        val height = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
        width to height
    }.getOrNull()

val Uri.size: Vec2<Int>?
    get() = MediaMetadataRetriever().use {
        it.setDataSource(app, this)
        it.size
    }
