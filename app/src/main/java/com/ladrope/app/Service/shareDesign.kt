package com.ladrope.app.Service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by USER on 2/20/18.
 */

fun getLocalBitmapUri(link: String, context: Context): Uri? {
    val imageView = ImageView(context)

    Picasso.with(context).load(link).into(imageView)
    // Extract Bitmap from ImageView drawable
    val drawable = imageView.getDrawable()

    var bmp: Bitmap? = null

    if (drawable is BitmapDrawable) {
        bmp = (imageView.getDrawable() as BitmapDrawable).bitmap
    } else {
        return null
    }
    // Store image to default external storage directory
    var bmpUri: Uri? = null
    try {
        val file = File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png")
        file.getParentFile().mkdirs()
        val out = FileOutputStream(file)
        bmp!!.compress(Bitmap.CompressFormat.PNG, 90, out)
        out.close()

        if(Build.VERSION.SDK_INT <= 24){
            bmpUri = Uri.fromFile(file)
        }else{
            bmpUri = FileProvider.getUriForFile(context, "com.ladrope.app.provider", file)
        }

    } catch (e: IOException) {
        e.printStackTrace()
    }

    return bmpUri
}

fun updateCoupon(){

    var coupon: Long? = null

    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("coupons").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(p0: DataSnapshot?) {
            if (p0!!.exists()){
                coupon = p0.value as Long
                setCouponValue(coupon!!)
            }else{
                setCouponValue(1)
            }
        }

        override fun onCancelled(p0: DatabaseError?) {

        }
    })


}

fun setCouponValue(num: Long){
    val coupon = num + 1
    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().uid).child("coupons").setValue(coupon)
}