package com.example.mymediatest.test

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import androidx.annotation.LayoutRes
import com.example.mymediatest.R
import com.example.mymediatest.play.CodecPlayerHelperController
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.mymediatest.play.codec.MediaSupport
import com.example.mymediatest.play.support.AVSupport
import com.example.mymediatest.test.base.CommonPlayerFragment

class Test004CodecPlayer: CommonPlayerFragment<Test004CodecPlayer.Params, BasePlayerHelper.Holder>() {

    enum class ViewType(
        @LayoutRes
        val playerLayoutId: Int,
    ) {

        SURFACE(
            R.layout.common_player_gl_surface_view,
        ),

        TEXTURE(
            R.layout.common_player_gl_texture_view,
        ),
    }

    enum class CodecType(
        val support: AVSupport<*>,
    ) {

        MEDIA(
            MediaSupport,
        ),
    }

    class Params(
        bundle: Bundle = Bundle(),
    ) : BaseParams(bundle) {

        var viewType: ViewType by ViewType::class.adapt()
        var codecType: CodecType by CodecType::class.adapt()
    }

    internal class ParamsBuilder : BaseParamsBuilder<Params>(Params()) {

        init {
            caseParams::viewType.adapt()
            caseParams::codecType.adapt()
        }
    }

    override val playerLayoutId: Int
        get() = params.viewType.playerLayoutId

    override val factory = object : CommonPlayerHelper.Factory {

        override val textureCacheSize: Int
            get() = when (params.codecType) {
                CodecType.MEDIA -> CodecPlayerHelperController.MAX_BUFFER_SIZE + 1
            }

        override fun createController(
            controllerParams: CommonPlayerHelper.Params,
        ): CommonPlayerHelper.Controller {
            return CodecPlayerHelperController(controllerParams, params.codecType.support)
        }
    }
}
