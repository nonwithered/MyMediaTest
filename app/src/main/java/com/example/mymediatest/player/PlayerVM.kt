package com.example.mymediatest.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerVM : ViewModel() {

    val state = MutableStateFlow(PlayerState.IDLE)

    val contentUri = MutableStateFlow(null as Uri?)

    val duration = MutableStateFlow(0L)

    val currentPosition = MutableStateFlow(0L)

    val isSeekDraging = MutableStateFlow(false)
}
