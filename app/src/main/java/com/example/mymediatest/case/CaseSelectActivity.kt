package com.example.mymediatest.case

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymediatest.R
import com.example.shared.page.BaseActivity

class CaseSelectActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.case_select_activity

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        initList()
    }

    private fun initList() {
        val listView: RecyclerView = findViewById(R.id.list_view)
        listView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        listView.adapter = CaseViewAdapter()
    }
}
