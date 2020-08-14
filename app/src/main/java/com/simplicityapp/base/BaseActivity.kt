package com.simplicityapp.base

import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.persistence.preferences.SharedPref

open class BaseActivity: AppCompatActivity() {

    lateinit var sharedPref: SharedPref
    var db: DatabaseHandler? = null

    fun initActivity(binding: ViewBinding) {
        setContentView(binding.root)
        db = DatabaseHandler(this)
        sharedPref = SharedPref(this)
        getArguments()
        initUI()
        initListeners()
    }

    open fun initUI() {

    }

    open fun initListeners() {

    }

    open fun getArguments() {

    }

}