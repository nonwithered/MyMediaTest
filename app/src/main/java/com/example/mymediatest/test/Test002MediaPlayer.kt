package com.example.mymediatest.test

import android.os.Bundle
import androidx.annotation.LayoutRes
import com.example.mymediatest.R
import com.example.mymediatest.play.MediaPlayerHelper
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.mymediatest.test.base.CommonPlayerFragment

class Test002MediaPlayer : CommonPlayerFragment<Test002MediaPlayer.Params, BasePlayerHelper.Holder>() {

    enum class ViewType(
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

        var viewType: ViewType by ViewType::class.adapt()
    }

    internal class ParamsBuilder : BaseParamsBuilder<Params>(Params()) {

        init {
            caseParams::viewType.adapt()
        }
    }
    
    override val playerLayoutId: Int
        get() = params.viewType.playerLayoutId
    
    override val player: CommonPlayerHelper by lazy {
        MediaPlayerHelper(requireContext())
    }
}
