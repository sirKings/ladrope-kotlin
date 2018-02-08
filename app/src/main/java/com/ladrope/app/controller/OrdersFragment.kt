package com.ladrope.app.controller


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
    private var savedAdapter: OrderClothAdapter? = null

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var layoutManager2: RecyclerView.LayoutManager? = null
    private var layoutManager3: RecyclerView.LayoutManager? = null

    private var options: FirebaseRecyclerOptions<Order>? = null
    private var options1: FirebaseRecyclerOptions<Order>? = null
    private var options2: FirebaseRecyclerOptions<Order>? = null

    private var ordersRV: RecyclerView? = null
    private var cOrdersRV: RecyclerView? = null
    private var sOrdersRV: RecyclerView? = null

    private var pendingProgressBar: ProgressBar? = null
    private var completedProgressBar: ProgressBar? = null
    private var savedProgressBar: ProgressBar? = null

    private var pendingEmptyListText: TextView? = null
    private var completedEmptyListText: TextView? = null
    private var savedEmptyListText: TextView? = null

    private var pendingLayout: ConstraintLayout? = null
    private var savedLayout: ConstraintLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        ordersRV = view.findViewById(R.id.pendingOrdersRecyclerView)
        cOrdersRV = view.findViewById(R.id.completedOrdersRecyclerView)
        sOrdersRV = view.findViewById(R.id.savedOrdersRecyclerView)


        pendingEmptyListText = view.findViewById(R.id.emptyListIndicatorPending)
        completedEmptyListText = view.findViewById(R.id.emptyListIndicatorCompleted)
        savedEmptyListText = view.findViewById(R.id.emptyListIndicatorSaved)

        completedProgressBar = view.findViewById(R.id.completeProgressBar)
        pendingProgressBar = view.findViewById(R.id.pendingProgressBar)
        savedProgressBar = view.findViewById(R.id.saveProgressBar)

        pendingLayout = view.findViewById(R.id.pending)
        savedLayout = view.findViewById(R.id.saved)


        val uid = FirebaseAuth.getInstance().uid

        val database = FirebaseDatabase.getInstance()
        val query1 = database.getReference("users").child(uid).child("orders")
        val query2 = database.reference.child("users").child(uid).child("completedorders")
        val query3 = database.reference.child("users").child(uid).child("savedOrders")


        options = FirebaseRecyclerOptions.Builder<Order>()
            .setQuery(query1, Order::class.java)
            .build()

        options1 = FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(query2, Order::class.java)
                .build()

        options2 = FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(query3, Order::class.java)
                .build()


        pendingAdapter = OrderClothAdapter(options!!, "pending")
        savedAdapter = OrderClothAdapter(options2!!, "saved")

        layoutManager = LinearLayoutManager(context)
        layoutManager2 = LinearLayoutManager(context)
        layoutManager3 = LinearLayoutManager(context)

        completedAdapter = OrderClothAdapter(options1!!, "complete")

        //set up recycler view
        ordersRV!!.layoutManager = layoutManager
        ordersRV!!.adapter = pendingAdapter
        sOrdersRV!!.adapter = savedAdapter

        cOrdersRV!!.layoutManager =layoutManager2
        cOrdersRV!!.adapter = completedAdapter
        sOrdersRV!!.layoutManager = layoutManager3


        return view
    }

    override fun onStart() {
        super.onStart()
        pendingAdapter?.startListening()
        completedAdapter?.startListening()
        savedAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        pendingAdapter?.stopListening()
        completedAdapter?.stopListening()
        savedAdapter?.stopListening()
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
                startDate.text = formatDate(order.startDate!!)
                if (type == "saved"){
                    deliveryDate.text = ""
                }else{
                    deliveryDate.text = formatDate(order.date!!)
                }

                Picasso.with(context).load(order.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(orderImage)


                    itemView.setOnClickListener {
                        if(type == "pending"){
                            val orderIntent = Intent(context, OrderActivity::class.java)
                            val ref = getRef(adapterPosition).toString()
                            orderIntent.putExtra("orderId", ref)
                            startActivity(orderIntent)
                        }else if (type == "complete"){
                                Toast.makeText(context, "This order has been completed", Toast.LENGTH_SHORT).show()
                        }else{
                            val alertDialog = AlertDialog.Builder(context!!).create()
                                alertDialog.setMessage("Please enter your height and take your measurement video to submit this order")
                                alertDialog.setTitle("Order Not Submitted")
                                alertDialog.setCancelable(false)
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Dismiss", object: DialogInterface.OnClickListener{
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        alertDialog.dismiss()
                                    }

                                })
                                alertDialog.show()
                        }

                    }
            }
        }


        override fun onDataChanged() {
            if (type == "complete"){
                Log.e("completed","called" )
                completedProgressBar?.visibility = View.GONE
                if(itemCount == 0){
                    completedEmptyListText?.visibility = View.VISIBLE
                }
            }else if (type == "pending") {
                Log.e("pending","called" )
                pendingProgressBar?.visibility = View.GONE
                if (itemCount == 0){
                    pendingLayout?.visibility = View.GONE
                }
            }else if (type == "saved"){
                Log.e("saved","called" )
                savedProgressBar?.visibility = View.GONE
                if (itemCount == 0){
                    savedLayout?.visibility = View.GONE
                }
            }
        }
    }

}// Required empty public constructor
