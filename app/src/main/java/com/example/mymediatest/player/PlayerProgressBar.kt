package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.example.mymediatest.R
import com.example.shared.utils.coroutineScope
import com.example.shared.utils.findView
import com.example.shared.utils.getValue
import com.example.shared.utils.inflate
import com.example.shared.utils.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlayerProgressBar(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    init {
        inflate(R.layout.player_progress_bar, true)
    }

    private val timestampCurrentState = MutableStateFlow(0L)
    private val timestampTotalState = MutableStateFlow(0L)

    var timestampCurrent: Long by timestampCurrentState
    var timestampTotal: Long by timestampTotalState

    private val timestampCurrentTextView: TextView = findView(R.id.timestamp_current)!!
    private val timestampTotalTextView: TextView = findView(R.id.timestamp_total)!!

    init {
        coroutineScope.launch {
            timestampCurrentState.collect {
                timestampCurrentTextView.text = it.convertText()
            }
        }
        coroutineScope.launch {
            timestampTotalState.collect {
                timestampTotalTextView.text = it.convertText()
            }
        }
    }

    private fun Long.convertText(): String {
        val duration = coerceAtLeast(0)
        val unit = TimeUnit.MILLISECONDS
        val second = unit.toSeconds(duration)
        val minute = unit.toMinutes(duration)
        val hour = unit.toHours(duration)
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
