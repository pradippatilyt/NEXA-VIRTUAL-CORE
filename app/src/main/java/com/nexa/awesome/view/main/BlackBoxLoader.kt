package com.nexa.awesome.view.main

import android.app.Application
import android.content.Context
import com.nexa.awesome.NexaCore
import com.nexa.awesome.app.BActivityThread.getUserId
import com.nexa.awesome.app.configuration.AppLifecycleCallback
import com.nexa.awesome.app.configuration.ClientConfiguration
import com.nexa.awesome.utils.Slog
import com.nexa.awesome.app.App
import com.nexa.awesome.biz.cache.AppSharedPreferenceDelegate
import java.io.File

class BlackBoxLoader {
    private var mHideRoot by AppSharedPreferenceDelegate(App.getContext(), false)
    private var mHideXposed by AppSharedPreferenceDelegate(App.getContext(), false)
    private var mDaemonEnable by AppSharedPreferenceDelegate(App.getContext(), false)
    private var mShowShortcutPermissionDialog by AppSharedPreferenceDelegate(App.getContext(), true)

    fun hideRoot(): Boolean {
        return mHideRoot
    }

    fun invalidHideRoot(hideRoot: Boolean) {
        this.mHideRoot = hideRoot
    }

    fun hideXposed(): Boolean {
        return mHideXposed
    }

    fun invalidHideXposed(hideXposed: Boolean) {
        this.mHideXposed = hideXposed
    }

    fun daemonEnable(): Boolean {
        return mDaemonEnable
    }

    fun invalidDaemonEnable(enable: Boolean) {
        this.mDaemonEnable = enable
    }

    fun showShortcutPermissionDialog(): Boolean {
        return mShowShortcutPermissionDialog
    }

    fun addLifecycleCallback() {
        NexaCore.get().addAppLifecycleCallback(object : AppLifecycleCallback() {
            override fun beforeCreateApplication(packageName: String?, processName: String?, context: Context?, userId: Int) {
                Slog.d(TAG, "beforeCreateApplication: pkg $packageName, processName $processName, userID:${getUserId()}")
            }

            override fun beforeApplicationOnCreate(packageName: String?, processName: String?, application: Application?, userId: Int) {
                Slog.d(TAG, "beforeApplicationOnCreate: pkg $packageName, processName $processName")
            }

            override fun afterApplicationOnCreate(packageName: String?, processName: String?, application: Application?, userId: Int) {
                Slog.d(TAG, "afterApplicationOnCreate: pkg $packageName, processName $processName")
            }
        })
    }

    fun attachBaseContext(context: Context) {
        NexaCore.get().doAttachBaseContext(context, object : ClientConfiguration() {
            override fun getHostPackageName(): String {
                return context.packageName
            }

            override fun setHideRoot(): Boolean {
                return mHideRoot
            }

            override fun setHideXposed(): Boolean {
                return mHideXposed
            }

            override fun isEnableDaemonService(): Boolean {
                return mDaemonEnable
            }

            override fun requestInstallPackage(file: File?): Boolean {
                return false
            }
        })
    }

    fun doOnCreate() {
        NexaCore.get().doCreate()
    }

    companion object {
        val TAG: String = BlackBoxLoader::class.java.simpleName
    }
}
