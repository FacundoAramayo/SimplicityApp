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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoriesSelectorFragment : Fragment(R.layout.categories_selector_fragment) {

    private lateinit var viewModel: CategoriesSelectorViewModel

    private lateinit var binding: CategoriesSelectorFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CategoriesSelectorFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CategoriesSelectorViewModel::class.java)
        // TODO: Use the ViewModel

        initUI()
        initRecycler()
    }

    companion object {
        fun newInstance() =
            CategoriesSelectorFragment()
    }

    private fun initUI() {
        binding.categoriesBanner.setOnClickListener {
            CategoriesSelectorActivity.CategoriesSelectorInstance.
                openCategory(resources.getString(R.string.title_nav_delivery),
                    resources.getIntArray(R.array.id_category)[7]
            )
        }
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

            GlobalScope.launch(Dispatchers.Main) {
                val items = viewModel.getCategoriesAsync().body()?.categories
                val adapter = AdapterCategoriesSelectorGrid(items ?: listOf()) { item ->
                        CategoriesSelectorActivity.CategoriesSelectorInstance.openCategory(item.title, item.categoryId)
                    }
                binding.categoriesRecycler.adapter = adapter
                adapter.notifyDataSetChanged()
            }

        }

    }

}