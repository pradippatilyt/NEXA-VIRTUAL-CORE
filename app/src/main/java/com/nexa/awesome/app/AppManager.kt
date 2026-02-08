package com.nexa.awesome.app

import android.content.Context
import android.content.SharedPreferences
import com.nexa.awesome.view.main.BlackBoxLoader


object AppManager {
    @JvmStatic
    val mBlackBoxLoader by lazy {
        BlackBoxLoader()
    }

    @JvmStatic
    val mRemarkSharedPreferences: SharedPreferences by lazy {
        App.getContext().getSharedPreferences("UserRemark", Context.MODE_PRIVATE)
    }

    fun doAttachBaseContext(context: Context) {
        try {
            mBlackBoxLoader.attachBaseContext(context)
            mBlackBoxLoader.addLifecycleCallback()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doOnCreate() {
        mBlackBoxLoader.doOnCreate()
        initThirdService()
    }

    private fun initThirdService() { }
}
