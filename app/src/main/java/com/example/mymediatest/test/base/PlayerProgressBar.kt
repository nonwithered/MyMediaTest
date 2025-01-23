package com.example.mymediatest.test.base

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.example.mymediatest.R
import com.example.shared.utils.TAG
import com.example.shared.utils.addOnLayoutChangeListenerAdapter
import com.example.shared.utils.asConst
import com.example.shared.utils.autoAttachScope
import com.example.shared.utils.findView
import com.example.shared.utils.inflate
import com.example.shared.utils.logD
import com.example.shared.utils.bind
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class PlayerProgressBar(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    init {
        inflate(R.layout.case_player_progress_bar, true)
    }

    val currentPosition = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)
    private val _isSeekDragging = MutableStateFlow(false)
    val isSeekDragging = _isSeekDragging.asConst

    private val timestampCurrentTextView: TextView = findView(R.id.timestamp_current)!!
    private val timestampTotalTextView: TextView = findView(R.id.timestamp_total)!!
    private val progressLine: View = findView(R.id.progress_line)!!
    private val progressCursor: View = findView(R.id.progress_cursor)!!

    private var firstLayoutDone = false

    init {
        autoAttachScope.launch {
            bind(currentPosition) {
                TAG.logD { "currentPosition get $it" }
                timestampCurrentTextView.text = it.convertText()
                refreshCursor()
            }
            bind(duration) {
                timestampTotalTextView.text = it.convertText()
                refreshCursor()
            }
        }
        addOnLayoutChangeListenerAdapter { _, rect, oldRect ->
            firstLayoutDone = true
            if (rect.left != oldRect.left || rect.right != oldRect.right) {
                postOnAnimation {
                    refreshCursor()
                }
            }
        }
        isClickable = true
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> {
                    detectSlide(event.x.roundToInt())?.also { current ->
                        _isSeekDragging.value = true
                        currentPosition.value = current
                    } !== null
                }
                MotionEvent.ACTION_UP -> {
                    performClick()
                    _isSeekDragging.value = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    _isSeekDragging.value = false
                    false
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun detectSlide(x: Int): Long? {
        if (!firstLayoutDone) {
            return null
        }
        if (x !in progressLine.left..progressLine.right) {
            return null
        }
        val offset = x - (progressLine.left + progressCursor.width / 2)
        val max = progressLine.width - progressCursor.width
        val fraction = offset.toDouble() / max.toDouble()
        val current = if (fraction.isNaN() || fraction.isInfinite()) {
            0
        } else {
            (fraction * duration.value).roundToLong().coerceIn(0, duration.value)
        }
        return current
    }

    private fun refreshCursor() {
        if (!firstLayoutDone) {
            return
        }
        val max = progressLine.width - progressCursor.width
        val fraction = currentPosition.value.toDouble() / duration.value.toDouble()
        val current = if (fraction.isNaN() || fraction.isInfinite()) {
            0
        } else {
            (max * fraction).roundToInt().coerceIn(0, max)
        }
        progressCursor.updateLayoutParams<MarginLayoutParams> {
            marginStart = current
        }
    }

    private companion object {

        private fun Long.convertText(): String {
            val ms = coerceAtLeast(0)
            val unit = TimeUnit.MILLISECONDS
            val hour = unit.toHours(ms)
            val minute = unit.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hour)
            val second =
                unit.toSeconds(ms) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(
                    minute
                )
            return listOf(hour, minute, second).joinToString(":") {
                it.convertString()
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
}
