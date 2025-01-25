package com.example.mymediatest.select

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.mymediatest.R
import com.example.shared.bean.BundleProperties
import com.example.shared.page.BaseActivity
import com.example.shared.utils.LateInitProxy
import com.example.shared.utils.TAG
import com.example.shared.utils.elseFalse
import com.example.shared.utils.logI
import com.example.shared.utils.parseClass
import com.example.shared.utils.viewModel

class CasePageActivity : BaseActivity(), LateInitProxy.Owner {

    class CasePageData(
        bundle: Bundle? = null,
    ): BundleProperties(bundle ?: Bundle()) {

        private var fragmentClassName: String? by "fragmentClassName".property()

        var fragmentClass: Class<out Fragment>?
            get() = fragmentClassName?.parseClass()
            set(value) {
                fragmentClassName = value?.name
            }

        var pageName: String? by "pageName".property()

        var hideActionBar: Boolean? by "hideActionBar".property()

        var hideSystemUI: Boolean? by "hideSystemUI".property()

        private var builderClassName: String? by "paramsClassName".property()

        var builderClass: Class<out Fragment>?
            get() = builderClassName?.parseClass()
            set(value) {
                builderClassName = value?.name
            }

        var extras: Bundle? by "extras".property()

        var paramsExtras: Bundle? by "paramsExtras".property()
    }

    class PageModel : ViewModel() {

        var pageData: CasePageData? = null
    }

    private var pageData: CasePageData by LateInitProxy()

    override fun onPropertyInit(proxy: LateInitProxy<*>) {
        when {
            pageData === proxy.get() -> viewModel<PageModel>().pageData = pageData
        }
    }

    override val layoutId: Int
        get() = R.layout.case_page_activity

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        initFragment()
        initActionBar()
        initSystemUI()
    }

    private val needParams: Boolean
        get() = pageData.builderClass !== null && pageData.paramsExtras === null

    private fun initFragment() {
        ensureFragmentEmpty()
        pageData = CasePageData(intent.extras)
        val fragmentClass = if (needParams) {
            pageData.builderClass!!
        } else {
            pageData.fragmentClass
        }
        if (fragmentClass === null) {
            finish()
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmentClass, null, TAG_PAGE_FRAGMENT)
            .commit()
    }

    private fun ensureFragmentEmpty() {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_PAGE_FRAGMENT) ?: return
        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commit()
    }

    private fun initActionBar() {
        supportActionBar?.title = pageData.pageName
        if (!needParams && pageData.hideActionBar.elseFalse) {
            supportActionBar?.hide()
            TAG.logI { "hideActionBar" }
        }
    }

    private fun initSystemUI() {
        if (!needParams && pageData.hideSystemUI.elseFalse) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
            TAG.logI { "hideSystemUI" }
        }
    }

    companion object {

        private const val TAG_PAGE_FRAGMENT = "TAG_SELECT_PAGE_FRAGMENT"

        fun start(context: Context, pageData: CasePageData) {
            val intent = Intent(context, CasePageActivity::class.java).putExtras(pageData.asBundle())
            context.startActivity(intent)
        }
    }
}
