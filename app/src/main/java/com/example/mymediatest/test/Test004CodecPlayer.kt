package com.example.mymediatest.test

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.example.mymediatest.R
import com.example.mymediatest.player.BasePlayerHelper
import com.example.mymediatest.test.base.PlayerParamsFragment

class Test004CodecPlayer : PlayerParamsFragment<Test004CodecPlayer.Params, BasePlayerHelper.Holder>() {

    enum class Type(
        @LayoutRes
        val playerLayoutId: Int,
    ) {

        SURFACE(
            R.layout.common_player_surface_view,
        ),

        TEXTURE(
            R.layout.common_player_texture_view,
        ),
    }

    class Params(
        bundle: Bundle = Bundle(),
    ) : BaseParams(bundle) {

        var type: Type by Type::class.adapt()
    }

    internal class ParamsBuilder : BaseParamsBuilder<Params>(Params()) {

        init {
            caseParams::type.adapt()
        }
    }

    override val playerLayoutId: Int
        get() = params.type.playerLayoutId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
