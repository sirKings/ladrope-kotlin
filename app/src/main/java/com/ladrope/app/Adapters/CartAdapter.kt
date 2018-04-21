package com.ladrope.app.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cart_row.view.*

/**
 * Created by USER on 2/21/18.
 */
class CartAdapter(private val list: ArrayList<Cloth>?, private val context: Context): RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cart_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindItem(list?.get(position)!!)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindItem(item: Cloth){
            val image = itemView.cartImageView
            val name = itemView.cartItemName
            val label = itemView.cartItemLabel
            val price = itemView.cartItemPrice
            val remove = itemView.cartRemoveBtn

            name.text = item.name?.capitalize()
            label.text = item.label?.capitalize()
            price.text = "Price: NGN"+item.price.toString()+".00"
            Picasso.with(context).load(item.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(image)

            remove.setOnClickListener {
                val cartRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("cart").child(item.cartKey)
                cartRef.setValue(null)
                list?.clear()
                //notifyDataSetChanged()
            }
        }

    }
}