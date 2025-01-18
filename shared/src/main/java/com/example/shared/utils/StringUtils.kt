package com.example.shared.utils

operator fun String.times(count: Int): String {
    val builder = StringBuilder(length * count)
    repeat(count) {
        builder += this
    }
    return builder.toString()
}

operator fun <T : Appendable> T.plusAssign(value: Char) {
    append(value)
}

operator fun <T : Appendable> T.plusAssign(value: CharSequence?) {
    append(value)
}


