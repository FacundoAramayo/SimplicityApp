package com.simplicityapp.modules.categories.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
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
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.Constant.COMMERCIAL_GUIDE
import com.simplicityapp.base.utils.tryOrDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoriesSelectorFragment : Fragment(R.layout.categories_selector_fragment) {

    private lateinit var viewModel: CategoriesSelectorViewModel

    private lateinit var binding: CategoriesSelectorFragmentBinding

    private var type: String? = null

    private var categoryId: Int? = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CategoriesSelectorFragmentBinding.inflate(inflater, container, false)
        type = activity?.intent?.getStringExtra(Constant.GUIDE_TYPE)
        categoryId = tryOrDefault({activity?.intent?.getStringExtra(CategoryFragment.TAG_CATEGORY)!!.toInt()}, 0)
        //This allow return to guide type on back pressed
        activity?.intent = Intent()
        activity?.intent?.putExtra(Constant.GUIDE_TYPE, type)
        //
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CategoriesSelectorViewModel::class.java)
        // TODO: Use the ViewModel

        initUI()
        initRecycler()
        categoryId?.let {
            if (it != 0) openQuickAccess(it)
        }
    }

    companion object {
        fun newInstance() =
            CategoriesSelectorFragment()
    }

    private fun initUI() {
        binding.categoriesBanner.apply {
            if (type != COMMERCIAL_GUIDE) {
                visibility = GONE
                return
            }
            setOnClickListener {
                CategoriesSelectorActivity.CategoriesSelectorInstance.openCategory(
                    resources.getString(R.string.title_nav_delivery),
                    resources.getIntArray(R.array.id_category)[7]
                )
            }
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

            type?.let {
                GlobalScope.launch(Dispatchers.Main) {
                    val items = viewModel.getCategoriesAsync(it).body()?.categories
                    val adapter = AdapterCategoriesSelectorGrid(items ?: listOf()) { item ->
                        CategoriesSelectorActivity.CategoriesSelectorInstance.openCategory(item.title, item.categoryId)
                    }
                    binding.categoriesRecycler.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }


        }

    }

    private fun openQuickAccess(categoryId: Int) {
        CategoriesSelectorActivity.CategoriesSelectorInstance.openCategory("", categoryId)
    }

}
