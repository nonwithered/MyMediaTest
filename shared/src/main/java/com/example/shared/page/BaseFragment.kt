package com.example.shared.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.shared.utils.CloseableGroup
import com.example.shared.utils.asCloseable

abstract class BaseFragment : Fragment() {

    @get:LayoutRes
    protected open val layoutId: Int
        get() = 0

    private val closeableGroup = CloseableGroup()

    protected fun defer(block: () -> Unit) {
        closeableGroup += block.asCloseable
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeableGroup.close()
    }
}
