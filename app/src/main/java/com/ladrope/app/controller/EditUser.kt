package com.ladrope.app.controller

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_user.*

class EditUser : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var mUser: User? = null
    private var mUnit: String? = null
    val mAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        editUserHeightUnitSpinner.onItemSelectedListener = this

        val units = arrayListOf<String>()
        units.add("Inches")
        units.add("Meters")
        units.add("Feet")
        units.add("Centimeters")

        val dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, units)

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        editUserHeightUnitSpinner.adapter = dataAdapter


        val userDataRef = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.uid)

        val userName = editUserName
        val userEmail = editUserEmail
        val userImage = editUserProfilePics
        val userAddress = editUserAddress
        val userPhone = editUserPhone
        val userHieght = editUserHeight



        userDataRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                mUser = p0?.getValue(User::class.java)

                val height = mUser?.height as HashMap<String, String>
                val heightText = height.get("height")
                val unit = height.get("unit")


                userAddress.setText(mUser?.address)
                userEmail.setText(mUser?.email)
                userPhone.setText(mUser?.phone)
                userName.setText(mUser?.displayName)
                userHieght.setText(heightText)

                Picasso.with(this@EditUser).load(mUser?.photoURL).placeholder(R.drawable.ic_account_box_black_24dp).into(userImage)
            }
        })

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(this,"Please select a unit", Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val unit = p0?.getItemAtPosition(p2).toString()

        when(unit){
            "Inches" -> mUnit = "inc"
            "Meters" -> mUnit = "meters"
            "Feet" -> mUnit = "ft"
            "Centimeters" -> mUnit = "cm"
        }
    }

    fun EditUserSave(view: View){
        if (editUserAddress.text.toString() != ""){
            if(editUserName.text.toString() != "" && editUserPhone.text.toString() != ""){
                if (editUserHeight.text.toString() != ""){
                    val updateObj = HashMap<String, Any?>()
                    updateObj.put("displayName", editUserName.text.toString())
                    updateObj.put("address", editUserAddress.text.toString())
                    val heightUpdate = HashMap<String, String?>()
                    heightUpdate.put("height", editUserHeight.text.toString())
                    heightUpdate.put("unit", mUnit)

                    updateObj.put("height",heightUpdate)
                    updateObj.put("phone", editUserPhone.text.toString())

                    val mDatabase = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.uid)
                    mDatabase!!.updateChildren(updateObj)
                            .addOnCompleteListener { task: Task<Void> ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this,"Updates saved", Toast.LENGTH_SHORT).show()
                                } else {

                                }
                            }
                }else{
                    Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Please enter your name", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"Please enter an address", Toast.LENGTH_SHORT).show()
        }
    }

}
