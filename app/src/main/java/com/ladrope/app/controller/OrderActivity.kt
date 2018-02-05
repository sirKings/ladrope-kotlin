package com.ladrope.app.controller

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.Cloth
import com.ladrope.app.Model.Order
import com.ladrope.app.R
import com.ladrope.app.Utilities.DECLINE_ORDER_URL
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.formatDate
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order.*
import org.json.JSONObject




class OrderActivity : AppCompatActivity() {

    var queue: RequestQueue? = null
    var order: Order? = null
    var uid: String? = null
    var mOldRating: Double? = null
    var mNumSold: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        queue = Volley.newRequestQueue(this)

        val ref = intent.extras.get("orderId") as String
        val index = ref.lastIndexOf('/')

        val userOrderId = ref.substring(index+1)

        uid = FirebaseAuth.getInstance().currentUser?.uid

        val databaseref = FirebaseDatabase.getInstance().reference.child("users").child(uid).child("orders").child(userOrderId)

        databaseref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                order = p0?.getValue(Order::class.java)
                activityLabelName.text = order?.label
                activityOrderName.text = order?.name
                activityOrderPrice.text = "NGN"+order?.price!!.toString() +".00"
                activityOrderStatus.text = order?.status
                activityOrderDeliveryDate.text = formatDate(order?.date.toString())
                activityOrderStartDate.text = formatDate(order?.startDate.toString())

                Picasso.with(this@OrderActivity).load(order?.image1).placeholder(R.drawable.ic_account_box_black_24dp).into(orderActivityImage)
            }

            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@OrderActivity, "Could not load order now", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun orderActivityCompleteOrder(view: View){
        val layoutInflater = this@OrderActivity.getLayoutInflater()
        val view = layoutInflater.inflate(R.layout.order_complete_rating, null)
        val alertDialog = AlertDialog.Builder(this).create()

        alertDialog.setCancelable(false)

        val ratingbar = view.findViewById<RatingBar>(R.id.ratingBar)

        var mRating = 0f

        ratingbar.setOnRatingBarChangeListener { rating, fl, b ->
            mRating = ratingbar.rating
        }

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Submit") { dialog, which ->
            //here we have to call Database firebase
            UpdateTask(mRating).execute()
            Log.e("Async", "Asyc called")
            dialog.dismiss()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, which -> dialog.dismiss() }
        alertDialog.setView(view)
        alertDialog.show()

    }

    fun orderActivityDeclineOrder(view: View){
        val layoutInflater = this@OrderActivity.getLayoutInflater()
        val view = layoutInflater.inflate(R.layout.order_decline_text, null)
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Please Why decline")

        alertDialog.setCancelable(false)

        val edtTxt = view.findViewById(R.id.edtTxt) as EditText

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Submit") { dialog, which ->
            val str_getTextFrom = edtTxt.getText().toString()
            //here we have to call Database firebase
            declineOrder(str_getTextFrom)
            dialog.dismiss()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, which -> dialog.dismiss() }
        alertDialog.setView(view)
        alertDialog.show()
    }


    fun declineOrder(str: String){
        val params = HashMap<String, String>()
        params.put("OrderId", order?.orderId!!)
        params.put("Reason", str)

        val parameters = JSONObject(params)
        val getRequest = JsonObjectRequest(Request.Method.POST, DECLINE_ORDER_URL, parameters,
                object : Response.Listener<JSONObject> {
                    override fun onResponse(response: JSONObject) {
                        // display response
                        Log.d("Response", response.toString())
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        //Log.d("Error.Response", error.message)
                    }
                }
        )

        // add it to the RequestQueue
        queue?.add(getRequest)
    }

    inner class UpdateTask internal constructor(private val mRating: Float) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {

            val clothRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(order?.clothId)
            clothRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists()){
                        val cloth = p0.getValue(Cloth::class.java)
                        mOldRating = cloth?.rating
                        mNumSold = cloth?.numSold
                        val rating = (mRating + mOldRating!!) / 2
                        Log.e("cloth", "Exists")
                        runUpdate(rating)
                    }else{
                        runUpdate(null)
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {

                }
            })

            return true
        }

        override fun onPostExecute(success: Boolean?) {

            if (success!!) {
                Toast.makeText(applicationContext, "Thanks for your Patronage", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(applicationContext, "Updates failed", Toast.LENGTH_SHORT).show()
            }
        }

    }


    fun runUpdate(rating: Double?){
        if (rating != null){
            Log.e("Async", "running update")
            val clothRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(order?.clothId)
            val updatesObj = HashMap<String, Any>()
            updatesObj.put("numSold", mNumSold!!+1)
            updatesObj.put("rating", rating)
            clothRef.updateChildren(updatesObj)
        }
        updateOrders()
    }

    fun updateOrders(){

        order?.status = "Completed"
        Log.e("Async", "updating orders")


        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid).child("completedorders").push().setValue(order).addOnCompleteListener {
            val userOrderRef =  FirebaseDatabase.getInstance().reference.child("users").child(uid).child("orders").child(order?.userOrderKey)
            Log.e("User", "completedorders")
            userOrderRef.setValue(null).addOnCompleteListener {

                val tailorRef = FirebaseDatabase.getInstance().reference.child("tailors").child(order?.labelId)
                tailorRef.child("orders").child(order?.tailorOrderKey)
                        .setValue(null).addOnCompleteListener {

                            val tailorCompletedOrderRef = tailorRef.child("completedOrders").push()
                            tailorCompletedOrderRef.setValue(order).addOnCompleteListener {
                                val orderRef = FirebaseDatabase.getInstance().reference.child("orders").child(order?.ordersKey)
                                orderRef.setValue(null).addOnCompleteListener {
                                    val completedOrderRef = FirebaseDatabase.getInstance().reference.child("completedOrders").push()
                                    completedOrderRef.setValue(order)
                                }

                            }
                }
            }
        }


    }

}
