package com.ladrope.app.Utilities

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by USER on 2/5/18.
 */
fun formatDate(str: Any): String{
    //val dateStr = "Mon Jun 18 00:00:00 IST 2012"
    //Sat Feb 17 09:57:06 GMT+00:00 2018
    //Thu Feb 15 09:57:06 GMT+00:00 2018
    if (str is String){
        var pattern = ""
        if (str.length == 25) {
            pattern = "yyyy-MM-dd HH:mm:ss Z"
        }
        else if(str.length < 26) {
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        } else{
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
    }else {
        val date = str as HashMap<String, Any>

        val day = date.get("date")
        val month = date.get("month").toString().toInt() + 1
        val yr = date.get("year").toString()
        var year = ""
        if (yr.length > 2){
            year = yr.substring(yr.length - 2)
        }else{
            year = yr
        }

        return day.toString() + "/"+ month.toString() + "/"+ year
    }

}