package com.ladrope.app.Utilities

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        p0?.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
    }


    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
        gl.glViewport(0, 0, w, h)
    }

    override fun onDrawFrame(gl: GL10) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
    }
}