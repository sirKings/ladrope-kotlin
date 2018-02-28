package com.ladrope.app.Service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Utilities.PUSHID

/**
 * Created by USER on 1/31/18.
 */
val database = FirebaseDatabase.getInstance()
val userRef = database.getReference("users")

fun createUser(user: com.ladrope.app.Model.User, uid: String?){

    userRef.addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.child(uid).exists())
            {
                println("User already exists")
            }
            else
            {
                userRef.child(uid).setValue(user)
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            println("An error occurred")
        }
    })

}

fun saveUserPushId(){
    if(PUSHID != null){
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid)
        userRef.child("idToken").setValue(PUSHID)
    }

}