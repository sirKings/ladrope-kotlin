package com.ladrope.app.Utilities

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by USER on 2/5/18.
 */
fun formatDate(str: String): String{
    //val dateStr = "Mon Jun 18 00:00:00 IST 2012"
    var pattern = ""
    if(str.length < 26){
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    } else {
        pattern = "E MMM dd yyyy HH:mm:ss Z"
    }

    val format = SimpleDateFormat(pattern, Locale.UK)
    val date = format.parse(str) as Date
    System.out.println(date)

    val cal = Calendar.getInstance()
    cal.time = date
    val formatedDate = cal.get(Calendar.DATE).toString() + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR)
    println("formatedDate : " + formatedDate)
    return formatedDate
}