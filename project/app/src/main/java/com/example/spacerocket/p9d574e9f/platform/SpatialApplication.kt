package com.example.spacerocket.p9d574e9f.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.spacerocket.p9d574e9f.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
