package com.ladrope.app.controller

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.ladrope.app.Service.createUser
import com.ladrope.app.Service.saveUserPushId
import com.ladrope.app.Utilities.RC_SIGN_IN
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.email.view.*


class Login : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var alertDialog: AlertDialog? = null

    //google sign in client
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mAuth = FirebaseAuth.getInstance()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        progressBar = progressBar2
        progressBar?.visibility = View.GONE

    }

    fun login(view: View){
        val email = loginEmailTxt.text.toString()
        val password = loginPasswordTxt.text.toString()

        if (isValidEmail(email)){
            startLogin(false)
            mAuth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                println("signInWithEmail:success")
                                startLogin(true)
                                goHome()
                                saveUserPushId()
                            } else {
                                // If sign in fails, display a message to the user.
                                println("signInWithEmail:failure")
                                startLogin(true)
                                Log.e("Error", "")
                                Toast.makeText(this@Login, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                //updateUI(null)
                            }

                            // ...
                        }
                    })
        }else{
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
        }

    }


    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun startLogin(status: Boolean){
        val signinBtn = loginLognBtn
        val googleBtn = loginGoogleBtn

        if(!status){
            progressBar?.visibility = View.VISIBLE
        }else {
            progressBar?.visibility = View.GONE
        }

        signinBtn.isClickable = status
        googleBtn.isClickable = status
    }

    private fun goHome(){
        val homeIntent = Intent(this, Home::class.java)
        startActivity(homeIntent)
        finish()
    }

    //google signin code
    fun signInWithGoogle(view: View) {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                println("Google sign in failed")
                // ...
            }

        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        println("firebaseAuthWithGoogle:" + acct.id!!)
        startLogin(false)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        println("signInWithCredential:success")
                        startLogin(true)
                        val currentUser = mAuth?.currentUser
                        val newUser = User(currentUser?.displayName, currentUser?.email, currentUser?.photoUrl.toString(), null,null,"male",null,null,null)
                        createUser(newUser,currentUser?.uid)
                        goHome()
                        saveUserPushId()
                    } else {
                        // If sign in fails, display a message to the user.
                        println("signInWithCredential:failure")
                        startLogin(true)
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                }
    }

    fun forgetPassword(view: View){
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.email, null)
        alertDialog = AlertDialog.Builder(this).create()

        alertDialog?.setCanceledOnTouchOutside(false)
        val emailInput = view.editText
        alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "Reset Password") { dialog, which ->

            }
        alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, which -> dialog.dismiss() }

        alertDialog?.setView(view)
        alertDialog?.show()

        val b = alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        b?.setOnClickListener(object : View.OnClickListener {

            override fun onClick(p0: View?) {
                val email = emailInput.text.toString()
                if(isValidEmail(email)){
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.bringToFront()
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                        Toast.makeText(this@Login, "We have sent you instructions to reset your password", Toast.LENGTH_SHORT).show()
                        progressBar?.visibility = View.GONE
                        alertDialog?.dismiss()
                    }
                            .addOnFailureListener {
                                Toast.makeText(this@Login, "Reset password email not sent. Try again later", Toast.LENGTH_SHORT).show()
                                progressBar?.visibility = View.GONE
                                alertDialog?.dismiss()
                            }

                }else{
                    Toast.makeText(this@Login, "Please enter a valid email address", Toast.LENGTH_SHORT).show()

                }

            }
        })
    }
}
