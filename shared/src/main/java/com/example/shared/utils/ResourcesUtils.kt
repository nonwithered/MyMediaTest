package com.example.shared.utils

import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat
import kotlin.math.roundToInt

val @receiver:StringRes Int.resString: String
    get() = ContextCompat.getString(app, this)

val @receiver:DrawableRes Int.resDrawable: Drawable
    get() = ContextCompat.getDrawable(app, this)!!

val @receiver:ColorRes Int.resColor: Int
    get() = ContextCompat.getColor(app, this)

private fun Float.applyDimension(@TypedValueCompat.ComplexDimensionUnit unit: Int): Float {
    return TypedValue.applyDimension(
        unit,
        this,
        app.resources.displayMetrics,
    )
}

val Float.dp: Float
    get() = applyDimension(TypedValue.COMPLEX_UNIT_DIP)

val Int.dp: Int
    get() = toFloat().dp.roundToInt()

val Float.sp: Float
    get() = applyDimension(TypedValue.COMPLEX_UNIT_SP)

val Int.sp: Int
    get() = toFloat().sp.roundToInt()
