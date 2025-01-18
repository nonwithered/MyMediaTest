package com.example.shared.utils

import android.util.Log

enum class LogLevel(
    private val priorityName: String,
    private val simpleName: String,
) {
    VERBOSE(
        priorityName = "VERBOSE",
        simpleName = "V",
    ),
    DEBUG(
        priorityName = "DEBUG",
        simpleName = "D",
    ),
    INFO(
        priorityName = "INFO",
        simpleName = "I",
    ),
    WARN(
        priorityName = "WARN",
        simpleName = "W",
    ),
    ERROR(
        priorityName = "ERROR",
        simpleName = "E",
    ),
    ASSERT(
        priorityName = "ASSERT",
        simpleName = "A",
    ),
}

fun log(level: LogLevel, tag: String, msg: String, e: Throwable?) {
    val priority = when (level) {
        LogLevel.VERBOSE -> Log.VERBOSE
        LogLevel.DEBUG -> Log.DEBUG
        LogLevel.INFO -> Log.INFO
        LogLevel.WARN -> Log.WARN
        LogLevel.ERROR -> Log.ERROR
        LogLevel.ASSERT -> Log.ASSERT
    }
    Log.println(priority, tag, "$msg\n${e?.stackTraceToString() ?: ""}")
}

inline fun String.log(level: LogLevel, e: Throwable? = null, crossinline msg: () -> String) {
    val tag = this
    log(level, tag, msg(), e)
}

inline fun String.logV(e: Throwable? = null, crossinline msg: () -> String) = log(LogLevel.VERBOSE, e, msg)
inline fun String.logD(e: Throwable? = null, crossinline msg: () -> String) = log(LogLevel.DEBUG, e, msg)
inline fun String.logI(e: Throwable? = null, crossinline msg: () -> String) = log(LogLevel.INFO, e, msg)
inline fun String.logW(e: Throwable? = null, crossinline msg: () -> String) = log(LogLevel.WARN, e, msg)
inline fun String.logE(e: Throwable? = null, crossinline msg: () -> String) = log(LogLevel.ERROR, e, msg)
inline fun String.logA(e: Throwable? = null, crossinline msg: () -> String) = log(LogLevel.ASSERT, e, msg)
