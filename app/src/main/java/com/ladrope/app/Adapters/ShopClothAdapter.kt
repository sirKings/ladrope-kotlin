package com.ladrope.app.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.squareup.picasso.Picasso





/**
 * Created by USER on 2/1/18.
 */
class ShopClothAdapter(options: FirebaseRecyclerOptions<Cloth>, private val context: Context): FirebaseRecyclerAdapter<Cloth, ShopClothAdapter.ViewHolder>(options){
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

            clothName.text = cloth.name
            labelName.text = cloth.label
            price.text = "NGN"+cloth.price.toString()+".00"
            numSold.text = "("+cloth.numSold.toString() + ")"
            rating.numStars = 5
            rating.rating = cloth.rating!!.toFloat()


//            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            val display = wm.defaultDisplay
//
//            val width = display.getWidth() // ((display.getWidth()*20)/100)
//            val height = display.getHeight()// ((display.getHeight()*30)/100)
//            val parms = LinearLayout.LayoutParams(width, height)
//            clothImage.layoutParams = parms

            Picasso.with(context).load(cloth.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(clothImage)
        }
    }
}