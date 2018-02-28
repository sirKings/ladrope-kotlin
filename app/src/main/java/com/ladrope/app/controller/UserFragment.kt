package com.ladrope.app.controller


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user.view.*


/**
 * A simple [Fragment] subclass.
 */
class UserFragment : Fragment() {

    private var mUser: User? = null
    private var mErrorText: TextView? = null
    private var mUserStory: CardView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        val mAuth = FirebaseAuth.getInstance()
        val userDataRef = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.uid)

        val userName = view.userTabDisplayName
        val userEmail = view.userTabEmail
        val userImage = view.userTabProfilePicture
        val userAddress = view.userTabAddress
        val userPhone = view.userTabPhone
        val userHieght = view.userTabHieght
        val coupon = view.userTabUserCoupon
        mErrorText = view.userErrorText
        mUserStory = view.userStory



        userDataRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

                mErrorText?.visibility = View.VISIBLE
                mUserStory?.visibility = View.GONE
            }

            override fun onDataChange(p0: DataSnapshot?) {
                     mUser = p0?.getValue(User::class.java)
                var couponText = ""

                if (mUser?.coupons == null){
                    couponText = "Coupons: 0"
                }else{
                    couponText = "Coupons: "+ mUser?.coupons.toString()
                }

                var heightText = ""

                if (mUser?.height == null){

                }else{
                    val height = mUser?.height as HashMap<String, String>
                    heightText = height.get("height") + " "+height.get("unit")
                }

                userAddress.text = mUser?.address
                userEmail.text = mUser?.email
                userPhone.text = mUser?.phone
                userName.text = mUser?.displayName
                userHieght.text = heightText
                coupon.text = couponText
                Picasso.with(context).load(mUser?.photoURL).placeholder(R.drawable.placeholder).into(userImage)
            }
        })

        view.userTabLogout.setOnClickListener {
            if (mAuth != null){
                mAuth.signOut()
                val mainIntent = Intent(context, MainActivity::class.java)
                startActivity(mainIntent)
                activity?.finish()
            }
        }

        view.UserTabEditBtn.setOnClickListener {
            val editIntent = Intent(context, EditUser::class.java)
            startActivity(editIntent)
        }

        view.userTabMeasure.setOnClickListener {
            val measureIntent = Intent(context, Measurement::class.java)
            startActivity(measureIntent)
        }

        return view

    }

}// Required empty public constructor
