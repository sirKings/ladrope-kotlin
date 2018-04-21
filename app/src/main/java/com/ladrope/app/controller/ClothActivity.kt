package com.ladrope.app.controller

import android.app.Activity
import android.content.Context
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
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Service.getLocalBitmapUri
import com.ladrope.app.Service.updateCoupon
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.SELECTEDCLOTH
import com.ladrope.app.Utilities.SHARE_INTENT_CODE
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_cloth.*

class ClothActivity : AppCompatActivity() {

    var mCloth: Cloth? = null
    var uid: String? = null
    var status: Boolean = false
    var mProgressBar: ProgressBar? = null
    var mClothStory: CardView? = null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloth)
        var clothKey = ""
        try {
            clothKey = intent.extras.get("clothKey") as String
        }catch (e: Exception){
            e.printStackTrace()
        }

        if (clothKey == ""){
            try {
                val data = intent.data
                val keyIndex = data.encodedPath.lastIndexOf("/")
                clothKey = data.encodedPath.substring(keyIndex+1)

            }catch (e: Exception){
                e.printStackTrace()
            }
        }


        getCloth(clothKey)
        uid = FirebaseAuth.getInstance().uid

        mProgressBar = clothProgressBar
        mClothStory = clothStory
        mErrorText = clothErrorText
        mEmptyText = clothEmptyText

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

        clothClothName.text = mCloth?.name?.capitalize()
        clothClothPrice.text = "NGN"+mCloth?.price+".00"
        clothDescription.text = mCloth?.description?.capitalize()
        clothFabricType.text = mCloth?.fabricType?.capitalize()
        clothLabelName.text = mCloth?.label?.capitalize()
        clothNumSold.text = "("+mCloth?.numSold.toString()+")"
        clothRatingBar.rating = mCloth?.rating!!.toFloat()
        clothProdTime.text = mCloth?.time.toString() + " days"
        clothNumComment.text = mCloth?.numComment.toString()
        clothNumLikes.text = mCloth?.likes.toString()

        clothCommentBtn.setColorFilter(resources.getColor(R.color.colorPrimary))
        clothShareBtn.setColorFilter(resources.getColor(R.color.colorPrimary))

        mCloth?.liked = getLikeStatus()

        status = getLikeStatus()

        if (mCloth?.liked!!){
            showLiked()
        }else{
            hideLiked()
        }

        clothLabelName.setOnClickListener {
            val tailorIntent = Intent(this, Tailor::class.java)
            tailorIntent.putExtra("labelId", mCloth?.labelId)
            tailorIntent.putExtra("labelName", mCloth?.label)
            startActivity(tailorIntent)
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
        shareDesign(mCloth!!, this)
    }

    fun comment(view: View){
        val commentIntent = Intent(this, Comments::class.java)
        commentIntent.putExtra("clothKey", mCloth?.clothKey)
        startActivity(commentIntent)
    }

    fun showLiked(){
        clothLikeFilledBtn.visibility = View.VISIBLE
        clothLikeFilledBtn.setColorFilter(resources.getColor(R.color.colorPrimary))
        clothLikeOutLineBtn.visibility = View.GONE
    }

    fun hideLiked(){
        clothLikeFilledBtn.visibility = View.GONE
        clothLikeOutLineBtn.visibility = View.VISIBLE
        clothLikeOutLineBtn.setColorFilter(resources.getColor(R.color.colorPrimary))
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

                if(p0!!.exists()){
                    val cloth = p0.getValue(Cloth::class.java)
                    mCloth = cloth
                    mClothStory?.visibility = View.VISIBLE
                    mProgressBar?.visibility = View.GONE
                    val ab = supportActionBar
                    ab?.title = mCloth?.name?.capitalize()
                    startSetup()
                }else{
                    mEmptyText?.visibility = View.VISIBLE
                    mProgressBar?.visibility = View.GONE
                }

            }

            override fun onCancelled(p0: DatabaseError?) {
                mClothStory?.visibility = View.GONE
                mProgressBar?.visibility = View.GONE
                mErrorText?.visibility = View.VISIBLE
            }

        })
    }

    fun shareDesign(cloth: Cloth, context: Context){
        val text = "https://ladrope.com/cloth/${cloth.clothKey}"
        val pictureUri = getLocalBitmapUri(cloth.image1!!, context)
        val shareIntent = Intent()
        shareIntent.setAction(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri)
        shareIntent.setPackage("com.whatsapp")
        shareIntent.type = "image/*"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(Intent.createChooser(shareIntent, "Share design"), SHARE_INTENT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SHARE_INTENT_CODE){
            Toast.makeText(this, "You have earned one 5% discount coupon", Toast.LENGTH_LONG).show()
            updateCoupon()
        }
    }
}
