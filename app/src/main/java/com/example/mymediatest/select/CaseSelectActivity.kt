package com.example.mymediatest.select

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymediatest.R
import com.example.shared.list.BaseViewAdapter
import com.example.shared.list.BaseViewHolder
import com.example.shared.page.BaseActivity
import com.example.shared.utils.cross
import com.example.shared.utils.findView
import kotlin.reflect.KClass

class CaseSelectActivity : BaseActivity() {

    interface CaseItem {

        val fragmentClass: KClass<out Fragment>

        val pageName: String
            get() = fragmentClass.java.simpleName

        val hideActionBar: Boolean
            get() = true

        val hideSystemUI: Boolean
            get() = true

        val builderClass: KClass<out Fragment>?
            get() = null

        val extras: Bundle?
            get() = null

        val paramsExtras: Bundle?
            get() = null
    }

    override val layoutId: Int
        get() = R.layout.case_select_activity

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        val listView: RecyclerView = findView(R.id.list_view)!!
        listView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        listView.adapter = BaseViewAdapter.simple(
            CaseItem::class to ItemViewHolder::class cross R.layout.case_item_view,
        ) {
            CaseCollector.items
        }
    }

    internal class ItemViewHolder(itemView: View) : BaseViewHolder<CaseItem>(itemView) {

        private val itemName: TextView = itemView.findView(R.id.item_name)!!

        override fun onBind(item: CaseItem, position: Int) {
            super.onBind(item, position)
            itemName.text = item.pageName
            itemView.setOnClickListener {
                val pageData = CasePageActivity.CasePageData()
                pageData.pageName = item.pageName
                pageData.fragmentClass = item.fragmentClass.java
                pageData.hideActionBar = item.hideActionBar
                pageData.hideSystemUI = item.hideSystemUI
                pageData.extras = item.extras
                pageData.builderClass = item.builderClass?.java
                pageData.paramsExtras = item.paramsExtras
                CasePageActivity.start(context, pageData)
            }
        }
    }
}
