package com.simplicityapp.modules.categories.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.baseui.decorator.SpacingItemDecoration
import com.simplicityapp.databinding.CategoriesSelectorFragmentBinding
import com.simplicityapp.baseui.adapter.AdapterCategoriesSelectorGrid
import com.simplicityapp.modules.categories.activity.CategoriesSelectorActivity
import com.simplicityapp.modules.categories.viewmodel.CategoriesSelectorViewModel
import com.simplicityapp.R

class CategoriesSelectorFragment : Fragment(R.layout.categories_selector_fragment) {

    private lateinit var viewModel: CategoriesSelectorViewModel

    private lateinit var binding: CategoriesSelectorFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CategoriesSelectorFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CategoriesSelectorViewModel::class.java)
        // TODO: Use the ViewModel


        initRecycler()
    }

    companion object {
        fun newInstance() =
            CategoriesSelectorFragment()
    }

    private fun initRecycler() {
        binding.categoriesRecycler.apply {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(
                SpacingItemDecoration(
                    UITools.getGridSpanCount(activity!!),
                    UITools.dpToPx(4),
                    true
                )
            )

            val items = viewModel.getCategories()
            val adapter =
                AdapterCategoriesSelectorGrid(
                    items
                ) { item ->
                    CategoriesSelectorActivity.CategoriesSelectorInstance.openCategory(item.categoryId)
                }
            this.adapter = adapter
            adapter.notifyDataSetChanged()

        }

    }

}
