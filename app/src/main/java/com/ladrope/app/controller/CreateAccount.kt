package com.ladrope.app.controller

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.ladrope.app.Service.createUser
import com.ladrope.app.Service.saveUserPushId
import com.ladrope.app.Utilities.RC_SIGN_UP
import kotlinx.android.synthetic.main.activity_create_account.*
import kotlinx.android.synthetic.main.activity_login.*

class CreateAccount : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null

    //google sign in client
    private var mGoogleSignInClient: GoogleSignInClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mAuth = FirebaseAuth.getInstance()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        progressBar = progressBar2
        progressBar?.visibility = View.GONE
        supportActionBar?.hide()
    }



    fun createAccount(view: View){
        val email = createAccountEmailTxt.text.toString()
        val password = createAccountPassTxt.text.toString()
        val name = createAccountNameTxt.text.toString()
        val password2 = createAccountPassMatchTxt.text.toString()

        if (isValidEmail(email)){
            if (password.length > 5){
                if (password == password2){
                    startLogin(false)
                    mAuth?.createUserWithEmailAndPassword(email, password)
                            ?.addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                                override fun onComplete(task: Task<AuthResult>) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        println("signUpWithEmail:success")
                                        val currentUser = mAuth?.currentUser
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setDisplayName(name).build()
                                        currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                            val newUser = User(currentUser?.displayName!!, currentUser.email!!, "default", null,null,null,null,null,null)
                                            createUser(newUser,currentUser.uid)
                                        }
                                        startLogin(true)
                                        goHome()
                                        saveUserPushId()
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        println("signInWithEmail:failure")
                                        startLogin(true)
                                        Log.e("Error", "")
                                        Toast.makeText(this@CreateAccount, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show()
                                        //updateUI(null)
                                    }

                                    // ...
                                }
                            })
                }else{
                    Toast.makeText(this, "Please your password must match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Please your must be upto six characters long", Toast.LENGTH_SHORT).show()
            }

        }else{
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
        }

    }


    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun startLogin(status: Boolean){
        val createAcctBtn = createAccountSignupBtn
        val googleBtn = createAccountGoogleBtn

        if(!status){
            progressBar?.visibility = View.VISIBLE
        }else {
            progressBar?.visibility = View.GONE
        }

        createAcctBtn.isClickable = status
        googleBtn.isClickable = status
    }

    private fun goHome(){
        val homeIntent = Intent(this, Home::class.java)
        startActivity(homeIntent)
        finish()
    }

    //google signin code
    fun createAccountWithGoogle(view: View) {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_UP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_UP) {
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
                        val newUser = User(currentUser?.displayName, currentUser?.email, currentUser?.photoUrl.toString(), null,null,null,null,null,null)
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
}
