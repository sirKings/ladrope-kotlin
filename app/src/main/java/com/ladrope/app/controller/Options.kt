package com.ladrope.app.controller

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
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
import com.ladrope.app.Adapters.OptionsAdapter
import com.ladrope.app.Model.Cloth
import com.ladrope.app.Model.Option
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.PlaceOrdersTask
import com.ladrope.app.Utilities.SELECTEDCLOTH
import com.ladrope.app.Utilities.VERIFY_PAYMENT_URL
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.paycard.view.*


class Options : AppCompatActivity() {
    var cloth: Cloth? = null
    var options: ArrayList<Option?>? = null
    var adapter: OptionsAdapter? = null
    var progressBar: ProgressBar? = null
    var infoText: TextView? = null
    var queue: RequestQueue? = null
    var alertDialog: AlertDialog? = null
    var mUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        getUser()

        cloth = SELECTEDCLOTH
        options = ArrayList()
        getOptions(cloth?.clothKey!!)
        queue = Volley.newRequestQueue(this)

        val layoutManager = LinearLayoutManager(this)
        adapter = OptionsAdapter(options,this)

        optionsRV.layoutManager = layoutManager
        optionsRV.adapter = adapter

        PaystackSdk.initialize(getApplicationContext())

    }

    fun getOptions(clothKey: String){
        val databaseRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(clothKey).child("options")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.exists()){
                    for (data in p0.children){
                        val option = data.getValue(Option::class.java)
                        options?.add(option)
                        adapter?.notifyDataSetChanged()
                    }
                }else{
                    optionsCard.visibility = View.GONE
                    noOptionsCard.visibility = View.VISIBLE
                }
            }

        })
    }

    fun OptionAddToCart(view: View){
        cloth?.selectedOption = adapter?.selectedOptions
        val dataRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("cart")

        val cartKey = dataRef.push().key
        cloth?.cartKey = cartKey

        dataRef.child(cartKey).setValue(cloth).addOnCompleteListener {
            Toast.makeText(this, "Cloth has been added to your cart", Toast.LENGTH_SHORT).show()
        }
    }

    fun OptionsPlaceOrder(view: View){
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.paycard, null)
        alertDialog = AlertDialog.Builder(this).create()

        alertDialog?.setCancelable(false)
        view.paycardAmount.text = "NGN"+cloth?.price.toString()+".00"

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
                        chargeCard(card)
                        //placeOrder(cloth!!, "fake string")
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

    fun chargeCard(card: Card){
        val charge = Charge()

        charge.card = card
        charge.amount = cloth!!.price!!.toInt()
        charge.email = FirebaseAuth.getInstance().currentUser?.email

        progressBar?.visibility = View.VISIBLE

        PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction?) {
                verifyPayment(transaction?.reference!!)
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

    fun verifyPayment(ref: String){
        val amount = (cloth?.price!! * 100).toString()
        val stringRequest = object : StringRequest(Request.Method.POST, VERIFY_PAYMENT_URL, Response.Listener { s ->
            // Your success code here
            if (s == "OK"){
                progressBar?.visibility = View.GONE
                infoText?.text = "Payment Successful"
                infoText?.visibility = View.VISIBLE
                setDailogButtons()
                placeOrder(cloth!!, ref)
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


     fun getUser() {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid)
            userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    mUser = p0?.getValue(User::class.java)
                    Log.e("User", mUser?.email)
                }

            })
    }

    fun placeOrder(cloth: Cloth, ref: String){
        val cloths = ArrayList<Cloth>()
        cloth.selectedOption = adapter?.selectedOptions
        cloths.add(cloth)
        PlaceOrdersTask(cloths, mUser!!, ref, this).execute()
    }

}
