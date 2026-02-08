package com.nexa.awesome.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
//import com.nexa.awesome.core.system.api.MetaActivationManager

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base!!
        AppManager.doAttachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
    
        try {
           // MetaActivationManager.activateSdk("720H-MAMAOWNER")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    
        AppManager.doOnCreate()
    }
}