package com.example.shared.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    protected open val layoutId: Int
        @LayoutRes
        get() = 0

    protected open fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): View = inflater.inflate(layoutId, container, false)

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return createView(inflater, container)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
    }
}
