package com.ladrope.app.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ladrope.app.Model.Option
import com.ladrope.app.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.option_row.view.*

/**
 * Created by USER on 2/7/18.
 */
class OptionsAdapter(private val list: ArrayList<Option?>?, private val context: Context): RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {

    val selectedOptions = ArrayList<Option>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.option_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindItem(list?.get(position)!!)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindItem(option: Option){
            val optionImage = itemView.optionImage
            val optionName = itemView.optionName

            optionName.text = option.name
            Picasso.with(context).load(option.image).placeholder(R.drawable.ic_account_box_black_24dp).into(optionImage)


            itemView.setOnClickListener {
                if (selectedOptions.contains(option)){
                    itemView.optionSelected.visibility = View.GONE
                    selectedOptions.remove(option)
                    itemView.optionSelect.visibility = View.VISIBLE
                }else{
                    itemView.optionSelect.visibility = View.GONE
                    itemView.optionSelected.visibility = View.VISIBLE
                    selectedOptions.add(option)
                }
            }
        }

    }
}