package com.example.shared.view.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLExt
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.TextureView
import com.example.shared.utils.CommonCleaner
import com.example.shared.utils.getValue
import com.example.shared.utils.registerWeak
import com.example.shared.utils.runCatchingTyped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

class GLTextureView(
    context: Context,
    attributeSet: AttributeSet,
) : TextureView(
    context,
    attributeSet,
), TextureView. SurfaceTextureListener {

    interface Renderer : GLSurfaceView.Renderer {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

        override fun onDrawFrame(gl: GL10?)
    }

    enum class RenderMode {
        RENDERMODE_WHEN_DIRTY,
        RENDERMODE_CONTINUOUSLY,
    }

    var renderer: Renderer? = null
        set(value) {
            checkRenderThreadState()
            eglConfigChooser = eglConfigChooser ?: SimpleEGLConfigChooser(true, eglContextClientVersion)
            eglContextFactory = eglContextFactory ?: DefaultContextFactory(eglContextClientVersion)
            eglWindowSurfaceFactory = eglWindowSurfaceFactory ?: DefaultWindowSurfaceFactory
            field = value!!
            startGLThread()
        }

    private var glThread: GLThread? = null

    private fun checkRenderThreadState() {
        glThread ?: return
        throw IllegalStateException("checkRenderThreadState")
    }

    @Volatile
    var preserveEGLContextOnPause: Boolean = false

    var eglContextClientVersion: Int = 0
        set(value) {
            checkRenderThreadState()
            field = value
        }

    var eglConfigChooser: EGLConfigChooser? = null
        set(value) {
            checkRenderThreadState()
            field = value!!
        }

    var eglContextFactory: EGLContextFactory? = null
        set(value) {
            checkRenderThreadState()
            field = value!!
        }

    var eglWindowSurfaceFactory: EGLWindowSurfaceFactory? = null
        set(value) {
            checkRenderThreadState()
            field = value!!
        }

    var glWrapper: GLWrapper? = null
        set(value) {
            checkRenderThreadState()
            field = value!!
        }

    var renderMode: RenderMode = RenderMode.RENDERMODE_CONTINUOUSLY
        set(value) {
            field = value
            glThread?.renderMode = value
        }

    private val glCoroutineScope: CoroutineScope

    init {
        val glCoroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        CommonCleaner.register(this) {
            glCoroutineContext.close()
        }
        glCoroutineScope = CoroutineScope(glCoroutineContext)
        surfaceTextureListener = this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        renderer ?: return
        startGLThread()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        glThread?.requestExitAndWait()
    }

    private fun startGLThread() {
        glThread = GLThread(
            glTextureView = this,
            renderMode = renderMode,
        ).also {
            it.start()
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        TODO("Not yet implemented")
    }

    private class GLThread(

        glTextureView: GLTextureView,

        @Volatile
        var renderMode: RenderMode,
    ) {

        @Volatile
        private var shouldExit = false
        private val exited = MutableStateFlow(false)

        @Suppress("UNUSED")
        private val cleanable = CommonCleaner.registerWeak(glTextureView) {
            requestExitAndWait()
        }

        private val view by WeakReference(glTextureView)

        fun start() {
            view?.glCoroutineScope?.launch { run() }
        }

        private fun run() {
        }

        fun requestExitAndWait() {
            shouldExit = true
            runBlocking {
                exited.first { it }
            }
        }
    }

    interface EGLConfigChooser : GLSurfaceView.EGLConfigChooser {

        override fun chooseConfig(egl: EGL10, display: EGLDisplay?): EGLConfig
    }

    /**
     * @see android.opengl.GLSurfaceView.BaseConfigChooser
     */
    abstract class BaseConfigChooser(
        configSpec: IntArray,
        eglContextClientVersion: Int,
    ) : EGLConfigChooser {

        private val filterConfigSpec = filterConfigSpec(configSpec, eglContextClientVersion)

        protected abstract fun chooseConfig(
            egl: EGL10,
            display: EGLDisplay?,
            configs: Array<EGLConfig>,
        ): EGLConfig

        final override fun chooseConfig(egl: EGL10, display: EGLDisplay?): EGLConfig {
            val numConfig = intArrayOf(0)
            if (!egl.eglChooseConfig(
                display,
                filterConfigSpec,
                null,
                0,
                numConfig,
            )) {
                throwEglException("eglChooseConfig")
            }
            val numConfigs = numConfig.first()
            if (numConfigs <= 0) {
                throwEglException("No configs match configSpec")
            }
            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            if (!egl.eglChooseConfig(
                display,
                filterConfigSpec,
                configs,
                numConfigs,
                numConfig,
            )) {
                throwEglException("eglChooseConfig(numConfigs=$numConfigs)")
            }
            return chooseConfig(egl, display, configs.filterNotNull().toTypedArray())
        }

        private fun filterConfigSpec(
            configSpec: IntArray,
            eglContextClientVersion: Int,
        ): IntArray {
            when (eglContextClientVersion) {
                2, 3 -> {}
                else -> return configSpec
            }
            val newConfigSpec = configSpec.toMutableList()
            newConfigSpec.removeLastOrNull()
            newConfigSpec += EGL10.EGL_RENDERABLE_TYPE
            newConfigSpec += if (eglContextClientVersion == 2) {
                EGL14.EGL_OPENGL_ES2_BIT
            } else {
                EGLExt.EGL_OPENGL_ES3_BIT_KHR
            }
            newConfigSpec += EGL10.EGL_NONE
            return newConfigSpec.toIntArray()
        }
    }

    /**
     * @see android.opengl.GLSurfaceView.ComponentSizeChooser
     */
    open class ComponentSizeChooser(
        private val redSize: Int,
        private val greenSize: Int,
        private val blueSize: Int,
        private val alphaSize: Int,
        private val depthSize: Int,
        private val stencilSize: Int,
        eglContextClientVersion: Int,
    ) : BaseConfigChooser(
        intArrayOf(
            EGL10.EGL_RED_SIZE, redSize,
            EGL10.EGL_GREEN_SIZE, greenSize,
            EGL10.EGL_BLUE_SIZE, blueSize,
            EGL10.EGL_ALPHA_SIZE, alphaSize,
            EGL10.EGL_DEPTH_SIZE, depthSize,
            EGL10.EGL_STENCIL_SIZE, stencilSize,
            EGL10.EGL_NONE,
        ),
        eglContextClientVersion,
    ) {

        override fun chooseConfig(
            egl: EGL10,
            display: EGLDisplay?,
            configs: Array<EGLConfig>,
        ): EGLConfig {
            fun Int.findConfigAttrib(config: EGLConfig) = findConfigAttrib(
                egl,
                display,
                config,
                this,
                0,
            )
            return configs.first {
                val d = EGL10.EGL_DEPTH_SIZE.findConfigAttrib(it)
                val s = EGL10.EGL_STENCIL_SIZE.findConfigAttrib(it)
                if (d >= depthSize && s >= stencilSize) {
                    val r = EGL10.EGL_RED_SIZE.findConfigAttrib(it)
                    val g = EGL10.EGL_GREEN_SIZE.findConfigAttrib(it)
                    val b = EGL10.EGL_BLUE_SIZE.findConfigAttrib(it)
                    val a = EGL10.EGL_ALPHA_SIZE.findConfigAttrib(it)
                    r == redSize && g == greenSize && b == blueSize && a == alphaSize
                } else {
                    false
                }
            }
        }

        private fun findConfigAttrib(
            egl: EGL10,
            display: EGLDisplay?,
            config: EGLConfig?,
            attribute: Int,
            defaultValue: Int,
        ): Int {
            val values = intArrayOf(0)
            if (egl.eglGetConfigAttrib(display, config, attribute, values)) {
                return values.first()
            }
            return defaultValue
        }
    }

    /**
     * @see android.opengl.GLSurfaceView.SimpleEGLConfigChooser
     */
    class SimpleEGLConfigChooser(
        withDepthBuffer: Boolean,
        eglContextClientVersion: Int,
    ) : ComponentSizeChooser(
        redSize = 8,
        greenSize = 8,
        blueSize = 8,
        alphaSize = 0,
        depthSize = if (withDepthBuffer) 16 else 0,
        stencilSize = 0,
        eglContextClientVersion = eglContextClientVersion,
    )

    interface EGLContextFactory : GLSurfaceView.EGLContextFactory {

        override fun createContext(
            egl: EGL10,
            display: EGLDisplay?,
            eglConfig: EGLConfig?,
        ): EGLContext?

        override fun destroyContext(
            egl: EGL10,
            display: EGLDisplay?,
            context: EGLContext?,
        )
    }

    /**
     * @see android.opengl.GLSurfaceView.DefaultContextFactory
     */
    private class DefaultContextFactory(
        private val eglContextClientVersion: Int,
    ) : EGLContextFactory {

        private companion object {

            private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        }

        override fun createContext(
            egl: EGL10,
            display: EGLDisplay?,
            eglConfig: EGLConfig?
        ): EGLContext? {
            return egl.eglCreateContext(
                display,
                eglConfig,
                EGL10.EGL_NO_CONTEXT,
                eglContextClientVersion.takeIf {
                    it != 0
                }?.let {
                    intArrayOf(
                        EGL_CONTEXT_CLIENT_VERSION,
                        it,
                        EGL10.EGL_NONE,
                    )
                },
            )
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay?, context: EGLContext?) {
            if (!egl.eglDestroyContext(display, context)) {
                egl.throwEglException("eglDestroyContext")
            }
        }
    }

    interface EGLWindowSurfaceFactory : GLSurfaceView.EGLWindowSurfaceFactory {

        override fun createWindowSurface(
            egl: EGL10,
            display: EGLDisplay?,
            config: EGLConfig?,
            nativeWindow: Any?,
        ): EGLSurface?

        override fun destroySurface(
            egl: EGL10,
            display: EGLDisplay?,
            surface: EGLSurface?,
        )
    }

    /**
     * @see android.opengl.GLSurfaceView.DefaultWindowSurfaceFactory
     */
    private object DefaultWindowSurfaceFactory : EGLWindowSurfaceFactory {

        override fun createWindowSurface(
            egl: EGL10,
            display: EGLDisplay?,
            config: EGLConfig?,
            nativeWindow: Any?,
        ): EGLSurface? {
            return runCatchingTyped<IllegalArgumentException, EGLSurface> {
                egl.eglCreateWindowSurface(
                    display,
                    config,
                    nativeWindow,
                    null,
                )
            }.getOrThrow()
        }

        override fun destroySurface(
            egl: EGL10,
            display: EGLDisplay?,
            surface: EGLSurface?,
        ) {
            egl.eglDestroySurface(display, surface)
        }

    }

    interface GLWrapper : GLSurfaceView.GLWrapper {

        override fun wrap(gl: GL?): GL?
    }
}
