package com.ladrope.app.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Utilities.SELECTEDCLOTH
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_cloth.*

class ClothActivity : AppCompatActivity() {

    var cloth: Cloth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloth)

        cloth = SELECTEDCLOTH

        val imageList = ArrayList<String>()
        imageList.add(cloth?.image1!!)
        imageList.add(cloth?.image2!!)
        imageList.add(cloth?.image3!!)
        imageList.add(cloth?.image4!!)

        val adapter = ViewPagerAdapter(imageList)

        clothClothImage.adapter = adapter

        addBottomDots(0)

        clothClothImage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                //Log.e("PageScrollState", state.toString())
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //Log.e("PageScrolled", position.toString())
            }

            override fun onPageSelected(position: Int) {
                addBottomDots(position)
            }


        })

        clothClothName.text = cloth?.name
        clothClothPrice.text = "NGN"+cloth?.price+".00"
        clothDescription.text = cloth?.description
        clothFabricType.text = cloth?.fabricType
        clothLabelName.text = cloth?.label
        clothNumSold.text = "("+cloth?.numSold.toString()+")"
        clothRatingBar.rating = cloth?.rating!!.toFloat()
        clothProdTime.text = cloth?.time.toString() + " days"

    }

    inner class ViewPagerAdapter(private val list: ArrayList<String>) : PagerAdapter(){
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
           return list.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = ImageView(this@ClothActivity)


            Picasso.with(this@ClothActivity).load(list[position]).placeholder(R.drawable.ic_account_box_black_24dp).into(imageView)

            imageView.scaleType = ImageView.ScaleType.FIT_XY

            container.addView(imageView, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            return imageView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

    private fun addBottomDots(index: Int) {
        val dots = arrayOfNulls<TextView>(4)

        clothTabLayout.removeAllViews()
        clothTabLayout.bringToFront()
        for (i in 0 until 4) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(Color.rgb(187,187,187))
            clothTabLayout.addView(dots[i])
        }
        dots[index]?.setTextColor(Color.rgb(0,74,0))
    }


    fun clothOrderNow(view: View){
        SELECTEDCLOTH = cloth
        val optionsIntent= Intent(this, Options::class.java)
        startActivity(optionsIntent)
    }
}
