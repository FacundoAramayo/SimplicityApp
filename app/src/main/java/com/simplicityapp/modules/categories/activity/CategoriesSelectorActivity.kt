package com.simplicityapp.modules.categories.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.simplicityapp.modules.categories.fragment.CategoriesSelectorFragment
import com.simplicityapp.modules.categories.fragment.CategoryFragment
import com.simplicityapp.R

class CategoriesSelectorActivity : AppCompatActivity() {

    var fragmentManager: FragmentManager? = null
    private val bundle = Bundle()
    var fragment = Fragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.categories_selector_activity)
        if (savedInstanceState == null) {
            fragmentManager = supportFragmentManager
            fragment = CategoriesSelectorFragment.newInstance()
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                ?.commitNow()

        }
        instance = this
    }

    fun openCategory(categoryId: Int) {
        fragment = CategoryFragment()
        bundle.putInt(CategoryFragment.TAG_CATEGORY, categoryId)
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.commitNow()
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        if (fragment is CategoryFragment) {
            fragment = CategoriesSelectorFragment.newInstance()
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                ?.commitNow()
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
}
