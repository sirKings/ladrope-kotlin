package com.ladrope.app.controller

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by USER on 2/10/18.
 */
class App : Application() {

    var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate() {
        super.onCreate()


        // Cloudinary Initialization
        MediaManager.init(this)

        //Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

    }



}