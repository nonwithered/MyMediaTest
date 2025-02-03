package com.example.shared.utils

import android.util.Log

enum class LogLevel(
    val priorityLevel: Int,
    val priorityName: String,
    val simpleName: String,
) {
    VERBOSE(
        priorityLevel = Log.VERBOSE,
        priorityName = "VERBOSE",
        simpleName = "V",
    ),
    DEBUG(
        priorityLevel = Log.DEBUG,
        priorityName = "DEBUG",
        simpleName = "D",
    ),
    INFO(
        priorityLevel = Log.INFO,
        priorityName = "INFO",
        simpleName = "I",
    ),
    WARN(
        priorityLevel = Log.WARN,
        priorityName = "WARN",
        simpleName = "W",
    ),
    ERROR(
        priorityLevel = Log.ERROR,
        priorityName = "ERROR",
        simpleName = "E",
    ),
    ASSERT(
        priorityLevel = Log.ASSERT,
        priorityName = "ASSERT",
        simpleName = "A",
    ),
}

fun log(level: LogLevel, tag: String, msg: String, e: Throwable?) {
    Log.println(level.priorityLevel, tag, "$msg\n${e?.stackTraceToString() ?: ""}")
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

val Any.TAG: String
    get() = javaClass.simpleName

val allStackTracesString: List<String>
    get() = Thread.getAllStackTraces().map { (thread, stackTraces) ->
        buildString {
            val builder = this
            builder += "Thread: "
            builder += thread.name
            builder += ", TID: "
            builder += thread.id.toString()
            stackTraces.forEach { stackTrace ->
                builder += "\nat "
                builder += stackTrace.className
                builder += "."
                builder += stackTrace.methodName
                builder += "("
                builder += stackTrace.fileName
                builder += ":"
                builder += stackTrace.lineNumber.toString()
                builder += ")"
            }
        }
    }
