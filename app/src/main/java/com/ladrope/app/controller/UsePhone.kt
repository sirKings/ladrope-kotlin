package com.ladrope.app.controller

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.MediaController
import com.ladrope.app.R
import com.ladrope.app.Service.NotificationPublisher
import kotlinx.android.synthetic.main.activity_use_phone.*


class UsePhone : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_phone)

        measurementVideo.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.measurement)
        measurementVideo.start()

        val mediaController = MediaController(this)
        mediaController.setAnchorView(measurementVideo)

        measurementVideo.setMediaController(mediaController)
    }

    fun RemindLater(view: View){
        val builderSingle = AlertDialog.Builder(this@UsePhone)
        builderSingle.setIcon(R.drawable.logo)
        builderSingle.setTitle("When")

        val arrayAdapter = ArrayAdapter<String>(this@UsePhone, android.R.layout.select_dialog_singlechoice)
        arrayAdapter.add("One hour time")
        arrayAdapter.add("Two hours time")
        arrayAdapter.add("Three hours time")
        arrayAdapter.add("Six hours time")
        arrayAdapter.add("Tomorrow")

        builderSingle.setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        builderSingle.setAdapter(arrayAdapter, DialogInterface.OnClickListener { dialog, which ->
            val strName = arrayAdapter.getItem(which)
            setTime(strName)
            val builderInner = AlertDialog.Builder(this@UsePhone)
            builderInner.setMessage(strName)
            builderInner.setTitle("We will remind you by")
            builderInner.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            builderInner.show()
        })
        builderSingle.show()
    }

    fun MeasureNow(view: View){
        val startMeasureIntent = Intent(this, StartMeasure::class.java)
        startActivity(startMeasureIntent)
    }

    fun setTime(time: String){
        var delay = 0

        when (time){
            "One hour time" -> delay = 60*60*60
            "Two hours time" -> delay = 2*60*60*60
            "Three hours time" -> delay = 3*60*60*60
            "Six hours time" -> delay = 6*60*60*60
            "Tomorrow" -> delay = 24*60*60*60
        }

        scheduleNotification(getNotification("Time to make your measurement video"), delay)
    }

    private fun scheduleNotification(notification: Notification, delay: Int) {

        val notificationIntent = Intent(this, NotificationPublisher::class.java)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val futureInMillis = SystemClock.elapsedRealtime() + delay
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
    }

    private fun getNotification(content: String): Notification {
        val contentIntent = PendingIntent.getActivity(this, 0,
                Intent(this, UsePhone::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(this)
        builder.setContentTitle(content)
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground)
        builder.setContentIntent(contentIntent)
        return builder.build()
    }
}
