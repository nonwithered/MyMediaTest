package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.example.mymediatest.R
import com.example.shared.utils.coroutineScope
import com.example.shared.utils.findView
import com.example.shared.utils.inflate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class PlayerProgressBar(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    init {
        inflate(R.layout.player_progress_bar, true)
    }

    val timestampCurrentState = MutableStateFlow(0L)
    val timestampTotalState = MutableStateFlow(100L)

    private val timestampCurrentTextView: TextView = findView(R.id.timestamp_current)!!
    private val timestampTotalTextView: TextView = findView(R.id.timestamp_total)!!
    private val progressLine: View = findView(R.id.progress_line)!!
    private val progressCursor: View = findView(R.id.progress_cursor)!!

    private var firstLayoutDone = false

    init {
        coroutineScope.launch {
            timestampCurrentState.collect {
                timestampCurrentTextView.text = it.convertText()
                refreshCursor()
            }
        }
        coroutineScope.launch {
            timestampTotalState.collect {
                timestampTotalTextView.text = it.convertText()
                refreshCursor()
            }
        }
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            firstLayoutDone = true
            postOnAnimation {
                refreshCursor()
            }
        }
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    val consume = detectSlide(event.x.roundToInt())
                    if (!consume && event.action == MotionEvent.ACTION_UP) {
                        performClick()
                    }
                    consume
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun detectSlide(x: Int): Boolean {
        if (!firstLayoutDone) {
            return false
        }
        if (x !in progressLine.left..progressLine.right) {
            return false
        }
        val offset = x - (progressLine.left + progressCursor.width / 2)
        val max = progressLine.width - progressCursor.width
        val fraction = offset.toDouble() / max.toDouble()
        val current = if (fraction.isNaN() || fraction.isInfinite()) {
            0
        } else {
            (fraction * timestampTotalState.value).roundToLong().coerceIn(0, timestampTotalState.value)
        }
        timestampCurrentState.value = current
        return true
    }

    private fun refreshCursor() {
        if (!firstLayoutDone) {
            return
        }
        val max = progressLine.width - progressCursor.width
        val fraction = timestampCurrentState.value.toDouble() / timestampTotalState.value.toDouble()
        val current = if (fraction.isNaN() || fraction.isInfinite()) {
            0
        } else {
            (max * fraction).roundToInt().coerceIn(0, max)
        }
        progressCursor.updateLayoutParams<MarginLayoutParams> {
            marginStart = current
        }
    }

    private fun Long.convertText(): String {
        val duration = coerceAtLeast(0)
        val unit = TimeUnit.MILLISECONDS
        val hour = unit.toHours(duration)
        val minute = unit.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hour)
        val second = unit.toSeconds(duration) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute)
        return if (hour <= 0) {
            listOf(minute, second)
        } else {
            listOf(hour, minute, second)
        }.joinToString(":") {
            convertString()
        }
    }

    private fun Long.convertString(): String {
        val str = toString()
        return if (str.length == 1) {
            "0$str"
        } else {
            str
        }
    }
}
