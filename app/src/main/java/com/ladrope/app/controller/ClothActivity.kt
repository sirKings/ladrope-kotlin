package com.ladrope.app.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.SELECTEDCLOTH
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_cloth.*

class ClothActivity : AppCompatActivity() {

    var mCloth: Cloth? = null
    var uid: String? = null
    var status: Boolean = false
    var mProgressBar: ProgressBar? = null
    var mClothStory: CardView? = null
    var mErrorText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloth)

        val clothKey = intent.extras.get("clothKey") as String

        getCloth(clothKey)
        uid = FirebaseAuth.getInstance().uid

        mProgressBar = clothProgressBar
        mClothStory = clothStory
        mErrorText = clothErrorText

        mClothStory?.visibility = View.GONE
        mProgressBar?.visibility = View.VISIBLE

    }

    fun startSetup(){
        val imageList = ArrayList<String>()
        imageList.add(mCloth?.image1!!)
        imageList.add(mCloth?.image2!!)
        imageList.add(mCloth?.image3!!)
        imageList.add(mCloth?.image4!!)

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

        clothClothName.text = mCloth?.name
        clothClothPrice.text = "NGN"+mCloth?.price+".00"
        clothDescription.text = mCloth?.description
        clothFabricType.text = mCloth?.fabricType
        clothLabelName.text = mCloth?.label
        clothNumSold.text = "("+mCloth?.numSold.toString()+")"
        clothRatingBar.rating = mCloth?.rating!!.toFloat()
        clothProdTime.text = mCloth?.time.toString() + " days"
        clothNumComment.text = mCloth?.numComment.toString()
        clothNumLikes.text = mCloth?.likes.toString()

        mCloth?.liked = getLikeStatus()

        status = getLikeStatus()

        if (mCloth?.liked!!){
            showLiked()
        }else{
            hideLiked()
        }
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
        SELECTEDCLOTH = mCloth
        val optionsIntent= Intent(this, Options::class.java)
        startActivity(optionsIntent)
    }

    fun like(view: View){
        if (status){
            hideLiked()
            likeCloth(!status)
        }else{
            showLiked()
            likeCloth(!status)
        }
        status = !status
    }

    fun share(view: View){

    }

    fun comment(view: View){
        val commentIntent = Intent(this, Comments::class.java)
        commentIntent.putExtra("clothKey", mCloth?.clothKey)
        startActivity(commentIntent)
    }

    fun showLiked(){
        clothLikeFilledBtn.visibility = View.VISIBLE
        clothLikeOutLineBtn.visibility = View.GONE
    }

    fun hideLiked(){
        clothLikeFilledBtn.visibility = View.GONE
        clothLikeOutLineBtn.visibility = View.VISIBLE
    }

    fun getLikeStatus(): Boolean{
        when (mCloth!!.likers?.get(uid)){
            true -> return true
        }
        return false
    }

    fun likeCloth(status: Boolean){

        val clothRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(mCloth!!.clothKey)
        if (status){
            val likes = mCloth?.likes!! + 1
            clothNumLikes.text = likes.toString()
            clothRef.child("likers").child(uid).setValue(true).addOnCompleteListener {
                clothRef.child("likes").setValue(likes)
            }
        }else{
            val likes = mCloth?.likes!! - 1
            clothNumLikes.text = likes.toString()
            clothRef.child("likers").child(uid).setValue(null).addOnCompleteListener {
                clothRef.child("likes").setValue(likes)
            }
        }
    }

    fun getCloth(key: String){
        val clothRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(key)
        clothRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                val cloth = p0?.getValue(Cloth::class.java)
                mCloth = cloth
                mClothStory?.visibility = View.VISIBLE
                mProgressBar?.visibility = View.GONE
                startSetup()
            }

            override fun onCancelled(p0: DatabaseError?) {
                mClothStory?.visibility = View.GONE
                mProgressBar?.visibility = View.GONE
                mErrorText?.visibility = View.VISIBLE
            }

        })
    }
}
