package com.simplicityapp.modules.categories.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.simplicityapp.modules.categories.fragment.CategoriesSelectorFragment
import com.simplicityapp.modules.categories.fragment.CategoryFragment
import com.simplicityapp.R
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.config.Constant.TAG_CATEGORY
import com.simplicityapp.databinding.CategoriesSelectorActivityBinding

class CategoriesSelectorActivity : BaseActivity()  {

    private lateinit var binding: CategoriesSelectorActivityBinding
    private val bundle = Bundle()
    var fragmentManager: FragmentManager? = null
    var fragment = Fragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CategoriesSelectorActivityBinding.inflate(layoutInflater)
        initActivity(binding)
        if (savedInstanceState == null) {
            fragmentManager = supportFragmentManager
            fragment = CategoriesSelectorFragment.newInstance()
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                ?.commitNow()
        }
        instance = this
    }

    override fun initUI() {
        binding.toolbar.toolbar.apply {
            setSupportActionBar(this)
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
            title = resources.getString(R.string.categories_selector_title)
        }

    }

    fun openCategory(categoryName: String, categoryId: Int) {
        fragment = CategoryFragment()
        bundle.putInt(TAG_CATEGORY, categoryId)
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.commit()
        binding.toolbar.toolbar.title = categoryName
    }

    override fun onBackPressed() {
        if (fragment is CategoryFragment) {
            bundle.putInt(TAG_CATEGORY, 0)
            fragment = CategoriesSelectorFragment.newInstance()
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                ?.commitNow()
            binding.toolbar.toolbar.title = resources.getString(R.string.categories_selector_title)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private lateinit var instance: CategoriesSelectorActivity

        val CategoriesSelectorInstance: CategoriesSelectorActivity
            get() {
                return instance
            }
    }
}
