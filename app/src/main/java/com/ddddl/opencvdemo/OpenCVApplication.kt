package com.ddddl.opencvdemo

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class OpenCVApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(applicationContext)
    }
}