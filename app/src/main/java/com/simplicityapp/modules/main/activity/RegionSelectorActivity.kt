package com.simplicityapp.modules.main.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplicityapp.R
import com.simplicityapp.base.config.Constant.IS_FROM_HOME
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.baseui.adapter.RegionAdapter
import com.simplicityapp.databinding.ActivityRegionSelectorBinding
import com.simplicityapp.modules.main.viewmodel.RegionSelectorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegionSelectorActivity : AppCompatActivity() {

    private lateinit var viewModel: RegionSelectorViewModel
    private lateinit var binding: ActivityRegionSelectorBinding

    private var adapter: RegionAdapter? = null
    private lateinit var sharedPref: SharedPref
    private var fromHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegionSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(RegionSelectorViewModel::class.java)
        sharedPref = SharedPref(this)

        fromHome = intent.getBooleanExtra(IS_FROM_HOME, false)

        initUI()
    }

    private fun initUI() {
        binding.recyclerViewRegion.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        GlobalScope.launch(Dispatchers.Main) {
            val items = viewModel.getRegionsAsync().body()
            adapter = RegionAdapter(applicationContext, items)
            binding.recyclerViewRegion.adapter = adapter
            adapter?.notifyDataSetChanged()
        }

        binding.buttonRegionSelectorCancel.setOnClickListener {
            if (sharedPref.regionId == -1) {
                Toast.makeText(applicationContext, resources.getString(R.string.region_selector_cancel_warning), Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }

        binding.buttonRegionSelectorOk.setOnClickListener {
            adapter?.selected?.let {
                sharedPref.regionId = it.regionId
                sharedPref.regionLat = it.latitude
                sharedPref.regionLon = it.longitude
                sharedPref.regionTitle = it.name
                if (fromHome) {
                    finish()
                } else {
                    startActivity(Intent(this, ActivityMain::class.java))
                }
            } ?: showAlert()
        }

    }

    private fun showAlert() {
        if (fromHome) {
            finish()
        } else {
            Toast.makeText(applicationContext, resources.getString(R.string.region_selector_ok_warning), Toast.LENGTH_SHORT).show()
        }
    }


    companion object {

        private lateinit var instance: RegionSelectorActivity

        val RegionSelectorActivityInstance: RegionSelectorActivity
            get() {
                if (instance == null) {
                    instance =
                        RegionSelectorActivity()
                }

                return instance
            }
    }
}
