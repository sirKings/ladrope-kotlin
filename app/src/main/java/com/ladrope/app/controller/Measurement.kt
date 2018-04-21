package com.ladrope.app.controller

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.ladrope.app.R

class Measurement : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurement)
    }

    fun measurementUsePhone(view: View){
        val startCame = Intent(this, ImageCapture::class.java)
        startActivity(startCame)

//        val usePhoneIntent = Intent(this, UsePhone::class.java)
//        startActivity(usePhoneIntent)
    }

    fun measurementEnterMeasurement(view: View){
        val enterMeasurement = Intent(this, EnterMeasurement::class.java)
        startActivity(enterMeasurement)
    }
}
