package com.ladrope.app.controller


import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Service.getLocalBitmapUri
import com.ladrope.app.Service.updateCoupon
import com.ladrope.app.Service.userRef
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.GENDER_TYPE
import com.ladrope.app.Utilities.SHARE_INTENT_CODE
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.filter.view.*
import kotlinx.android.synthetic.main.fragment_shop.view.*


/**
 * A simple [Fragment] subclass.
 */
class ShopFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    var adapter: ShopClothAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Cloth>? = null
    var shopRV: RecyclerView? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var mEmptyText: TextView? = null
    var mCartItem:  TextView? = null
    var alertDialog: AlertDialog? = null
    var uid: String? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        mProgressBar = view.findViewById(R.id.shopProgressBar)
        mErrorText = view.findViewById(R.id.shopErrorText)
        mEmptyText = view.findViewById(R.id.shopEmptyText)
        shopRV = view.findViewById(R.id.shopRecyclerView)

        view.shopFilterBtn.setColorFilter(resources.getColor(R.color.cardview_light_background))


        uid = FirebaseAuth.getInstance().uid

        getCartItems(uid)

        try {
            val sharedPref = activity?.getSharedPreferences("LADROPE", Context.MODE_PRIVATE)
            val gender = sharedPref?.getString(GENDER_TYPE, GENDER)
            if (gender!=null){
                GENDER = gender
            }
        }catch (e:Exception){
            e.printStackTrace()
        }


        val query = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).orderByChild("date")
        setup(query)

        mCartItem = view.shopCartNo

        view.shopCartNo.setOnClickListener {
            openCart()
        }

        view.shopCartBtn.setOnClickListener {
            openCart()
        }

        view.shopCartLayout.setOnClickListener {
            openCart()
        }

        view.shopFilterBtn.setOnClickListener {
            openFilter()
        }

        swipeRefreshLayout = view.swipe_container
        swipeRefreshLayout?.setOnRefreshListener(this)
        swipeRefreshLayout?.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark)

        return view
    }

    fun setup(query: Query){
        Log.e("setup", "started")
        options = FirebaseRecyclerOptions.Builder<Cloth>()
                .setQuery(query, Cloth::class.java)
                .build()
        adapter = ShopClothAdapter(options!!, context!!, uid!!)
        layoutManager = LinearLayoutManager(context)

        //set up recycler view
        shopRV!!.layoutManager = layoutManager
        shopRV!!.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
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
            Toast.makeText(context, "You have earned one 5% discount coupon", Toast.LENGTH_LONG).show()
            updateCoupon()
        }
    }

    inner class ShopClothAdapter(options: FirebaseRecyclerOptions<Cloth>, private val context: Context, private val uid: String): FirebaseRecyclerAdapter<Cloth, ShopClothAdapter.ViewHolder>(options){
        var likeFilled: ImageButton? = null
        var likeOutLine: ImageButton? = null

        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
            swipeRefreshLayout?.isRefreshing = false
            if(adapter?.itemCount == 0){
                mEmptyText?.visibility = View.VISIBLE
            }
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

                labelName.setOnClickListener {
                    val tailorIntent = Intent(context, Tailor::class.java)
                    tailorIntent.putExtra("labelId", cloth.labelId)
                    tailorIntent.putExtra("labelName", cloth.label)
                    startActivity(tailorIntent)
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

    fun getCartItems(uid: String?){
        val cartRef = FirebaseDatabase.getInstance().reference.child("users").child(uid).child("cart")
            cartRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists()){
                        var numItems = 0
                        for (cloth in p0.children){
                            numItems++
                            mCartItem?.text = numItems.toString()
                        }
                    }else{
                        mCartItem?.text = "0"
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {
                    mCartItem?.text = "0"
                }
            })
    }

    fun openCart(){
        if (mCartItem?.text == "0"){
            Toast.makeText(context, "Your cart is empty", Toast.LENGTH_SHORT).show()
        }else{
            val cartIntent = Intent(context, Cart::class.java)
            Log.e("Cart", "clicked")
            startActivity(cartIntent)
        }
    }

    fun openFilter(){
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.filter, null)
        alertDialog = AlertDialog.Builder(context!!).create()

        val classList = arrayOf("Filter by type","Casual wears", "Corporate wears", "Traditional wears", "Shirts", "Trousers", "Gown", "Wedding Outfits", "Suits", "Ankara")
        val genderList = arrayOf("Filter by gender","male","female")

        val classAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, classList)
        val genderAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, genderList)

        view.filterClassSpinner.adapter = classAdapter
        view.filterGenderSpinner.adapter = genderAdapter
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->

        }
        alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, which -> dialog.dismiss() }

        alertDialog?.setView(view)
        alertDialog?.show()

        val b = alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        b?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val clas = view.filterClassSpinner.selectedItem as String
                val gender = view.filterGenderSpinner.selectedItem as String

                var filterClass: String? = ""
                var filterGender: String? = ""

                when (clas){
                    "Filter by type" -> filterClass = null
                    "Casual wears" -> filterClass = "casuals"
                    "Corporate wears" -> filterClass = "corporate"
                    "Traditional wears" -> filterClass = "native"
                    "Shirts" -> filterClass = "shirt"
                    "Gown" -> filterClass = "gown"
                    "Trousers" -> filterClass = "trousers"
                    "Wedding Outfits" -> filterClass = "wedding"
                    "Ankara" -> filterClass = "ankara"
                    "Suits" -> filterClass = "suit"
                }

                when (gender){
                    "Filter by gender" -> filterGender = null
                    "male" -> filterGender = "male"
                    "female" -> filterGender = "female"
                }

                if (filterClass != null && filterGender != null){
                    GENDER = filterGender
                    val query = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).orderByChild("tags").equalTo(filterClass)
                    mProgressBar?.visibility = View.VISIBLE
                    setup(query)
                    adapter?.startListening()
                    updateGender()

                }else if (filterClass != null && filterGender == null){
                    val query = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).orderByChild("tags").equalTo(filterClass)
                    mProgressBar?.visibility = View.VISIBLE
                    setup(query)
                    adapter?.startListening()
                } else if (filterClass == null && filterGender != null){
                    GENDER = filterGender
                    val query = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).orderByChild("date")
                    mProgressBar?.visibility = View.VISIBLE
                    setup(query)
                    adapter?.startListening()
                    updateGender()
                }
                alertDialog?.dismiss()
            }
        })
    }


    override fun onRefresh() {
        val query = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).orderByChild("date")
        setup(query)
        adapter?.startListening()
    }

    fun updateGender(){
        userRef.child(FirebaseAuth.getInstance().uid).child("gender").setValue(GENDER)
        val sharedPref = activity?.getSharedPreferences("LADROPE", MODE_PRIVATE)?.edit()
        sharedPref?.putString(GENDER_TYPE, GENDER)
        sharedPref?.apply()

    }

}// Required empty public constructor
