package com.simplicityapp.modules.start.ui

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager

import com.simplicityapp.R
import com.simplicityapp.base.analytics.AnalyticsConstants
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.TUTORIAL_BEGIN
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.TUTORIAL_COMPLETE
import com.simplicityapp.base.ui.ActivityInterface

class ActivityWelcome : AppCompatActivity(), ActivityInterface {

    private var btnBack: Button? = null
    private var btnNext: Button? = null

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var mViewPager: ViewPager? = null
    private var toolbar: Toolbar? = null
    private var mainLayout: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        initUI()
        initListeners()
    }

    override fun initUI() {
        btnBack = findViewById<View>(R.id.btn_welcome_back) as Button
        btnNext = findViewById<View>(R.id.btn_welcome_next) as Button
        toolbar = findViewById(R.id.toolbar)
        mainLayout = findViewById<CoordinatorLayout>(R.id.main_content)
        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager = findViewById(R.id.container)
        mViewPager?.adapter = mSectionsPagerAdapter

        startBackgroundAnimation(mainLayout)
    }

    override fun initListeners() {
        AnalyticsConstants.logAnalyticsEvent(TUTORIAL_BEGIN)
        val pageListener = PageListener()
        mViewPager?.addOnPageChangeListener(pageListener)

        btnBack?.setOnClickListener {
            if (mViewPager?.currentItem != 0) {
                mViewPager?.currentItem = mViewPager!!.currentItem - 1
            }
        }

        btnNext?.setOnClickListener {
            if (mViewPager?.currentItem != 2) {
                mViewPager?.currentItem = mViewPager!!.currentItem.plus(1)
            } else {
                AnalyticsConstants.logAnalyticsEvent(TUTORIAL_COMPLETE)
                val i = Intent(this@ActivityWelcome, ActivityLogin::class.java)
                startActivity(i)
                finish()
            }
        }
    }

    private fun startBackgroundAnimation(mainLayout: CoordinatorLayout?) {
        val animationDrawable: AnimationDrawable = mainLayout?.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(3500)
        animationDrawable.start()
    }

    override fun getArguments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class PageListener : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            currentPage = position
            when (position + 1) {
                1 -> {
                    btnBack?.visibility = View.INVISIBLE
                    btnNext?.text = getString(R.string.welcome_next)
                }
                2 -> {
                    btnBack?.visibility = View.VISIBLE
                    btnNext?.text = getString(R.string.welcome_next)
                }
                3 -> {
                    btnBack?.visibility = View.VISIBLE
                    btnNext?.text = getString(R.string.welcome_start)
                }
            }
        }
    }

    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val bundle = arguments
            var section_number = -1
            if (bundle != null && bundle.containsKey(ARG_SECTION_NUMBER)) {
                section_number = bundle.getInt(ARG_SECTION_NUMBER)
            } else if (bundle == null) {
                Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show()
            }

            var rootView = inflater.inflate(R.layout.fragment_welcome_1, container, false)

            when (section_number) {
                1 -> {
                    rootView = inflater.inflate(R.layout.fragment_welcome_1, container, false)
                }
                2 -> {
                    rootView = inflater.inflate(R.layout.fragment_welcome_2, container, false)
                }
                3 -> {
                    rootView = inflater.inflate(R.layout.fragment_welcome_3, container, false)
                }
            }
            return rootView
        }

        companion object {
            private val ARG_SECTION_NUMBER = "section_number"

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

        override fun getCount(): Int {
            return 3
        }
    }

    companion object {
        private var currentPage: Int = 0
    }
}
