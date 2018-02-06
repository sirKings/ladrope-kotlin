package com.ladrope.app.controller


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Adapters.SearchViewAdapter
import com.ladrope.app.Model.Cloth
import com.ladrope.app.R
import com.ladrope.app.Utilities.GENDER
import kotlinx.android.synthetic.main.fragment_search.view.*


/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    var searchView: SearchView? = null

    var clothList: ArrayList<Cloth?>? = null
    var clothAdapter: SearchViewAdapter? = null
    var progressBar : ProgressBar? = null
    var mainClothList =  ArrayList<Cloth?>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        progressBar = view.searchProgressBar
        progressBar?.visibility = View.VISIBLE

        searchView = view.findViewById(R.id.searchView)

        clothList = ArrayList()
        getCloths()

        clothAdapter = SearchViewAdapter(clothList, context!!)

        val layoutManager = LinearLayoutManager(context)
        val searchRV = view.search_reclyclerView

        searchRV.layoutManager = layoutManager
        searchRV.adapter = clothAdapter




        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                filterClothList(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterClothList(newText)
                return false
            }
        })

        return view
    }

    fun getCloths(){
        val dataRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER)

        dataRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot?) {
                for (data in p0!!.children) {
                    val cloth = data.getValue(Cloth::class.java)
                    clothList?.add(cloth)
                    mainClothList.add(cloth)
                    progressBar?.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }

    fun filterClothList(str: String){
        clothList?.clear()
        for (cloth: Cloth? in mainClothList){
            if (cloth?.name?.toLowerCase()!!.contains(str.toLowerCase())){
                clothList?.add(cloth)
            }
        }
        clothAdapter?.notifyDataSetChanged()
    }

}// Required empty public constructor
