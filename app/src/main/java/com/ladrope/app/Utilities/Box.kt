package com.ladrope.app.Utilities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.ladrope.app.R


class Box internal constructor(context: Context) : View(context) {
    private val paint = Paint()

    override fun onDraw(canvas: Canvas) { // Override the onDraw() Method
        super.onDraw(canvas)

        paint.setStyle(Paint.Style.STROKE)
        paint.setColor(resources.getColor(R.color.colorPrimaryDark))
        paint.setStrokeWidth(10F)

        //center
        val x0 = canvas.width / 2F
        val y0 = canvas.height / 2F
        val dx = canvas.width / 3F
        val dy = canvas.height / 3F
        //draw guide box
        canvas.drawRect(x0 - dx, y0 - (dy + 50F), x0 + dx, y0 + dy - 50F, paint)
    }
}