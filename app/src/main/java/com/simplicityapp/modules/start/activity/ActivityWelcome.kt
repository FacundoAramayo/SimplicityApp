package com.simplicityapp.modules.start.activity

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.TUTORIAL_BEGIN
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.TUTORIAL_COMPLETE
import com.simplicityapp.R
import com.simplicityapp.base.config.Constant.ARG_SECTION_NUMBER
import com.simplicityapp.databinding.ActivityWelcomeBinding

class ActivityWelcome : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        initActivity(binding)
        AnalyticsConstants.logAnalyticsEvent(TUTORIAL_BEGIN)
    }

    override fun initUI() {
        binding.apply {
            mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
            viewPager.adapter = mSectionsPagerAdapter
            startBackgroundAnimation(mainContent)
        }
    }

    override fun initListeners() {
        binding.apply {
            val pageListener = PageListener()
            viewPager.addOnPageChangeListener(pageListener)

            btnWelcomeBack.setOnClickListener {
                if (viewPager.currentItem != 0) {
                    viewPager.currentItem = viewPager.currentItem - 1
                }
            }
            btnWelcomeNext.setOnClickListener {
                if (viewPager.currentItem != 2) {
                    viewPager.currentItem = viewPager.currentItem.plus(1)
                } else {
                    AnalyticsConstants.logAnalyticsEvent(TUTORIAL_COMPLETE)
                    val i = Intent(this@ActivityWelcome, ActivityLogin::class.java)
                    startActivity(i)
                    finish()
                }
            }
        }
    }

    private fun startBackgroundAnimation(mainLayout: CoordinatorLayout?) {
        val animationDrawable: AnimationDrawable = mainLayout?.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(3500)
        animationDrawable.start()
    }

    private inner class PageListener : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            currentPage = position
            binding.apply {
                when (position + 1) {
                    1 -> {
                        btnWelcomeBack.visibility = View.INVISIBLE
                        btnWelcomeNext.text = getString(R.string.welcome_next)
                    }
                    2 -> {
                        btnWelcomeBack.visibility = View.VISIBLE
                        btnWelcomeNext.text = getString(R.string.welcome_next)
                    }
                    3 -> {
                        btnWelcomeBack.visibility = View.VISIBLE
                        btnWelcomeNext.text = getString(R.string.welcome_start)
                    }
                }
            }
        }
    }

    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val bundle = arguments
            var sectionNumber = -1
            if (bundle != null && bundle.containsKey(ARG_SECTION_NUMBER)) {
                sectionNumber = bundle.getInt(ARG_SECTION_NUMBER)
            } else if (bundle == null) {
                Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show()
            }

            var rootView = inflater.inflate(R.layout.fragment_welcome_1, container, false)

            rootView = when (sectionNumber) {
                1 -> inflater.inflate(R.layout.fragment_welcome_1, container, false)
                2 -> inflater.inflate(R.layout.fragment_welcome_2, container, false)
                3 -> inflater.inflate(R.layout.fragment_welcome_3, container, false)
                else -> rootView

            }
            return rootView
        }

        companion object {

            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount() = 3
    }

    companion object {
        private var currentPage: Int = 0
    }
}
