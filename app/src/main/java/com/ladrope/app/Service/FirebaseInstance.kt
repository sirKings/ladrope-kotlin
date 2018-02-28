package com.ladrope.app.Service

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.ladrope.app.Utilities.PUSHID


/**
 * Created by USER on 2/9/18.
 */
class MyFirebaseInstanceIDService: FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.e("serviceToken", refreshedToken)

        PUSHID = refreshedToken

    }
}

