package com.ladrope.app.controller

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.ladrope.app.R
import kotlinx.android.synthetic.main.activity_home.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class Home : AppCompatActivity() {

    private var viewPager: ViewPager? = null
    private var mFragmentList = arrayListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        viewPager = view_pager

        val adapter = ViewPagerAdapter(getSupportFragmentManager())
        adapter.addFragment(ShopFragment())
        adapter.addFragment(OrdersFragment())
        adapter.addFragment(SearchFragment())
        adapter.addFragment(UserFragment())
        adapter.addFragment(ChatFragment())

        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 5

        tabs.setupWithViewPager(viewPager)

        val imageResId = intArrayOf(R.drawable.ic_home_black_24dp, R.drawable.ic_view_agenda_black_24dp, R.drawable.ic_search_black_24dp, R.drawable.ic_account_box_black_24dp, R.drawable.ic_library_books_black_24dp)

        for (i in imageResId.indices) {
            tabs.getTabAt(i)?.setIcon(imageResId[i])
        }

        tabs.setSelectedTabIndicatorColor(Color.parseColor("#004a00"))
        //tabs.setTabTextColors(Color.GRAY, Color.parseColor("#004a00"))

    }

    internal inner class ViewPagerAdapter(manager: FragmentManager): FragmentPagerAdapter(manager) {

        override fun getCount(): Int {
            return mFragmentList.size
        }
        override fun getItem(position:Int): Fragment {
            return mFragmentList.get(position)
        }
        fun addFragment(fragment: Fragment) {
            mFragmentList.add(fragment)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }

    }
}
