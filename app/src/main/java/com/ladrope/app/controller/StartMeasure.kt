package com.ladrope.app.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.PendingOrder
import com.ladrope.app.R
import com.ladrope.app.Utilities.PERMISSION_REQUEST_CODE
import com.ladrope.app.Utilities.REQUEST_VIDEO_CAPTURE
import com.ladrope.app.Utilities.SubmitOrdersTask
import kotlinx.android.synthetic.main.activity_start_measure.*
import java.io.File


class StartMeasure : AppCompatActivity(), SensorEventListener {


    private var senSensorManager: SensorManager? = null
    private var senAccelerometer: Sensor? = null
    private var mVideoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_measure)


        senSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        senAccelerometer = senSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        senSensorManager?.registerListener(this@StartMeasure, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL)

        startVideo.visibility = View.GONE
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val mySensor = p0?.sensor

        if (mySensor?.type == Sensor.TYPE_ACCELEROMETER){
            val z = p0.values[2]
            //Log.e("sensor", z.toString())

            val nZ = amplify(z)
            //Log.e("amplify", nZ.toString())

            arrowIndicator.setPadding(0,nZ.toInt(),0,0)

            if(nZ > 40){
                arrowIndicator.setColorFilter(Color.RED)
                startVideo.visibility = View.GONE
            }else{
                arrowIndicator.setColorFilter(resources.getColor(R.color.colorPrimary))
                startVideo.visibility = View.VISIBLE
            }
        }

    }

    fun amplify(num: Float): Float{

        return num *20
    }

    override fun onPause() {
        super.onPause()
        senSensorManager?.unregisterListener(this)

    }

    override fun onResume() {
        super.onResume()
        senSensorManager?.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun StartVideo(view: View){
        if(!checkPermission()){
            requestPermission()
        }

        mVideoUri = Uri.parse(Environment.getExternalStorageDirectory().absolutePath + "measurement.mp4")

        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideoIntent.putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, 15)
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (5*1048*1048).toString() +"L")
        takeVideoIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
        takeVideoIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        takeVideoIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mVideoUri)
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null && checkPermission()) {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
            mVideoUri = data!!.data
            Log.e("Video", mVideoUri.toString())
            postVideoLayout.visibility = View.VISIBLE
            Log.e("videoView", "shown")
            val mc = MediaController(this)
            mc.setAnchorView(videoView)
            videoView.setMediaController(mc)
            videoView.setVideoURI(mVideoUri)
            videoView.start()
            senSensorManager?.unregisterListener(this)
            preVideoLayout.visibility = View.GONE

        }
    }

    fun SubmitVideo(view: View){
        UpLoadVideo(mVideoUri!!)
    }

    fun RetakeVideo(view: View){
        postVideoLayout.visibility = View.GONE
        preVideoLayout.visibility = View.VISIBLE
        senSensorManager?.registerListener(this,senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

    }

    fun UpLoadVideo(uri: Uri){
        //val imageName = getName(uri)

        class callback: UploadCallback {
            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                Log.e("Service Upload", "Rescheduled")
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                Log.e("Service Upload", "Error")
            }

            override fun onStart(requestId: String?) {
                Log.e("Service Upload", "Started")
                Toast.makeText(applicationContext, "Video submission started", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                Log.e("Service Upload", resultData.toString())
                Toast.makeText(applicationContext, "Video submitted", Toast.LENGTH_SHORT).show()
                checkAndSubmitOrder()

                val fdelete = File(uri.path)
                    fdelete.delete()
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + uri.getPath())
                    } else {
                        System.out.println("file not Deleted :" + uri.getPath())
                    }
                }

            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
            }
        }

        var requestid = MediaManager.get().upload(uri).unsigned("hsivazse").option("public_id", FirebaseAuth.getInstance().uid).option("resource_type", "auto").callback(callback()).dispatch()

    }



    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {

                val readAccessGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (readAccessGranted)

                else {

                    Toast.makeText(this, "Ladrope needs permission to record video for your measurement analysis", Toast.LENGTH_LONG).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            showMessageOKCancel("Ladrope needs permission to record video for your measurement analysis",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                                                    PERMISSION_REQUEST_CODE)
                                        }
                                    })
                            return
                        }
                    }

                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {

        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    fun checkAndSubmitOrder(){

        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("savedOrders")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                val savedOrders = ArrayList<PendingOrder>()
                if (p0!!.exists()){

                    for (item in p0.children){
                        val order = item.getValue(PendingOrder::class.java)
                        savedOrders.add(order!!)
                    }
                    SubmitOrdersTask(savedOrders, this@StartMeasure)
                }

            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }


}
