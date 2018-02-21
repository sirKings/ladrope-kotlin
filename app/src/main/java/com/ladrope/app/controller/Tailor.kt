package com.ladrope.app.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Service.getLocalBitmapUri
import com.ladrope.app.Service.updateCoupon
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.SHARE_INTENT_CODE
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tailor.*

class Tailor : AppCompatActivity() {

    var adapter: TailorClothAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Cloth>? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tailor)

        val labelId = intent.extras.get("labelId") as String

        uid = FirebaseAuth.getInstance().uid

        val query = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).orderByChild("labelId").equalTo(labelId)

        options = FirebaseRecyclerOptions.Builder<Cloth>()
                .setQuery(query, Cloth::class.java)
                .build()
        adapter = TailorClothAdapter(options!!, this, uid!!)
        layoutManager = LinearLayoutManager(this)

        mProgressBar = tailorProgressBar
        mErrorText = tailorErrorText

        //set up recycler view
        tailorRecyclerView!!.layoutManager = layoutManager
        tailorRecyclerView!!.adapter = adapter
    }

    inner class TailorClothAdapter(options: FirebaseRecyclerOptions<Cloth>, private val context: Context, private val uid: String): FirebaseRecyclerAdapter<Cloth, TailorClothAdapter.ViewHolder>(options){
        var likeFilled: ImageButton? = null
        var likeOutLine: ImageButton? = null

        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
        }

        override fun onError(error: DatabaseError) {
            super.onError(error)
            mErrorText?.visibility = View.VISIBLE
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.cloth_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Cloth) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bindItem(cloth: Cloth){
                val clothName = itemView.findViewById<TextView>(R.id.shopClothName)
                val labelName = itemView.findViewById<TextView>(R.id.shopLabelName)
                val price = itemView.findViewById<TextView>(R.id.shopClothPrice)
                val numSold = itemView.findViewById<TextView>(R.id.ShopNumSold)
                val rating = itemView.findViewById<RatingBar>(R.id.shopRatingBar)
                val clothImage = itemView.findViewById<ImageView>(R.id.shopClothImage)
                val moreBtn = itemView.findViewById<Button>(R.id.shopSeeMoreBtn)
                val numComments = itemView.findViewById<TextView>(R.id.shopNumComment)
                val numLikes = itemView.findViewById<TextView>(R.id.shopNumLikes)
                val shareBtn = itemView.findViewById<LinearLayout>(R.id.shopShareLayout)
                val commentBtn = itemView.findViewById<LinearLayout>(R.id.shopCommentLayout)
                val likeBtn = itemView.findViewById<LinearLayout>(R.id.shopLikeLayout)
                likeFilled = itemView.findViewById(R.id.shopLikeFilledBtn)
                likeOutLine = itemView.findViewById(R.id.shopLikeOutLineBtn)
                val commentIcon = itemView.findViewById<ImageButton>(R.id.shopCommentBtn)
                commentIcon.setColorFilter(resources.getColor(R.color.colorPrimary))
                val shareIcon = itemView.findViewById<ImageButton>(R.id.shopShareBtn)
                shareIcon.setColorFilter(resources.getColor(R.color.colorPrimary))

                cloth.liked = getLikeStatus(cloth, uid)

                if (cloth.liked){
                    showLiked()
                }else{
                    hideLiked()
                }

                clothName.text = cloth.name
                if(cloth.label!!.length > 18){
                    labelName.text = cloth.label?.substring(0,17)
                }else{
                    labelName.text = cloth.label
                }
                price.text = "NGN"+cloth.price.toString()+".00"
                numSold.text = "("+cloth.numSold.toString() + ")"
                rating.numStars = 5
                rating.rating = cloth.rating!!.toFloat()
                numComments.text = cloth.numComment.toString()
                numLikes.text = cloth.likes.toString()

                Picasso.with(context).load(cloth.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(clothImage)

                moreBtn.setOnClickListener {
                    val clothIntent = Intent(context, ClothActivity::class.java)
                    clothIntent.putExtra("clothKey", cloth.clothKey)
                    context.startActivity(clothIntent)
                }

                likeBtn.setOnClickListener {
                    if (cloth.liked){
                        hideLiked()
                        likeCloth(cloth, uid, false)
                    }else{
                        showLiked()
                        likeCloth(cloth, uid, true)
                    }

                }

                numLikes.setOnClickListener {
                    if (cloth.liked){
                        hideLiked()
                        likeCloth(cloth, uid, false)
                    }else{
                        showLiked()
                        likeCloth(cloth, uid, true)
                    }
                }

                likeFilled?.setOnClickListener {
                    if (cloth.liked){
                        hideLiked()
                        likeCloth(cloth, uid, false)
                    }else{
                        showLiked()
                        likeCloth(cloth, uid, true)
                    }
                }

                likeOutLine?.setOnClickListener {
                    if (cloth.liked){
                        hideLiked()
                        likeCloth(cloth, uid, false)
                    }else{
                        showLiked()
                        likeCloth(cloth, uid, true)
                    }
                }



                shareBtn.setOnClickListener {
                    shareDesign(cloth, context)
                }

                shareIcon.setOnClickListener {
                    shareDesign(cloth, context)
                }

                commentBtn.setOnClickListener{
                    val commentIntent = Intent(context, Comments::class.java)
                    commentIntent.putExtra("clothKey", cloth.clothKey)
                    context.startActivity(commentIntent)
                }

                numComments.setOnClickListener {
                    val commentIntent = Intent(context, Comments::class.java)
                    commentIntent.putExtra("clothKey", cloth.clothKey)
                    context.startActivity(commentIntent)
                }

                commentIcon.setOnClickListener {
                    val commentIntent = Intent(context, Comments::class.java)
                    commentIntent.putExtra("clothKey", cloth.clothKey)
                    context.startActivity(commentIntent)
                }

            }
        }

        fun showLiked(){
            likeFilled?.visibility = View.VISIBLE
            likeFilled?.setColorFilter(resources.getColor(R.color.colorPrimary))
            likeOutLine?.visibility = View.GONE
        }

        fun hideLiked(){
            likeFilled?.visibility = View.GONE
            likeOutLine?.visibility = View.VISIBLE
            likeOutLine?.setColorFilter(resources.getColor(R.color.colorPrimary))
        }

        fun getLikeStatus(cloth: Cloth, uid: String): Boolean{
            when (cloth.likers?.get(uid)){
                true -> return true
            }
            return false
        }

        fun likeCloth(cloth: Cloth, uid: String, status: Boolean){

            val clothRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(cloth.clothKey)
            if (status){
                val likes = cloth.likes!! + 1
                clothRef.child("likers").child(uid).setValue(true).addOnCompleteListener {
                    clothRef.child("likes").setValue(likes)
                }
            }else{
                val likes = cloth.likes!! - 1
                clothRef.child("likers").child(uid).setValue(null).addOnCompleteListener {
                    clothRef.child("likes").setValue(likes)
                }
            }
        }
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

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
