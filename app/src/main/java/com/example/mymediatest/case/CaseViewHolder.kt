package com.example.mymediatest.case

import android.view.View
import android.widget.TextView
import com.example.mymediatest.R
import com.example.shared.list.BaseViewHolder

class CaseViewHolder(itemView: View) : BaseViewHolder<CaseItem>(itemView) {

    private val itemName: TextView = itemView.findViewById(R.id.item_name)

    override fun onBind(item: CaseItem, position: Int) {
        super.onBind(item, position)
        itemName.text = item.pageName
        itemView.setOnClickListener {
            val pageData = CasePageData()
            pageData.pageName = item.pageName
            pageData.fragmentClass = item.fragmentClass.java
            pageData.showActionBar = item.showActionBar
            pageData.extras = item.extras
            CasePageActivity.start(context, pageData)
        }
    }

    override fun onUnBind() {
        super.onUnBind()
    }
}
