package com.example.mymediatest.case

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.mymediatest.R
import com.example.shared.page.BaseActivity
import com.example.shared.utils.elseTrue
import com.example.shared.utils.logI

class CasePageActivity : BaseActivity() {

    companion object {

        private const val TAG = "CasePageActivity"

        private const val TAG_PAGE_FRAGMENT = "TAG_SELECT_PAGE_FRAGMENT"

        fun start(context: Context, pageData: CasePageData) {
            val intent = Intent(context, CasePageActivity::class.java).putExtras(pageData.asBundle())
            context.startActivity(intent)
        }
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
        if (!pageData.showActionBar.elseTrue) {
            supportActionBar?.hide()
        }
    }
}
