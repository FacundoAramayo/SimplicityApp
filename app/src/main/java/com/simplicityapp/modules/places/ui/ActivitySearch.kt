package com.simplicityapp.modules.places.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import java.util.ArrayList

import com.simplicityapp.base.adapter.AdapterPlaceGrid
import com.simplicityapp.base.adapter.AdapterSuggestionSearch
import com.simplicityapp.base.data.database.DatabaseHandler
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.utils.UITools
import com.simplicityapp.base.widget.SpacingItemDecoration
import com.simplicityapp.R
import com.simplicityapp.base.ui.ActivityInterface

class ActivitySearch : AppCompatActivity(), ActivityInterface {

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var etSearch: EditText? = null
    private var btClear: ImageButton? = null
    private var parentView: View? = null

    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterPlaceGrid? = null

    private var recyclerSuggestion: RecyclerView? = null
    private var mAdapterSuggestion: AdapterSuggestionSearch? = null
    private var lytSuggestion: LinearLayout? = null
    private var lytNoItem: View? = null

    private var db: DatabaseHandler? = null

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
            if (c.toString().trim { it <= ' ' }.isEmpty()) {
                btClear?.visibility = View.GONE
            } else {
                btClear?.visibility = View.VISIBLE
            }
        }

        override fun beforeTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        parentView = findViewById(android.R.id.content)
        db = DatabaseHandler(this)
        initUI()
        initComponents()
        initListeners()
        setupToolbar()
    }

    override fun initUI() {
        lytSuggestion = findViewById<View>(R.id.lyt_suggestion) as LinearLayout
        etSearch = findViewById<View>(R.id.et_search) as EditText
        etSearch?.addTextChangedListener(textWatcher)

        btClear = findViewById<View>(R.id.bt_clear) as ImageButton
        btClear?.visibility = View.GONE
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerSuggestion = findViewById<View>(R.id.recyclerSuggestion) as RecyclerView

        recyclerView?.layoutManager = StaggeredGridLayoutManager(UITools.getGridSpanCount(this), StaggeredGridLayoutManager.VERTICAL)
        recyclerView?.addItemDecoration(SpacingItemDecoration(UITools.getGridSpanCount(this), UITools.dpToPx(this, 4), true))

        recyclerSuggestion?.layoutManager = LinearLayoutManager(this)
        recyclerSuggestion?.setHasFixedSize(true)

        lytNoItem = findViewById(R.id.lyt_no_item) as View
    }

    override fun initListeners() {
        mAdapterSuggestion?.setOnItemClickListener { view, viewModel, pos ->
            Log.d("LOG-", "OnClick AdapterSuggestion")
            etSearch?.setText(viewModel)
            lytSuggestion?.visibility = View.GONE
            hideKeyboard()
            searchAction()
        }

        btClear?.setOnClickListener {
            etSearch?.setText("")
            mAdapter?.resetListData()
            showNotFoundView()
        }

        etSearch?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                searchAction()
                return@OnEditorActionListener true
            }
            false
        })

        etSearch?.setOnTouchListener { view, motionEvent ->
            showSuggestionSearch()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            false
        }
    }

    override fun getArguments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initComponents() {
        //set data and list adapter
        mAdapter = AdapterPlaceGrid(this, recyclerView, ArrayList())
        recyclerView?.adapter = mAdapter
        mAdapter?.setOnItemClickListener { v, obj -> ActivityPlaceDetail.navigate(this@ActivitySearch, v.findViewById(R.id.lyt_content), obj) }

        //set data and list adapter suggestion
        mAdapterSuggestion = AdapterSuggestionSearch(this)
        recyclerSuggestion!!.adapter = mAdapterSuggestion
        showSuggestionSearch()

        showNotFoundView()
    }

    private fun showSuggestionSearch() {
        mAdapterSuggestion?.refreshItems()
        lytSuggestion?.visibility = View.VISIBLE
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
        lytSuggestion?.visibility = View.GONE
        showNotFoundView()
        val query = etSearch!!.text.toString().trim { it <= ' ' }
        if (query != "") {
            mAdapterSuggestion!!.addSearchHistory(query)
            mAdapter?.resetListData()
            mAdapter?.insertData(Tools.filterItemsWithDistance(this, db!!.searchAllPlace(query)))
            showNotFoundView()
        } else {
            Toast.makeText(this, R.string.please_fill, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotFoundView() {
        if (mAdapter?.itemCount == 0) {
            recyclerView?.visibility = View.GONE
            lytNoItem?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            lytNoItem?.visibility = View.GONE
        }
    }
}
