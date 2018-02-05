package com.ladrope.app.controller


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.Model.Order
import com.ladrope.app.R
import com.ladrope.app.Utilities.formatDate
import com.squareup.picasso.Picasso


/**
 * A simple [Fragment] subclass.
 */
class OrdersFragment : Fragment() {

    private var pendingAdapter: OrderClothAdapter? = null
    private var completedAdapter: OrderClothAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var layoutManager2: RecyclerView.LayoutManager? = null
    private var options: FirebaseRecyclerOptions<Order>? = null
    private var options1: FirebaseRecyclerOptions<Order>? = null
    private var ordersRV: RecyclerView? = null
    private var cOrdersRV: RecyclerView? = null
    private var pendingProgressBar: ProgressBar? = null
    private var completedProgressBar: ProgressBar? = null
    private var pendingEmptyListText: TextView? = null
    private var completedEmptyListText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        ordersRV = view.findViewById(R.id.pendingOrdersRecyclerView)
        cOrdersRV = view.findViewById(R.id.completedOrdersRecyclerView)
        pendingEmptyListText = view.findViewById(R.id.emptyListIndicatorPending)
        completedEmptyListText = view.findViewById(R.id.emptyListIndicatorCompleted)
        completedProgressBar = view.findViewById(R.id.completeProgressBar)
        pendingProgressBar = view.findViewById(R.id.pendingProgressBar)


        val uid = FirebaseAuth.getInstance().uid

        val database = FirebaseDatabase.getInstance()
        val query1 = database.getReference("users").child(uid).child("orders")

        val query2 = database.reference.child("users").child(uid).child("completedorders")


        options = FirebaseRecyclerOptions.Builder<Order>()
            .setQuery(query1, Order::class.java)
            .build()

        options1 = FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(query2, Order::class.java)
                .build()


        pendingAdapter = OrderClothAdapter(options!!, "pending")
        layoutManager = LinearLayoutManager(context)
        layoutManager2 = LinearLayoutManager(context)

        completedAdapter = OrderClothAdapter(options1!!, "complete")

        //set up recycler view
        ordersRV!!.layoutManager = layoutManager
        ordersRV!!.adapter = pendingAdapter

        cOrdersRV!!.layoutManager =layoutManager2
        cOrdersRV!!.adapter = completedAdapter


        return view
    }

    override fun onStart() {
        super.onStart()
        pendingAdapter?.startListening()
        completedAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        pendingAdapter?.stopListening()
        completedAdapter?.stopListening()
    }

    inner class OrderClothAdapter(options: FirebaseRecyclerOptions<Order>, private val type: String): FirebaseRecyclerAdapter<Order, OrderClothAdapter.ViewHolder>(options){


        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.order_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Order) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bindItem(order: Order){
                val orderName = itemView.findViewById<TextView>(R.id.orderName)
                val status = itemView.findViewById<TextView>(R.id.orderStatus)
                val startDate = itemView.findViewById<TextView>(R.id.orderStartDate)
                val deliveryDate = itemView.findViewById<TextView>(R.id.orderDeliveryDate)
                val orderImage = itemView.findViewById<ImageView>(R.id.orderImageView)

                orderName.text = order.name
                status.text = order.status
                startDate.text = formatDate(order.startDate.toString())
                deliveryDate.text = formatDate(order.date.toString())

                Picasso.with(context).load(order.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(orderImage)


                    itemView.setOnClickListener {
                        if(type == "pending"){
                            val orderIntent = Intent(context, OrderActivity::class.java)
                            val ref = getRef(adapterPosition).toString()
                            orderIntent.putExtra("orderId", ref)
                            startActivity(orderIntent)
                        }else{
                                Toast.makeText(context, "This order has been completed", Toast.LENGTH_SHORT).show()
                        }

                    }
            }
        }


        override fun onDataChanged() {
            if (type == "complete"){
                completedProgressBar?.visibility = View.GONE
                if(itemCount == 0){
                    completedEmptyListText?.visibility = View.VISIBLE
                }
            }else {
                pendingProgressBar?.visibility = View.GONE
                if (itemCount == 0){
                    pendingEmptyListText?.visibility = View.VISIBLE
                }
            }
        }
    }

}// Required empty public constructor
