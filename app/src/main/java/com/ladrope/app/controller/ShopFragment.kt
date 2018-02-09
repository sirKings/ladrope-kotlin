package com.ladrope.app.controller


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.ladrope.app.Utilities.GENDER
import com.squareup.picasso.Picasso


/**
 * A simple [Fragment] subclass.
 */
class ShopFragment : Fragment() {

    var adapter: ShopClothAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Cloth>? = null
    var shopRV: RecyclerView? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        mProgressBar = view.findViewById(R.id.shopProgressBar)
        mErrorText = view.findViewById(R.id.shopErrorText)
        shopRV = view.findViewById(R.id.shopRecyclerView)

        val uid = FirebaseAuth.getInstance().uid

        val database = FirebaseDatabase.getInstance()
        val query = database.getReference("cloths").child(GENDER)


        options = FirebaseRecyclerOptions.Builder<Cloth>()
                .setQuery(query, Cloth::class.java)
                .build()


        adapter = ShopClothAdapter(options!!, context!!, uid!!)
        layoutManager = LinearLayoutManager(context)

        //set up recycler view
        shopRV!!.layoutManager = layoutManager
        shopRV!!.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    inner class ShopClothAdapter(options: FirebaseRecyclerOptions<Cloth>, private val context: Context, private val uid: String): FirebaseRecyclerAdapter<Cloth, ShopClothAdapter.ViewHolder>(options){
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
                val shareIcon = itemView.findViewById<ImageButton>(R.id.shopShareBtn)

                cloth.liked = getLikeStatus(cloth, uid)

                if (cloth.liked){
                    showLiked()
                }else{
                    hideLiked()
                }

                clothName.text = cloth.name
                labelName.text = cloth.label
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
                    Toast.makeText(context,"About to share", Toast.LENGTH_SHORT).show()
                }

                shareIcon.setOnClickListener {
                    Toast.makeText(context,"About to share", Toast.LENGTH_SHORT).show()
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
            likeOutLine?.visibility = View.GONE
        }

        fun hideLiked(){
            likeFilled?.visibility = View.GONE
            likeOutLine?.visibility = View.VISIBLE
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



}// Required empty public constructor
