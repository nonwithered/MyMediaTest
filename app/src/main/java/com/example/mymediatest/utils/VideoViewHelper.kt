package com.example.mymediatest.utils

import android.view.SurfaceView.getDefaultSize
import android.view.View.MeasureSpec
import com.example.shared.utils.Vec2

object VideoViewHelper {

    /**
     * @see android.widget.VideoView.onMeasure
     */
    fun onMeasure(
        measureSpec: Vec2<Int>,
        videoSize: Vec2<Int>,
    ): Vec2<Int> {
        val (widthMeasureSpec, heightMeasureSpec) = measureSpec
        val (videoWidth, videoHeight) = videoSize

        if (videoWidth <= 0 || videoHeight <= 0) {
            val width = getDefaultSize(videoWidth, widthMeasureSpec)
            val height = getDefaultSize(videoHeight, heightMeasureSpec)
            return width to height
        }

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
            var width = widthSpecSize
            var height = heightSpecSize

            if (videoWidth * height < width * videoHeight) {
                width = height * videoWidth / videoHeight
            }
            if (videoWidth * height > width * videoHeight) {
                height = width * videoHeight / videoWidth
            }
            return width to height
        }
        if (widthSpecMode == MeasureSpec.EXACTLY) {
            val width = widthSpecSize
            var height = width * videoHeight / videoWidth
            if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                height = heightSpecSize
            }
            return width to height
        }
        if (heightSpecMode == MeasureSpec.EXACTLY) {
            val height = heightSpecSize
            var width = height * videoWidth / videoHeight
            if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                width = widthSpecSize
            }
            return width to height
        }
        var width = videoWidth
        var height = videoHeight
        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
            height = heightSpecSize
            width = height * videoWidth / videoHeight
        }
        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
            width = widthSpecSize
            height = width * videoHeight / videoWidth
        }
        return width to height
    }
}