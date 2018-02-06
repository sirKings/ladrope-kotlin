package com.ladrope.app.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.controller.ClothActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.search_row.view.*

/**
 * Created by USER on 2/6/18.
 */
class SearchViewAdapter(private val list: ArrayList<Cloth?>?, private val context: Context): RecyclerView.Adapter<SearchViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindItem(list?.get(position)!!)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindItem(cloth: Cloth){
            val searchImage = itemView.searchImage
            val searchClothName = itemView.searchClothName
            val searchClothLabel = itemView.searchLabelName

            searchClothLabel?.text = cloth.label
            searchClothName?.text = cloth.name
            Picasso.with(context).load(cloth.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(searchImage)


            itemView.setOnClickListener {
                val clothIntent = Intent(context, ClothActivity::class.java)
                clothIntent.putExtra("clothKey", cloth.clothKey)
            }
        }

    }
}