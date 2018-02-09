package com.ladrope.app.Utilities

import android.content.Context
import android.os.AsyncTask
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

/**
 * Created by USER on 2/9/18.
 */

class ReportCommentTask constructor(private val clothKey: String, private val uid: String, private val context: Context) : AsyncTask<String?, String?, String?>() {
    override fun doInBackground(vararg str: String?): String? {

        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Request.Method.POST, COMMENT_REPORT_URL, Response.Listener { s ->
            // Your success code here

        }, Response.ErrorListener { e ->
            // Your error code here

        }) {
            override fun getParams(): Map<String, String> = mapOf("clothKey" to clothKey, "uid" to uid)
        }
        queue?.add(stringRequest)
        return null
    }
}