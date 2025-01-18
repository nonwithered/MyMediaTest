package com.example.mymediatest.case

import com.example.shared.list.BaseViewAdapter
import com.example.shared.utils.loadService

class CaseViewAdapter : BaseViewAdapter<CaseItem, CaseViewHolder>() {

    override val items = loadService<CaseItem>()

    override val factory = listOf(
        CaseViewFactory,
    )
}
