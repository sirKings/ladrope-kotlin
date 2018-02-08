package com.ladrope.app.Utilities

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.Model.Cloth
import com.ladrope.app.Model.Order
import com.ladrope.app.Model.PendingOrder
import com.ladrope.app.Model.User
import java.util.*


/**
 * Created by USER on 2/8/18.
 */
class PlaceOrdersTask internal constructor(private val cloths: ArrayList<Cloth>, private val user: User, private val ref: String, private val context: Context) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void): Boolean? {

        Log.e("Order", "Doinbackground started")

        for (cloth in cloths) {
            if (user.size != null && user.height != null) {
                submitOrder(cloth, user, ref)
            } else {
                saveOrder(cloth, user, ref)
            }

        }

        return true
    }

    override fun onPostExecute(success: Boolean?) {

        if (success!!) {
            Toast.makeText(context, "Your order has been submitted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Order submission failed", Toast.LENGTH_SHORT).show()
        }
    }


    fun submitOrder(cloth: Cloth, user: User, ref: String) {
        val orderRef = FirebaseDatabase.getInstance().reference.child("orders")
        val orderKey = orderRef.push().key

        val userOrderRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("orders")
        val userOrderKey = userOrderRef.push().key


        val tailorOrderRef = FirebaseDatabase.getInstance().reference.child("tailors").child(cloth.labelId).child("orders")
        val tailorOrderKey = tailorOrderRef.push().key

        val order = Order()
        order.user = FirebaseAuth.getInstance().uid
        order.userOrderKey = userOrderKey
        order.ordersKey = orderKey
        order.status = "Pending"
        order.image1 = cloth.image1
        order.options = cloth.selectedOption
        order.size = user.size
        order.tailorOrderKey = tailorOrderKey
        order.email = user.email
        order.orderId = ref
        order.displayName = user.displayName
        order.clientAddress = user.address
        order.clothId = cloth.clothKey
        order.cost = cloth.cost
        order.date = getTime(cloth.time!!.toString().toInt() + 2)
        order.startDate = getTime(0)
        order.tailorDate = getTime(cloth.time!!.toString().toInt())
        order.price = cloth.price
        order.label = cloth.label
        order.labelEmail = cloth.labelEmail
        order.labelId = cloth.labelId
        order.labelPhone = cloth.labelPhone
        order.name = cloth.name


        orderRef.child(orderKey).setValue(order).addOnCompleteListener {

            tailorOrderRef.child(tailorOrderKey).setValue(order).addOnCompleteListener {

                userOrderRef.child(userOrderKey).setValue(order).addOnCompleteListener {
                    CallTailorTask(cloth.labelPhone!!, context).execute()
                    Toast.makeText(context,"Your order has been submitted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun saveOrder(cloth: Cloth, user: User, ref: String) {
        val userOrderRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("savedOrders")
        val userOrderKey = userOrderRef.push().key

        val order = PendingOrder()

        order.user = FirebaseAuth.getInstance().uid
        order.userOrderKey = userOrderKey
        order.status = "Not Submitted"
        order.image1 = cloth.image1
        order.options = cloth.selectedOption
        order.email = user.email
        order.orderId = ref
        order.displayName = user.displayName
        order.clientAddress = user.address
        order.clothId = cloth.clothKey
        order.cost = cloth.cost
        order.startDate = getTime(0)
        order.price = cloth.price
        order.time = cloth.time.toString()
        order.label = cloth.label
        order.labelEmail = cloth.labelEmail
        order.labelId = cloth.labelId
        order.labelPhone = cloth.labelPhone
        order.name = cloth.name

        userOrderRef.child(userOrderKey).setValue(order).addOnCompleteListener {
            Toast.makeText(context, "Your order has been saved, Take your measurement video and we will start working on it", Toast.LENGTH_LONG).show()
        }
    }

}

private class CallTailorTask constructor(private val num: String, private val context: Context) : AsyncTask<String?, String?, String?>() {
    override fun doInBackground(vararg str: String?): String? {

        val queue = Volley.newRequestQueue(context)

        var phoneNumber = ""
        if (num.length == 11) {
            val number = num.substring(1)
            phoneNumber = "+234" + number
        }else{
            phoneNumber = num
        }
        val baseUrl = "http://smsplus4.routesms.com:8080/bulksms/bulksms?username=ladrope&password=rB6V4KDt&type=0&dlr=1&destination=$phoneNumber&source=LadRope&message=Hello%20you%20just%20got%20an%20order%20on%20Ladrope.com.%20Endeavour%20to%20complete%20and%20deliver%20on%20schedule"

        val getRequest = StringRequest(Request.Method.POST, baseUrl,
                object : Response.Listener<String> {
                    override fun onResponse(response: String) {
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
        queue.add(getRequest)
        return null
    }
}


class SubmitOrdersTask internal constructor(private val cloths: ArrayList<PendingOrder>, private val context: Context) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void): Boolean? {

        for (cloth in cloths) {
            submitOrder(cloth)
        }

        return true
    }

    override fun onPostExecute(success: Boolean?) {

        if (success!!) {
            Toast.makeText(context, "Your order has been submitted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Order submission failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitOrder(pendingOrder: PendingOrder){
        val savedOrderKey = pendingOrder.userOrderKey

        val orderRef = FirebaseDatabase.getInstance().reference.child("orders")
        val orderKey = orderRef.push().key

        val userOrderRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("orders")
        val userOrderKey = userOrderRef.push().key


        val tailorOrderRef = FirebaseDatabase.getInstance().reference.child("tailors").child(pendingOrder.labelId).child("orders")
        val tailorOrderKey = tailorOrderRef.push().key

        val order = Order()
        order.user = FirebaseAuth.getInstance().uid
        order.userOrderKey = userOrderKey
        order.ordersKey = orderKey
        order.status = "Pending"
        order.image1 = pendingOrder.image1
        order.options = pendingOrder.options
        order.size = pendingOrder.size
        order.tailorOrderKey = tailorOrderKey
        order.email = pendingOrder.email
        order.orderId = pendingOrder.orderId
        order.displayName = pendingOrder.displayName
        order.clientAddress = pendingOrder.clientAddress
        order.clothId = pendingOrder.clothId
        order.cost = pendingOrder.cost
        order.date = getTime(pendingOrder.time!!.toString().toInt() + 2)
        order.startDate = getTime(0)
        order.tailorDate = getTime(pendingOrder.time!!.toString().toInt())
        order.price = pendingOrder.price
        order.label = pendingOrder.label
        order.labelEmail = pendingOrder.labelEmail
        order.labelId = pendingOrder.labelId
        order.labelPhone = pendingOrder.labelPhone
        order.name = pendingOrder.name


        orderRef.child(orderKey).setValue(order).addOnCompleteListener {

            tailorOrderRef.child(tailorOrderKey).setValue(order).addOnCompleteListener {

                userOrderRef.child(userOrderKey).setValue(order).addOnCompleteListener {
                    CallTailorTask(pendingOrder.labelPhone!!, context).execute()
                    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("savedOrders").child(savedOrderKey).setValue(null)
                    Toast.makeText(context,"Your order has been submitted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}

fun getTime(num: Int): Date {
    val dt = Date()
    val c = Calendar.getInstance()
    c.time = dt
    c.add(Calendar.DATE, num)
    Log.e("Date", c.time.toString())
    return c.time
}