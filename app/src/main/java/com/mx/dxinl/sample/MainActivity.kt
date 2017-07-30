package com.mx.dxinl.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pager.adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
                return view == `object`
            }

            override fun getCount(): Int {
                return 9
            }

            override fun instantiateItem(container: ViewGroup?, position: Int): Any {
                val textView = TextView(this@MainActivity)
                val toString = position.toString()
                textView.text = toString
                container?.addView(textView)
                return textView
            }

            override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
                container?.removeView((`object` as View));
            }
        }
        indicator.bindViewPager(pager)
    }
}
