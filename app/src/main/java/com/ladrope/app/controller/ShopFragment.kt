package com.ladrope.app.controller


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.Adapters.ShopClothAdapter
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Utilities.GENDER


/**
 * A simple [Fragment] subclass.
 */
class ShopFragment : Fragment() {

    var adapter: ShopClothAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Cloth>? = null
    var shopRV: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        shopRV = view.findViewById<RecyclerView>(R.id.shopRecyclerView)



        val uid = FirebaseAuth.getInstance().uid

        val database = FirebaseDatabase.getInstance()
        val query = database.getReference("cloths").child(GENDER)


        options = FirebaseRecyclerOptions.Builder<Cloth>()
                .setQuery(query, Cloth::class.java)
                .build()


        adapter = ShopClothAdapter(options!!, context!!)
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


}// Required empty public constructor
