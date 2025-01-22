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

typealias Tuple2<T1, T2> = Pair<T1, T2>

fun <
        T1,
        T2,
        > tupleOf(
    v1: T1,
    v2: T2,
) = Tuple2(
    v1,
    v2,
)

typealias Tuple3<T1, T2, T3> = Triple<T1, T2, T3>

fun <
        T1,
        T2,
        T3,
        > tupleOf(
    v1: T1,
    v2: T2,
    v3: T3,
) = Tuple3(
    v1,
    v2,
    v3,
)

typealias Tuple4<T1, T2, T3, T4> = Quad<T1, T2, T3, T4>

fun <
        T1,
        T2,
        T3,
        T4,
        > tupleOf(
    v1: T1,
    v2: T2,
    v3: T3,
    v4: T4,
) = Tuple4(
    v1,
    v2,
    v3,
    v4,
)

data class Tuple5<
        T1,
        T2,
        T3,
        T4,
        T5,
        >(
    val v1: T1,
    val v2: T2,
    val v3: T3,
    val v4: T4,
    val v5: T5,
)

fun <
        T1,
        T2,
        T3,
        T4,
        T5,
        > tupleOf(
    v1: T1,
    v2: T2,
    v3: T3,
    v4: T4,
    v5: T5,
) = Tuple5(
    v1,
    v2,
    v3,
    v4,
    v5,
)
