package com.example.shared.view.gl

import com.example.shared.utils.toStringHex
import javax.microedition.khronos.egl.EGL10

/**
 * @see android.opengl.GLSurfaceView.EglHelper.throwEglException
 */
fun EGL10.throwEglException(function: String): Nothing {
    throwEglException(function, eglGetError())
}

/**
 * @see android.opengl.GLSurfaceView.EglHelper.throwEglException
 */
fun throwEglException(function: String, error: Int? = null): Nothing {
    val message = formatEglError(function, error)
    throw EglException(message)
}

/**
 * @see android.opengl.GLSurfaceView.EglHelper.formatEglError
 */
fun formatEglError(function: String, error: Int?): String {
    return if (error === null) {
        "$function failed"
    } else {
        "$function failed: 0x${error.toStringHex}"
    }
}
