package com.ladrope.app.controller

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.ladrope.app.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser?.uid != null){
            val homeIntent = Intent(this, Home::class.java)
            startActivity(homeIntent)
            finish()
        }
    }

    fun login(view: View){
        val loginIntent = Intent(this, Login::class.java)
        startActivity(loginIntent)
    }

    fun createAccount(view: View){
        val createAccountIntent = Intent(this, CreateAccount::class.java)
        startActivity(createAccountIntent)
    }
}
