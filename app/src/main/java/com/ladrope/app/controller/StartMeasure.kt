package com.ladrope.app.controller

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.ladrope.app.R
import com.ladrope.app.Service.CameraPreview
import java.io.File
import java.io.IOException






class StartMeasure : AppCompatActivity(), SensorEventListener {


    private var senSensorManager: SensorManager? = null
    private var senAccelerometer: Sensor? = null
    private var mPreview: CameraPreview? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mCamera: Camera? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_measure)


        senSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        senAccelerometer = senSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        senSensorManager?.registerListener(this@StartMeasure, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val mySensor = p0?.sensor

        if (mySensor?.type == Sensor.TYPE_ACCELEROMETER){
            val z = p0.values[2]
            //Log.e("sensor", z.toString())
        }

    }

    override fun onPause() {
        super.onPause()
        senSensorManager?.unregisterListener(this)
        releaseMediaRecorder()       // if you are using MediaRecorder, release it first
        releaseCamera()              // release the camera immediately on pause event
    }

    override fun onResume() {
        super.onResume()
        senSensorManager?.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun StartVideo(view: View){
        if(checkCameraHardware(this)){

            prepareVideoRecorder()
            Toast.makeText(this, "Camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else{
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        }
    }

    fun SubmitVideo(view: View){

    }

    private fun checkCameraHardware(context: Context): Boolean {
        return (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
    }

    fun getCameraInstance(): Camera? {
        var c: Camera? = null
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT) // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Toast.makeText(this, "No front camera on this device", Toast.LENGTH_LONG)
                    .show()
        }

        return c // returns null if camera is unavailable
    }



    private fun prepareVideoRecorder(): Boolean? {

        mCamera = getCameraInstance()

        Log.e("Camera",mCamera.toString())

        if(mCamera == null){
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show()
            return null
        }


        mPreview = CameraPreview(this, mCamera!!)
        val preview = findViewById<View>(R.id.camera_preview) as FrameLayout
        preview.addView(mPreview)

        mMediaRecorder = MediaRecorder()

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera?.unlock()
        mMediaRecorder?.setCamera(mCamera)

        // Step 2: Set sources
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder?.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW))

        // Step 4: Set output file
        mMediaRecorder?.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())

        // Step 5: Set the preview output
        mMediaRecorder?.setPreviewDisplay(mPreview?.holder?.surface)

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder?.prepare()
        } catch (e: IllegalStateException) {
            Log.d("mRecoder", "IllegalStateException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (e: IOException) {
            Log.d("mRecorder", "IOException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        }

        return true
    }


    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder?.reset()   // clear recorder configuration
            mMediaRecorder?.release() // release the recorder object
            mMediaRecorder = null
            mCamera?.lock()           // lock camera for later use
        }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera?.release()        // release the camera for other applications
            mCamera = null
        }
    }

    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(applicationContext.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), "MyCameraApp")
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory")
                return null
            }
        }

        // Create a media file name
        val timeStamp = System.currentTimeMillis()
        val mediaFile: File
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg")
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4")
        } else {
            return null
        }

        return mediaFile
    }

    fun RecordVideo(view: View){
        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder?.stop()  // stop the recording
            releaseMediaRecorder() // release the MediaRecorder object
            mCamera?.lock()         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            //setCaptureButtonText("Capture");
            isRecording = false
        } else {
            // initialize video camera
            if (prepareVideoRecorder() == true) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder?.start()

                // inform the user that recording has started
                //setCaptureButtonText("Stop");
                isRecording = true
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder()
                // inform user
            }
        }
    }


}
