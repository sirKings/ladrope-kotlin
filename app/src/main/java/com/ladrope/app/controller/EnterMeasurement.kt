package com.ladrope.app.controller

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.R
import kotlinx.android.synthetic.main.activity_enter_measurement.*

class EnterMeasurement : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    private var mUnit: String? = null
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_measurement)

        enterMeasurementSpinner.onItemSelectedListener = this

        val units = arrayListOf<String>()
        units.add("Inches")
        units.add("Meters")
        units.add("Feet")
        units.add("Centimeters")

        val dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, units)

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        enterMeasurementSpinner.adapter = dataAdapter




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

    fun enterMeasurementSave(view: View){

        if (mUnit == null){
            Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show()
        }else{
            val userSizeRef = FirebaseDatabase.getInstance().reference.child("users").child(mAuth.uid).child("size")

            val size = HashMap<String, String>()

            size.put("ankle", ankleTxt.text.toString())
            size.put("belly", bellyTxt.text.toString())
            size.put("bicep", bicepTxt.text.toString())
            size.put("chest", chestTxt.text.toString())
            size.put("fullback",backTxt.text.toString())
            size.put("head", headTxt.text.toString())
            size.put("hips",hipTxt.text.toString())
            size.put("neck", neckTxt.text.toString())
            size.put("shoulder", shoulderTxt.text.toString())
            size.put("sleeve", sleeveTxt.text.toString())
            size.put("thigh",thighTxt.text.toString())
            size.put("trouserLength",trouserLength.text.toString())
            size.put("unit", mUnit!!)
            size.put("waist", waistTxt.text.toString())
            size.put("wrist", wristTxt.text.toString())

            userSizeRef.setValue(size).addOnCompleteListener {
                Toast.makeText(this,"Your measurements have been saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
