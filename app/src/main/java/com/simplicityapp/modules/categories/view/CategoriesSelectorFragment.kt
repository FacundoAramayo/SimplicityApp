package com.simplicityapp.modules.categories.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.simplicityapp.R
import com.simplicityapp.base.utils.UITools
import com.simplicityapp.base.widget.SpacingItemDecoration
import com.simplicityapp.databinding.CategoriesSelectorFragmentBinding
import com.simplicityapp.modules.categories.adapter.AdapterCategoriesSelectorGrid
import com.simplicityapp.modules.categories.viewModel.CategoriesSelectorViewModel

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
            addItemDecoration(SpacingItemDecoration(UITools.getGridSpanCount(activity!!), UITools.dpToPx(4), true))

            val items = viewModel.getCategories()
            val adapter = AdapterCategoriesSelectorGrid(items) {
                item -> CategoriesSelectorActivity.CategoriesSelectorInstance.openCategory(item.categoryId)
            }
            this.adapter = adapter
            adapter.notifyDataSetChanged()

        }

    }

}
