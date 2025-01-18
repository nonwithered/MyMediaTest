package com.example.mymediatest.case

import android.view.View
import com.example.mymediatest.R
import com.example.shared.list.BaseViewFactory

object CaseViewFactory : BaseViewFactory<CaseItem, CaseViewHolder> {

    override val layoutId: Int
        get() = R.layout.case_item_view

    override fun createViewHolder(itemView: View): CaseViewHolder {
        return CaseViewHolder(itemView)
    }

    override fun checkItemType(item: CaseItem): Boolean {
        return true
    }
}
