package com.spa.management

import android.app.Application
import com.spa.management.data.api.SessionManager

class SpaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
