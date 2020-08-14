package com.simplicityapp.modules.places.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.baseui.adapter.AdapterPlaceGrid
import com.simplicityapp.baseui.adapter.AdapterSuggestionSearch
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.baseui.decorator.SpacingItemDecoration
import com.simplicityapp.R
import com.simplicityapp.databinding.ActivitySearchBinding
import java.util.ArrayList

class ActivitySearch : BaseActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var parentView: View? = null
    private var mAdapter: AdapterPlaceGrid? = null
    private var mAdapterSuggestion: AdapterSuggestionSearch? = null

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
            if (c.toString().trim { it <= ' ' }.isEmpty()) {
                binding.btnClear.visibility = View.GONE
            } else {
                binding.btnClear.visibility = View.VISIBLE
            }
        }

        override fun beforeTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        initActivity(binding)
        parentView = findViewById(android.R.id.content)
    }

    override fun initUI() {
        binding.run {
            editTextSearch.addTextChangedListener(textWatcher)
            btnClear.visibility = View.GONE
            recyclerView.layoutManager = StaggeredGridLayoutManager(UITools.getGridSpanCount(this@ActivitySearch), StaggeredGridLayoutManager.VERTICAL)
            recyclerView.addItemDecoration(
                SpacingItemDecoration(
                    UITools.getGridSpanCount(this@ActivitySearch),
                    UITools.dpToPx(4),
                    true
                )
            )
            recyclerSuggestion.layoutManager = LinearLayoutManager(this@ActivitySearch)
            recyclerSuggestion.setHasFixedSize(true)

            //set data and list adapter
            mAdapter = AdapterPlaceGrid(this@ActivitySearch, recyclerView, ArrayList(), StaggeredGridLayoutManager.VERTICAL, 0)
            recyclerView.adapter = mAdapter
            mAdapter?.setOnItemClickListener { v, obj -> ActivityPlaceDetail.navigate(this@ActivitySearch, v.findViewById(R.id.lyt_content), obj, AnalyticsConstants.SELECT_SEARCHED_PLACE) }

            //set data and list adapter suggestion
            mAdapterSuggestion = AdapterSuggestionSearch(this@ActivitySearch)
            recyclerSuggestion.adapter = mAdapterSuggestion
            showSuggestionSearch()

            showNotFoundView()
        }
        setupToolbar()
    }

    override fun initListeners() {
        mAdapterSuggestion?.setOnItemClickListener { view, viewModel, pos ->
            binding.editTextSearch.setText(viewModel)
            binding.lytSuggestion.visibility = View.GONE
            hideKeyboard()
            searchAction()
        }

        binding.btnClear.setOnClickListener {
            binding.editTextSearch.setText("")
            mAdapter?.resetListData()
            showNotFoundView()
        }

        binding.editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                searchAction()
                return@OnEditorActionListener true
            }
            false
        })

        binding.editTextSearch.setOnTouchListener { view, motionEvent ->
            showSuggestionSearch()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            false
        }
    }

    private fun showSuggestionSearch() {
        mAdapterSuggestion?.refreshItems()
        binding.lytSuggestion.visibility = View.VISIBLE
    }

    override fun onResume() {
        mAdapter?.notifyDataSetChanged()
        super.onResume()
    }

    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun searchAction() {
        binding.lytSuggestion.visibility = View.GONE
        showNotFoundView()
        val query = binding.editTextSearch.text.toString().trim { it <= ' ' }
        AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SEARCH_PLACE, query, false)
        if (query != "") {
            mAdapterSuggestion?.addSearchHistory(query)
            mAdapter?.resetListData()
            mAdapter?.insertData(Tools.filterItemsWithDistance(this, db!!.searchAllPlace(query)), false)
            showNotFoundView()
        } else {
            Toast.makeText(this, R.string.please_fill, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotFoundView() {
        if (mAdapter?.itemCount == 0) {
            binding.recyclerView.visibility = View.GONE
            binding.lytNoItem.root.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.lytNoItem.root.visibility = View.GONE
        }
    }
}
