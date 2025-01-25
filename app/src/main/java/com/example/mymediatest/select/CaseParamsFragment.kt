package com.example.mymediatest.select

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymediatest.R
import com.example.mymediatest.select.CasePageActivity.PageModel
import com.example.shared.bean.BundleProperties
import com.example.shared.list.BaseViewAdapter
import com.example.shared.list.BaseViewHolder
import com.example.shared.page.BaseFragment
import com.example.shared.utils.cross
import com.example.shared.utils.findView
import com.example.shared.utils.getValue
import com.example.shared.utils.viewModel

open class CaseParamsFragment<T : BundleProperties>(
    protected val caseParams: T,
) : BaseFragment() {

    internal data class Options<V : Any>(
        val list: List<V>,
        private val block: (V) -> Unit,
    ) {

        fun select(index: Int) {
            block(list[index])
        }
    }

    private val pageData by {
        requireActivity().viewModel<PageModel>().pageData!!
    }

    private val paramList = mutableListOf<Options<out Any>>()

    protected fun <V : Any> option(vararg args: V, block: (V) -> Unit) {
        paramList += Options(args.toList(), block)
    }

    override val layoutId: Int
        get() = R.layout.case_params_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView: RecyclerView = view.findView(R.id.list_view)!!
        listView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        listView.adapter = BaseViewAdapter.simple(
            Options::class to ItemViewHolder::class cross R.layout.param_item__view,
        ) {
            paramList
        }
        val commit: View = view.findView(R.id.commit_text)!!
        commit.setOnClickListener {
            requireActivity().run {
                pageData.paramsExtras = caseParams.asBundle()
                val intent = Intent(context, CasePageActivity::class.java).putExtras(pageData.asBundle())
                startActivity(intent)
                finish()
            }
        }
    }

    internal class ItemViewHolder(itemView: View) : BaseViewHolder<Options<*>>(itemView), AdapterView.OnItemSelectedListener {

        private val spinner: AppCompatSpinner = itemView.findView(R.id.item_spinner)!!

        private var param: Options<*>? = null

        init {
            spinner.onItemSelectedListener = this
        }

        override fun onBind(item: Options<*>, position: Int) {
            super.onBind(item, position)
            param = item
            defer {
                param = null
            }
            spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, item.list)
            defer {
                spinner.adapter = null
            }
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            param?.select(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}
