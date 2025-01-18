package com.example.mymediatest.player

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerVM : ViewModel() {

    val state = MutableStateFlow(PlayerState.IDLE)
}
