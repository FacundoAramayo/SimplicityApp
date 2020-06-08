package com.simplicityapp.modules.categories.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.simplicityapp.modules.categories.fragment.CategoriesSelectorFragment
import com.simplicityapp.modules.categories.fragment.CategoryFragment
import com.simplicityapp.R
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.databinding.CategoriesSelectorActivityBinding

class CategoriesSelectorActivity : AppCompatActivity(), BaseActivity {

    var fragmentManager: FragmentManager? = null
    private val bundle = Bundle()
    var fragment = Fragment()

    private lateinit var binding: CategoriesSelectorActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CategoriesSelectorActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            fragmentManager = supportFragmentManager
            fragment = CategoriesSelectorFragment.newInstance()
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                ?.commitNow()

        }
        instance = this
        initUI()
    }

    fun openCategory(categoryName: String, categoryId: Int) {
        fragment = CategoryFragment()
        bundle.putInt(CategoryFragment.TAG_CATEGORY, categoryId)
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.commitNow()
        binding.toolbar.toolbar.title = categoryName
    }

    override fun onBackPressed() {
        if (fragment is CategoryFragment) {
            fragment = CategoriesSelectorFragment.newInstance()
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                ?.commitNow()
            binding.toolbar.toolbar.title = resources.getString(R.string.categories_selector_title)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        var TAG_CATEGORY = "key.TAG_CATEGORY"
        private lateinit var instance: CategoriesSelectorActivity

        val CategoriesSelectorInstance: CategoriesSelectorActivity
            get() {
                return instance
            }
    }

    override fun initUI() {
        binding.toolbar.toolbar.apply {
            setSupportActionBar(this)
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
            title = resources.getString(R.string.categories_selector_title)
        }

    }

    override fun initListeners() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getArguments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
