package com.ladrope.app.Service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ladrope.app.R
import com.ladrope.app.Utilities.ADMIN_CHANNEL_ID
import com.ladrope.app.controller.MainActivity
import java.net.URL
import java.util.*


/**
 * Created by USER on 2/9/18.
 */
class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("msg", remoteMessage.data.toString())

        val bitmap = getBitmapfromUrl(remoteMessage.getData().get("image-url"))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            setupChannels()
//        }
        var notificationIntent: Intent? = null

        notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.putExtra("clothKey", remoteMessage.data.get("clothKey"))
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(applicationContext,0, notificationIntent, PendingIntent.FLAG_ONE_SHOT)
        val notificationId = Random().nextInt(60000)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, ADMIN_CHANNEL_ID)
        notificationBuilder.setContentTitle(remoteMessage.data.get("title"))
        notificationBuilder.setContentText(remoteMessage.data.get("message"))
        notificationBuilder.setLargeIcon(bitmap)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setSound(defaultSoundUri)
        notificationBuilder.setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())


//        fun setupChannels() {
//            val adminChannelName = getString(R.string.notifications_admin_channel_name)
//            val adminChannelDescription = getString(R.string.notifications_admin_channel_description)
//            val adminChannel: NotificationChannel
//            adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW)
//            adminChannel.setDescription(adminChannelDescription)
//            adminChannel.enableLights(true)
//            adminChannel.setLightColor(Color.RED)
//            adminChannel.enableVibration(true)
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(adminChannel)
//            }
//        }
    }

    fun getBitmapfromUrl(url: String?): Bitmap?{

        try {
            val url = URL(url)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        }catch (e: Exception){
            e.printStackTrace()
            return null
        }

    }



}