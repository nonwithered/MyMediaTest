package com.example.mymediatest.play

import android.content.Context
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.mymediatest.utils.VideoViewHelper

/**
 * @see androidx.media3.ui.PlayerView
 */
class ExoPlayerHelper private constructor(
    context: Context,
    private val player: Player,
) : BasePlayerHelper(context),
    Player by player {

    constructor(
        context: Context,
    ) : this(
        context,
        ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = false
            },
    )

    override fun onInit(view: View) {
        super.onInit(view)
        when (view) {
            is SurfaceView -> {
                setVideoSurfaceView(view)
            }
            is TextureView -> {
                setVideoTextureView(view)
            }
        }
        addListener(object : Player.Listener {

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                requestLayout()
            }
        })
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        setMeasuredDimension: (width: Int, height: Int) -> Unit,
    ): Boolean {
        val (width, height) = VideoViewHelper.onMeasure(
            measureSpec = widthMeasureSpec to heightMeasureSpec,
            videoSize = videoSize.width to videoSize.height,
        )
        setMeasuredDimension(width, height)
        return true
    }
}
