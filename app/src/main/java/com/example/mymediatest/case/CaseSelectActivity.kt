package com.example.mymediatest.case

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymediatest.R
import com.example.shared.bean.BundleProperties
import com.example.shared.page.BaseActivity
import com.example.shared.utils.findView
import com.example.shared.utils.info

class CaseSelectActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.case_select_activity

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        initList()
    }

    private fun initList() {
        val listView: RecyclerView = findView(R.id.list_view)!!
        listView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        listView.adapter = CaseViewAdapter(collector)
    }

    private val collector: CaseCollector
        get() {
            val bundle = info().metaData
            return CollectorInfo(bundle).newCollector
        }

    private class CollectorInfo(
        bundle: Bundle,
    ) : BundleProperties(bundle) {

        private val className: String? by "collector_class_name".property()

        private val classType: Class<*>
            get() = Class.forName(className!!)

        val newCollector: CaseCollector
            get() = classType.getConstructor().newInstance() as CaseCollector
    }
}
