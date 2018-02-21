package com.ladrope.app.Service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService



/**
 * Created by USER on 2/9/18.
 */
class MyFirebaseInstanceIDService: FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.e("serviceToken", refreshedToken)
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid)
        userRef.child("idToken").setValue(refreshedToken)
    }
}