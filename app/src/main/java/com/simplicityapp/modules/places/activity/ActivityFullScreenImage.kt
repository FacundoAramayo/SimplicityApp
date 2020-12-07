package com.simplicityapp.modules.places.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.simplicityapp.baseui.adapter.AdapterFullScreenImage
import com.simplicityapp.R
import com.simplicityapp.base.config.Constant.EXTRA_IMGS
import com.simplicityapp.base.config.Constant.EXTRA_POS
import java.util.ArrayList

class ActivityFullScreenImage : AppCompatActivity() {

    private var adapter: AdapterFullScreenImage? = null
    private var viewPager: ViewPager? = null
    private var textPage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_full_screen_image)

        viewPager = findViewById<View>(R.id.pager) as ViewPager
        textPage = findViewById<View>(R.id.text_page) as TextView

        var items = ArrayList<String>()
        val i = intent
        val position = i.getIntExtra(EXTRA_POS, 0)
        items = i.getStringArrayListExtra(EXTRA_IMGS)
        adapter = AdapterFullScreenImage(this@ActivityFullScreenImage, this, items)
        val total = adapter!!.count
        viewPager!!.adapter = adapter

        textPage!!.text = String.format(getString(R.string.image_of), position + 1, total)

        // displaying selected image first
        viewPager!!.currentItem = position
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(pos: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(pos: Int) {
                textPage!!.text = String.format(getString(R.string.image_of), pos + 1, total)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        (findViewById<View>(R.id.btnClose) as ImageButton).setOnClickListener { finish() }
    }

}
