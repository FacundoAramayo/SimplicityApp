package com.simplicityapp.modules.start.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.simplicityapp.R
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.databinding.ActivityLoginSuccessfulBinding
import com.simplicityapp.modules.main.activity.ActivityMain

class ActivityLoginSuccessful : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSuccessfulBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSuccessfulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        setupView()
        continueToMainActivity()
    }

    private fun setupView() {
        binding.apply {
            tvTitle.text = getString(R.string.login_successfull_title, mAuth.currentUser?.displayName.orEmpty())
            tvSubtitle.text = getString(R.string.login_successfull_subtitle)
        }
    }

    private fun continueToMainActivity() {
        val intent = Intent(this, ActivityMain::class.java)
        ActionTools.startActivityWithDelay(this,this, intent)
    }
}
