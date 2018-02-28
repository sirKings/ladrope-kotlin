package com.ladrope.app.controller

import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Adapters.CartAdapter
import com.ladrope.app.Model.Cloth
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.ladrope.app.Utilities.PlaceOrdersTask
import com.ladrope.app.Utilities.VERIFY_PAYMENT_URL
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.paycard.view.*

class Cart : AppCompatActivity() {

    var cartItems: ArrayList<Cloth>? = null
    var adapter: CartAdapter? = null
    var progressBar: ProgressBar? = null
    var errorText: TextView? = null
    var mUser: User? = null
    var infoText: TextView? = null
    var queue: RequestQueue? = null
    var alertDialog: AlertDialog? = null
    var price: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


        cartItems = ArrayList()
        getCartItems()

        progressBar = cartProgressBar
        progressBar?.visibility = View.VISIBLE

        errorText = cartErrorText


        val layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter(cartItems,this)

        cartRecyclerView.layoutManager = layoutManager
        cartRecyclerView.adapter = adapter

        queue = Volley.newRequestQueue(this)

        PaystackSdk.initialize(getApplicationContext())
    }

    fun getCartItems(){
        val cartRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("cart")

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                if (p0?.exists()!!){
                    for (cloth in p0.children){
                        val item = cloth.getValue(Cloth::class.java)
                        cartItems?.add(item!!)
                        adapter?.notifyDataSetChanged()
                        getTotalPrice()
                        progressBar?.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                errorText?.visibility = View.VISIBLE
            }
        })
    }

    fun getTotalPrice(){
        var total = 0
        for (cloth in cartItems!!){
            total += cloth!!.price!!.toInt()
            getUser(total)
        }
    }

    fun cartPlaceOrder(view: View){
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.paycard, null)
        alertDialog = AlertDialog.Builder(this).create()

        alertDialog?.setCancelable(false)

        if (mUser?.coupons != null){
            view.total.text = "NGN"+price.toString()+".00"
            view.total.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
        }else{
            view.textVi15.visibility = View.GONE
            view.total.visibility = View.GONE
        }

        view.paycardAmount.text = "NGN"+price.toInt().toString()+".00"


        val cardNumbern = view.paycardNumber
        val cardMonth = view.paycardMonth
        val cardYear = view.paycardYear
        val cardCvv = view.paycardCVV

        progressBar = view.paycardProgress
        progressBar?.visibility = View.GONE

        infoText = view.paycardInfo
        infoText?.visibility = View.GONE


        alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "Submit") { dialog, which ->

        }
        alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, which -> dialog.dismiss() }

        alertDialog?.setView(view)
        alertDialog?.show()

        val b = alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        b?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val cardNumber = cardNumbern.text.toString()
                val expiryMonth = cardMonth.text.toString() //any month in the future
                val expiryYear = cardYear.text.toString() // any year in the future. '2018' would work also!
                val cvv = cardCvv.text.toString()  // cvv of the test card
                if (cardNumber != "" && expiryMonth!= "" && expiryYear!="" && cvv!=""){
                    val card = Card(cardNumber, expiryMonth.toInt(), expiryYear.toInt(), cvv)
                    if (card.isValid) {
                        // charge card
                        chargeCard(card, price)
                    } else {
                        //do something
                        Toast.makeText(applicationContext, "Please enter valid card details", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(applicationContext, "Please enter all details", Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    fun chargeCard(card: Card, price: Int){
        val charge = Charge()

        charge.card = card
        charge.amount = (price * 100)
        charge.email = FirebaseAuth.getInstance().currentUser?.email

        progressBar?.visibility = View.VISIBLE

        PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction?) {
                verifyPayment(transaction?.reference!!, price)
            }

            override fun beforeValidate(transaction: Transaction?) {
            }

            override fun onError(error: Throwable?, transaction: Transaction?) {
                progressBar?.visibility = View.GONE
                infoText?.text = "Payment Failed"
                infoText?.visibility = View.VISIBLE
                setDailogButtons()

            }

        })

    }

    fun verifyPayment(ref: String, price: Int){
        val amount = (price * 100).toString()
        val stringRequest = object : StringRequest(Request.Method.POST, VERIFY_PAYMENT_URL, Response.Listener { s ->
            // Your success code here
            if (s == "OK"){
                progressBar?.visibility = View.GONE
                infoText?.text = "Payment Successful"
                infoText?.visibility = View.VISIBLE
                setDailogButtons()
                PlaceOrdersTask(cartItems!!, mUser!!, ref, this).execute()
            }
        }, Response.ErrorListener { e ->
            // Your error code here
            progressBar?.visibility = View.GONE
            infoText?.text = "Cant verify payment"
            infoText?.visibility = View.VISIBLE
            setDailogButtons()
        }) {
            override fun getParams(): Map<String, String> = mapOf("code" to ref, "amount" to amount)
        }
        queue?.add(stringRequest)
    }


    fun setDailogButtons(){
        alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.visibility = View.GONE
        alertDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.text = "Dismiss"
    }


    fun getUser(num: Int) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid)
        userRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                mUser = p0?.getValue(User::class.java)
                if (mUser?.coupons != null){
                    price = (num * 0.95).toInt()
                    cartDiscountedTotal.text = "Discounted Total: NGN"+price.toString()+".00"
                    cartTotal.text = "Total: NGN"+num+".00"
                    cartTotal.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
                }else{
                    price = num
                    cartDiscountedTotal?.visibility = View.GONE
                    cartTotal.text = "Total: NGN"+num+".00"
                }
            }

        })
    }
}
