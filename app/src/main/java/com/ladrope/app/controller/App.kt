package com.ladrope.app.controller

import android.app.Application
import com.cloudinary.android.MediaManager

/**
 * Created by USER on 2/10/18.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()


        // Cloudinary Initialization
        MediaManager.init(this)

        //Volley request queue

    }



}