package com.example.shared.view.gl

import android.opengl.GLES20
import android.opengl.GLU
import com.example.shared.utils.toStringHex
import javax.microedition.khronos.egl.EGL10

class EglException : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class GlException : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

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

/**
 * @see androidx.media3.common.util.GlUtil.checkGlError
 */
fun checkGlError() {
    var e: GlException? = null
    while (true) {
        val error = GLES20.glGetError()
        if (error == GLES20.GL_NO_ERROR) {
            break
        }
        val errorString = GLU.gluErrorString(error)
        val message = "glError $error $errorString"
        e = if (e === null) {
            GlException(message)
        } else {
            GlException(message, e)
        }
    }
    if (e !== null) {
        throw e
    }
}