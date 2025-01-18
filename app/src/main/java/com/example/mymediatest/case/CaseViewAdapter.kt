package com.example.mymediatest.case

import com.example.shared.list.BaseViewAdapter

class CaseViewAdapter(
    collector: CaseCollector,
) : BaseViewAdapter<CaseItem, CaseViewHolder>() {

    override val items by collector::items

    override val factory = listOf(
        CaseViewFactory,
    )
}
