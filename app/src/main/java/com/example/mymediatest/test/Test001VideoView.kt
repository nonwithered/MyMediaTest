package com.example.mymediatest.test

import com.example.mymediatest.case.CaseItem
import com.example.shared.page.BaseFragment
import com.google.auto.service.AutoService

@AutoService(CaseItem::class)
class Test001VideoView : CaseItem {

    override val fragmentClass = Page::class

    class Page : BaseFragment() {

    }
}
