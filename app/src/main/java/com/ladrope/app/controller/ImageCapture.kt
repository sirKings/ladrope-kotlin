package com.ladrope.app.controller

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.PointF
import android.hardware.*
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.ladrope.app.Model.Size
import com.ladrope.app.R
import com.ladrope.app.Utilities.Box
import com.ladrope.app.Utilities.InputStreamVolleyRequest
import com.ladrope.app.Utilities.MyGLRenderer
import kotlinx.android.synthetic.main.activity_image_capture.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class ImageCapture : AppCompatActivity(), SensorEventListener {

    private var senSensorManager: SensorManager? = null
    private var senAccelerometer: Sensor? = null
    var angle: String? = null

    private var preview: SurfaceView? = null
    private var previewHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private var inPreview = false
    internal var start = PointF()
    internal var dialog: ProgressDialog? = null
    var requestQueue: RequestQueue? = null
    var box: View? = null

    var image_1: String? = null
    var image_2: String? = null
    var mKey: String? = null

    internal var surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera!!.setPreviewDisplay(previewHolder)
            } catch (t: Throwable) {
                Log.e("Previ",
                        "Exception in setPreviewDisplay()", t)
                Toast.makeText(this@ImageCapture, t.message, Toast.LENGTH_LONG)
                        .show()
            }

        }

        override fun surfaceChanged(holder: SurfaceHolder,
                                    format: Int, width: Int,
                                    height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // no-op
        }
    }


    internal var photoCallback: Camera.PictureCallback = object : Camera.PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            dialog = ProgressDialog.show(this@ImageCapture, "", "Saving Photo")
            object : Thread() {
                override fun run() {
                    try {
                        Thread.sleep(1000)
                    } catch (ex: Exception) {
                    }

                    onPictureTake(data, camera)
                }
            }.start()
        }
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)
        supportActionBar?.hide()

        preview = findViewById(R.id.prevviewLayout) as SurfaceView
        val verticalReminder = verticalUpdate
        verticalReminder.visibility = View.GONE

        previewHolder = preview!!.holder
        previewHolder!!.addCallback(surfaceCallback)
        previewHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        box =  Box(this)
        addContentView(box, FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT))

        senSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        senAccelerometer = senSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        senSensorManager?.registerListener(this@ImageCapture, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL)

        requestQueue = Volley.newRequestQueue(this)

        displayView.visibility = View.GONE

    }


    public override fun onResume() {
        super.onResume()
        camera = Camera.open(0)
        Log.e("OnResume", "Camera started")
        setCameraDisplayOrientation(this,0,camera!!)

        camera!!.stopPreview()
        camera!!.startPreview()
        Log.e("OnResume", "Preview started")

        senSensorManager?.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

    }

    public override fun onStart() {
        super.onStart()

        if (camera == null){
            camera = Camera.open(0)
            Log.e("OnStart", "Camera started")
            setCameraDisplayOrientation(this,0,camera!!)
        }

        if(!inPreview){
            camera!!.startPreview()
            Log.e("OnStart", "Preview started")
        }
    }

    public override fun onPause() {
        if (inPreview) {
            camera!!.stopPreview()
            Log.e("OnPause", "Preview stoped")
        }

        camera!!.release()
        Log.e("OnPause", "Camera Stoped")

        camera = null
        inPreview = false
        super.onPause()
        senSensorManager?.unregisterListener(this)
    }



    fun onPictureTake(data: ByteArray, camera: Camera) {

        val pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE)
        if (pictureFile == null) {
            Log.d("Error", "Error creating media file, check storage permissions: ")
            return
        }


        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
        } catch (e: IOException) {
            Log.d("Error", "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d("Error", "Error accessing file: " + e.message)
        }
        dialog?.dismiss()
        uploadImage(pictureFile)


    }




    fun takePicture(view: View) {
        //Log.e("onBack :", "yes")
        camera!!.takePicture(null, null, photoCallback)
        inPreview = false
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

            if(nZ < -20){
                verticalUpdate.visibility = View.VISIBLE
                captureBtn.isClickable = false
            }else if (nZ > 20){
                verticalUpdate.visibility = View.VISIBLE
                captureBtn.isClickable = false
            }else{
                verticalUpdate.visibility = View.GONE
                captureBtn.isClickable = true
            }
        }

    }

    fun amplify(num: Float): Float{

        return num *20
    }


    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(applicationContext.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), "ModelImage")
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile: File
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg")
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4")
        } else if (type == 98) {
            mediaFile = File(mediaStorageDir.path + File.separator + "model.obj")
        } else{
            return null
        }

        return mediaFile
    }

    fun setCameraDisplayOrientation(activity: Activity,
                                    cameraId:Int, camera:android.hardware.Camera) {
        val info = android.hardware.Camera.CameraInfo()
        android.hardware.Camera.getCameraInfo(cameraId, info)
        val rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation()
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result:Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        }
        else
        { // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        angle = result.toString()


        val params = camera.parameters
        params.jpegQuality = 80
        if (params.flashMode != null){
            params.flashMode = Camera.Parameters.FLASH_MODE_AUTO
        }
        camera.parameters = params
        camera.setDisplayOrientation(result)
    }

    fun Cancel(view: View){
        finish()
    }

    fun uploadImage(file: File): String?{
        val gson = Gson()

        runOnUiThread(object:Runnable {
            override fun run() {
                dialog = ProgressDialog.show(this@ImageCapture, "", "Uploading Photo")
            }
        })

        var conn: HttpURLConnection? = null
        var dos: DataOutputStream? = null
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"
        var bytesRead:Int
        var bytesAvailable:Int
        var bufferSize:Int
        val buffer:ByteArray
        val maxBufferSize = 1 * 1024 * 1024
        val sourceFile = file
        val serverResponseMessage:String?
        var response:String? = null
        if (!sourceFile.isFile())
        {
            dialog?.dismiss()
            runOnUiThread(object:Runnable {
                public override fun run() {
                    Toast.makeText(getApplicationContext(), "File not found !", Toast.LENGTH_LONG).show()
                }
            })
            return "no file"
        }
        else
        {
            try
            {
                val fileInputStream = FileInputStream(sourceFile.getPath())
                val url = URL("http://saia.3dlook.me/api/v1/uploads/")
                conn = url.openConnection() as HttpURLConnection
                conn.setDoInput(true) // Allow Inputs
                conn.setDoOutput(true) // Allow Outputs
                conn.setUseCaches(false) // Don't use a Cached Copy
                conn.setRequestMethod("POST")
                conn.setRequestProperty("Authorization", "APIKey 3b0cb128ff8f55388300de57af9dd7eca45451ae")
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("ENCTYPE", "multipart/form-data")
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary)
                conn.setRequestProperty("image", sourceFile.getName())
                dos = DataOutputStream(conn.getOutputStream())
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes(("Content-Disposition: form-data; name=\"" + "image" + "\";filename="
                        + sourceFile.getName() + lineEnd))
                dos.writeBytes(lineEnd)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }
                dos.writeBytes(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                val serverResponseCode = conn.responseCode
                serverResponseMessage = conn.responseMessage
                Log.e("uploadFile", ("HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode))
                if (serverResponseCode <= 200)
                {

                    response = streamToString(conn.inputStream)

                    runOnUiThread(object:Runnable {
                        override fun run() {
                            Toast.makeText(this@ImageCapture, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show()
                            dialog?.dismiss()
                            camera?.stopPreview()
                            camera?.startPreview()
                        }
                    })

                    val info = gson.fromJson(response, UploadRes::class.java)
                    Log.e("Res", info.name)
                    Log.e("Res", info.status.toString())

                    if (info.status == true){
                        runOnUiThread(object:Runnable {
                            override fun run() {

                                detectImage(info.name!!, mKey)
                                dialog = ProgressDialog.show(this@ImageCapture, "", "Analyzing Photo")
                            }
                        })

                    }else{
                        runOnUiThread(object:Runnable {
                            override fun run() {
                                showAlert("Error", "Upload failed, please try again")
                            }
                        })
                    }
                }else{
                    runOnUiThread(object:Runnable {
                        override fun run() {
                            dialog?.dismiss()
                            camera?.stopPreview()
                            camera?.startPreview()

                            showAlert("Error", "Upload failed, please try again")
                        }
                    })
                }
                fileInputStream.close()
                dos.flush()
                dos.close()
            }
            catch (e:FileNotFoundException) {
                e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
            }
            catch (ex: MalformedURLException) {
                dialog?.dismiss()
                ex.printStackTrace()
                runOnUiThread(object:Runnable {
                    override fun run() {
                        Toast.makeText(this@ImageCapture, "MalformedURLException",
                                Toast.LENGTH_SHORT).show()
                    }
                })
            }
            catch (e:IOException) {
                dialog?.dismiss()
                e.printStackTrace()
                runOnUiThread(object:Runnable {
                    override fun run() {
                        Toast.makeText(this@ImageCapture, "Got Exception, Please try again",
                                Toast.LENGTH_SHORT).show()
                    }
                })
                Log.e("Upload", ("Exception : " + e.message), e)
            }
        }


        //showAlert(info.name!!, info.status.toString())
        return response
    }

    class UploadRes{
        var name: String? = null
        var status: Boolean? = null
    }

    fun streamToString(inputStream: InputStream): String {

        Log.e("trying", "To read input stream")

        val bufferReader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var result = ""

        try {
            do {
                line = bufferReader.readLine()
                if (line != null) {
                    result += line
                }
            } while (line != null)
            inputStream.close()
        } catch (ex: Exception) {

        }

        return result
    }

    fun showAlert(title: String, msg: String){
        val alert = AlertDialog.Builder(this)
        alert.create()
        alert.setTitle(title)
        alert.setMessage(msg)
        alert.setPositiveButton( "Ok") { dialog, which -> dialog.dismiss() }
        alert.show()
    }

    fun detectImage(name: String, key: String?){
        if (key == null){
            image_1 = name
        }else{
            image_2 = name
        }

        //val jsonParams =
        val stringRequest = object : StringRequest(Request.Method.POST, "http://saia.3dlook.me/api/v1/step/", Response.Listener { s ->
            // Your success code here
            val jsonResPonse = JSONObject(s)
            Log.e("Detection Res", jsonResPonse.toString(1))
            dialog?.dismiss()
            val status = jsonResPonse.getBoolean("status")
            if (status){
                val tempkey = jsonResPonse.getString("key")
                mKey = tempkey
                if (key == null){
                    showAlert("Upload successful", "Now capture the side image")
                    stepNotice.text = "Step 2: Side Image"
                }else{
                    getMeasurementDetails()
                }

            }else{
                showAlert("Error", "Could not analyze the image, Please try again")
            }


        }, Response.ErrorListener { e ->
            // Your error code here
            Log.e("Detection Res", e.toString())
            //s.toString(1)
            dialog?.dismiss()
            showAlert("Upload Failed", "Please try again")

        }) {

            override fun getParams(): MutableMap<String, String> {
                Log.e("angle",angle)
                Log.e("name",name)
                val params: MutableMap<String, String>
                 when (key == null){
                    true -> params = mutableMapOf("angle" to angle!!, "step" to "1", "height" to "168", "gender" to "male", "image" to name)
                    false -> params = mutableMapOf("angle" to angle!!, "height" to "168", "key" to key!!, "gender" to "male", "image" to name, "step" to "2")

               }

                return params
            }


            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String,String>()
                headers["Authorization"]  = "APIKey 3b0cb128ff8f55388300de57af9dd7eca45451ae"
                headers.put("Content-Type","application/x-www-form-urlencoded charset=utf-8")
                return headers
            }
        }

        stringRequest.retryPolicy = (DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        requestQueue?.getCache()?.clear()
        requestQueue?.add(stringRequest)

    }

    fun getMeasurementDetails(){
        dialog = ProgressDialog.show(this@ImageCapture, "", "Getting your measurement details")
        val stringRequest = object : StringRequest(Request.Method.POST, "http://saia.3dlook.me/api/v1/complete/", Response.Listener { s ->
            // Your success code here
            val resPonse = JSONObject(s)

            //Log.e("Detection Res", resPonse.toString(1))
            dialog?.dismiss()
            val status = resPonse.getBoolean("status")
            if (status){
                //showAlert("Successful", "Measurement obtained")
                //dialog?.dismiss()
                stepNotice.text = "Complete!"
                createSize(resPonse)
            }else{
                showAlert("Error", "Could not analyze the image, Please try again")
            }


        }, Response.ErrorListener { e ->
            // Your error code here
            Log.e("Detection Res", e.toString())
            //s.toString(1)
            dialog?.dismiss()
            showAlert("Upload Failed", "Please try again")

        }) {

            override fun getParams(): Map<String, String> {

                return   mapOf("angle" to angle!!, "height" to "168", "key" to mKey!!, "gender" to "male", "image_1" to image_1!!, "image_2" to image_2!!)
            }


            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String,String>()
                headers["Authorization"]  = "APIKey 3b0cb128ff8f55388300de57af9dd7eca45451ae"
                headers.put("Content-Type","application/x-www-form-urlencoded charset=utf-8")
                return headers
            }
        }

        stringRequest.retryPolicy = (DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        requestQueue?.cache?.clear()

        requestQueue?.add(stringRequest)
    }

    fun createSize(json: JSONObject){
        dialog = ProgressDialog.show(this@ImageCapture, "", "Saving measurement details")

        val size = Size()

        size.wrist = gSBD(json.getJSONObject("volume_parameters").getString("wrist"))
        size.waist = json.getJSONObject("volume_parameters").getDouble("waist").toInt().toString()
        size.thigh = gSBD(json.getJSONObject("volume_parameters").getString("thigh"))
        size.sleeve = aNTV(json.getJSONObject("front_parameters").getString("sleeve_length"), 5.0)
        size.shoulder = gSBD(json.getJSONObject("front_parameters").getDouble("shoulders").toString())
        size.neck = gSBD(json.getJSONObject("volume_parameters").getString("neck"))
        size.hips = gSBD(json.getJSONObject("volume_parameters").getDouble("hips").toString())
        size.fullback = gSBD(json.getJSONObject("front_parameters").getDouble("jacket_lenght").toString())
        size.chest = gSBD(json.getJSONObject("volume_parameters").getString("chest_v2"))
        size.bicep = gSBD(json.getJSONObject("volume_parameters").getString("biceps"))
        size.belly = json.getJSONObject("volume_parameters").getDouble("waist").toInt().toString()
        size.ankle = gSBD(json.getJSONObject("volume_parameters").getString("ankle"))
        size.trouserLength = aNTV(json.getDouble("legs_height").toString(), 5.0)
        size.unit = "cm"
        size.model = json.getJSONObject("volume_parameters").getJSONObject("test_data").getString("model_url")

        FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(FirebaseAuth.getInstance().uid)
                .child("size")
                .setValue(size)
                .addOnCompleteListener {
                    Toast.makeText(this@ImageCapture, "Size saved successfully", Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                    getAvartar(size.model!!)
                }.addOnFailureListener {
                    Toast.makeText(this@ImageCapture, "Could not save size, Please try again", Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                    mKey = null
                    stepNotice.text = "Step 1: Front Body"
                }
    }

    //get string before decimal point
    fun gSBD(str: String): String{
        return str.toDouble().toInt().toString()
    }
    //add value to string
    fun aNTV(str: String, num: Double): String{
        return (str.toDouble() + num).toInt().toString()
    }

    fun getAvartar(url: String){
        camera?.stopPreview()
        camera?.release()
        camera = null
        box?.visibility = View.GONE

        Log.e("Avatar", "Starting to get avatar")
        dialog = ProgressDialog.show(this@ImageCapture, "", "Building your 3D model")

        captureScreen.visibility = View.GONE
        displayView.visibility = View.VISIBLE
        gl_view.setRenderer(MyGLRenderer())

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.getDeviceConfigurationInfo()
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if(supportsEs2){
            //gl_view.setEGLContextClientVersion(2)
            dialog?.dismiss()

            downloadModel(url)


        }else{
            showAlert("Successful", "Your size data has been save. We could not load your avata because you phone cant support that"
                    )
        }


    }

    fun downloadModel(url: String){
        dialog = ProgressDialog.show(this@ImageCapture, "", "Downloading your 3D model")

            val request = InputStreamVolleyRequest(Request.Method.GET, url,
                    object:Response.Listener<ByteArray> {
                     override   fun onResponse(response:ByteArray) {
                            dialog?.dismiss()

                            try
                            {
                                if (response != null)
                                {
                                    val name = "File.obj"
                                    val dir = File(applicationContext.filesDir.getAbsolutePath() + "/directory1/directory2/")
                                    dir.mkdirs()
                                    val videoFile = File(dir.absolutePath + name)
                                    val stream = FileOutputStream(videoFile)
                                    try
                                    {
                                        stream.write(response)
                                    }
                                    finally
                                    {
                                        stream.close()
                                    }
                                    Toast.makeText(this@ImageCapture, "Download complete.", Toast.LENGTH_LONG).show()
                                    renderFile(videoFile)
                                }
                            }
                            catch (e:Exception) {

                                Log.d("ERROR!!", "NOT DOWNLOADED")
                                e.printStackTrace()
                            }
                        }
                    }, object:Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {

                    error.printStackTrace()
                    dialog?.dismiss()
                }
            }, null)
            val mRequestQueue = Volley.newRequestQueue(getApplicationContext(), HurlStack())
            mRequestQueue.add(request)

    }

    fun renderFile(model: File){

    }

}
