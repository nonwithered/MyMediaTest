package com.example.shared.utils

import kotlinx.coroutines.Job

fun Job.dispose() {
    cancel()
}
