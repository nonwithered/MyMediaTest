package com.example.shared.utils

import java.io.Serializable

typealias Vec2<T> = Pair<T, T>

typealias Vec3<T> = Triple<T, T, T>

typealias Vec4<T> = Quad<T, T, T, T>

infix fun <A, B, C> Pair<A, B>.cross(third: C) = Triple(first, second, third)

infix fun <A, B, C, D> Triple<A, B, C>.cross(fourth: D) = Quad(first, second, third, fourth)

data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
) : Serializable {

    override fun toString(): String = "($first, $second, $third, $fourth)"
}

fun <T> Quad<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)
