package com.ladrope.app.controller

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.ladrope.app.R

class MainActivity : AppCompatActivity() {

    var clothKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val mAuth = FirebaseAuth.getInstance()

        try{
            clothKey = intent.extras.get("clothKey") as String
        }catch (e: Exception){
            e.printStackTrace()
        }

        if (mAuth.currentUser?.uid != null){

            if (clothKey != null){
                val clothIntent = Intent(this, ClothActivity::class.java)
                clothIntent.putExtra("clothKey", clothKey)
                startActivity(clothIntent)
            }else{
                val homeIntent = Intent(this, Home::class.java)
                startActivity(homeIntent)
                finish()
            }

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
