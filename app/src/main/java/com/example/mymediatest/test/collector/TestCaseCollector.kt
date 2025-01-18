package com.example.mymediatest.test.collector

import androidx.annotation.Keep
import com.example.mymediatest.case.CaseCollector
import com.example.mymediatest.test.*

@Keep
class TestCaseCollector : CaseCollector {

    override val items = listOf(
        Test001VideoView,
    )
}
