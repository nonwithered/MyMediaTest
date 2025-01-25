package com.example.mymediatest.select

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import com.example.mymediatest.R
import com.example.shared.bean.BundleProperties
import com.example.shared.page.BaseActivity
import com.example.shared.utils.TAG
import com.example.shared.utils.elseFalse
import com.example.shared.utils.logI

class CasePageActivity : BaseActivity() {

    class CasePageData(
        bundle: Bundle? = null,
    ): BundleProperties(bundle ?: Bundle()) {

        private var fragmentClassName: String? by "fragmentClassName".property()

        var fragmentClass: Class<out Fragment>?
            get() {
                val fragmentClassName = fragmentClassName ?: return null
                val clazz = runCatching {
                    Class.forName(fragmentClassName)
                }.getOrNull()
                @Suppress("UNCHECKED_CAST")
                return clazz as? Class<out Fragment>
            }
            set(value) {
                fragmentClassName = value?.name
            }

        var pageName: String? by "pageName".property()

        var hideActionBar: Boolean? by "hideActionBar".property()

        var hideSystemUI: Boolean? by "hideSystemUI".property()

        var extras: Bundle? by "extras".property()
    }

    private val pageData by lazy {
        CasePageData(intent.extras).also { pageData ->
            TAG.logI { "update pageData ${pageData.pageName}" }
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

    private fun initFragment() {
        ensureFragmentEmpty()
        val fragmentClass = pageData.fragmentClass
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
        if (pageData.hideActionBar.elseFalse) {
            supportActionBar?.hide()
            TAG.logI { "hideActionBar" }
        }
    }

    private fun initSystemUI() {
        if (pageData.hideSystemUI.elseFalse) {
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
