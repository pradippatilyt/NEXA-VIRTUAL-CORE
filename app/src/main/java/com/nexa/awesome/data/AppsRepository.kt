package com.nexa.awesome.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.NexaCore
import com.nexa.awesome.NexaCore.getPackageManager
import com.nexa.awesome.core.GmsCore
import com.nexa.awesome.utils.AbiUtils
import com.nexa.awesome.utils.Slog
import com.nexa.awesome.utils.compat.BuildCompat
import com.nexa.awesome.R
import com.nexa.awesome.app.AppManager
import com.nexa.awesome.bean.AppInfo
import com.nexa.awesome.bean.InstalledAppBean
import com.nexa.awesome.util.ResUtil.getString
import java.io.File

class AppsRepository {
    private val TAG: String = "AppsRepository"
    private var mInstalledList = mutableListOf<AppInfo>()

    fun previewInstallList() {
        synchronized(mInstalledList) {
            val installedList = mutableListOf<AppInfo>()
            val installedApplications: List<ApplicationInfo> = if (BuildCompat.isT()) {
                getPackageManager().getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                getPackageManager().getInstalledApplications(0)
            }

            for (installedApplication in installedApplications) {
                val file = File(installedApplication.sourceDir)
                if (installedApplication.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    continue
                }

                if (!AbiUtils.isSupport(file)) {
                    continue
                }

                val isXpModule = NexaCore.get().isXposedModule(file)
                val info = AppInfo(
                    installedApplication.loadLabel(getPackageManager()).toString(),
                    installedApplication.loadIcon(getPackageManager()),
                    installedApplication.packageName,
                    installedApplication.sourceDir,
                    isXpModule
                )
                installedList.add(info)
            }

            installedList.sortWith { a, b ->
                if (a.name > b.name) {
                    1
                } else {
                    -1
                }
            }

            this.mInstalledList.clear()
            this.mInstalledList.addAll(installedList)
        }
    }

    fun getInstalledAppList(userID: Int, loadingLiveData: MutableLiveData<Boolean>, appsLiveData: MutableLiveData<List<InstalledAppBean>>) {
        loadingLiveData.postValue(true)

        synchronized(mInstalledList) {
            val nexaCore = NexaCore.get()
            Slog.d(TAG, mInstalledList.joinToString(","))

            val newInstalledList = mInstalledList.map {
                InstalledAppBean(it.name, it.icon, it.packageName, it.sourceDir, nexaCore.isInstalled(it.packageName, userID))
            }
            appsLiveData.postValue(newInstalledList)
            loadingLiveData.postValue(false)
        }
    }

    fun getInstalledModuleList(loadingLiveData: MutableLiveData<Boolean>, appsLiveData: MutableLiveData<List<InstalledAppBean>>) {
        loadingLiveData.postValue(true)

        synchronized(mInstalledList) {
            val nexaCore = NexaCore.get()
            val moduleList = mInstalledList.filter {
                it.isXpModule
            }.map {
                InstalledAppBean(it.name, it.icon, it.packageName, it.sourceDir, nexaCore.isInstalledXposedModule(it.packageName))
            }
            appsLiveData.postValue(moduleList)
            loadingLiveData.postValue(false)
        }
    }

    fun getVmInstallList(userId: Int, appsLiveData: MutableLiveData<List<AppInfo>>) {
        val sortListData = AppManager.mRemarkSharedPreferences.getString("AppList$userId", "")
        val sortList = sortListData?.split(",")

        val applicationList = NexaCore.get().getInstalledApplications(0, userId)
        val appInfoList = mutableListOf<AppInfo>()
        applicationList.also {
            if (sortList.isNullOrEmpty()) {
                return@also
            }
            it.sortWith(AppsSortComparator(sortList))
        }.forEach {
            val info = AppInfo(it.loadLabel(getPackageManager()).toString(), it.loadIcon(getPackageManager()), it.packageName, it.sourceDir,
                isInstalledXpModule(it.packageName))
            appInfoList.add(info)
        }
        appsLiveData.postValue(appInfoList)
    }

    private fun isInstalledXpModule(packageName: String): Boolean {
        NexaCore.get().installedXPModules.forEach {
            if (packageName == it.packageName) {
                return@isInstalledXpModule true
            }
        }
        return false
    }

    fun installApk(source: String, userId: Int, resultLiveData: MutableLiveData<String>) {
        val nexaCore = NexaCore.get()
        val installResult = if (URLUtil.isValidUrl(source)) {
            val uri = Uri.parse(source)
            nexaCore.installPackageAsUser(uri, userId)
        } else {
            nexaCore.installPackageAsUser(source, userId)
        }

        if (installResult.success) {
            updateAppSortList(userId, installResult.packageName, true)
            resultLiveData.postValue(getString(R.string.install_success))
        } else {
            resultLiveData.postValue(getString(R.string.install_fail, installResult.msg))
        }
        scanUser()
    }

    fun unInstall(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        NexaCore.get().uninstallPackageAsUser(packageName, userID)
        updateAppSortList(userID, packageName, false)
        scanUser()
        resultLiveData.postValue(getString(R.string.uninstall_success))
    }

    fun launchApk(packageName: String, userId: Int, launchLiveData: MutableLiveData<Boolean>) {
        val result = NexaCore.get().launchApk(packageName, userId)
        launchLiveData.postValue(result)
    }

    fun clearApkData(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        NexaCore.get().clearPackage(packageName, userID)
        resultLiveData.postValue(getString(R.string.clear_success))
    }

    /**
     * 倒序递归扫描用户，
     * 如果用户是空的，就删除用户，删除用户备注，删除应用排序列表
     */
    private fun scanUser() {
        val nexaCore = NexaCore.get()
        val userList = nexaCore.users

        if (userList.isEmpty()) {
            return
        }

        val id = userList.last().id
        if (nexaCore.getInstalledApplications(0, id).isEmpty()) {
            nexaCore.deleteUser(id)
            AppManager.mRemarkSharedPreferences.edit {
                remove("Remark$id")
                remove("AppList$id")
            }
            scanUser()
        }
    }

    /**
     * 更新排序列表
     * @param userID Int
     * @param pkg String
     * @param isAdd Boolean true是添加，false是移除
     */
    private fun updateAppSortList(userID: Int, pkg: String, isAdd: Boolean) {
        val savedSortList = AppManager.mRemarkSharedPreferences.getString("AppList$userID", "")
        val sortList = linkedSetOf<String>()
        if (savedSortList != null) {
            sortList.addAll(savedSortList.split(","))
        }

        if (isAdd) {
            sortList.add(pkg)
        } else {
            sortList.remove(pkg)
        }

        AppManager.mRemarkSharedPreferences.edit {
            putString("AppList$userID", sortList.joinToString(","))
        }
    }

    /**
     * 保存排序后的apk顺序
     */
    fun updateApkOrder(userID: Int, dataList: List<AppInfo>) {
        AppManager.mRemarkSharedPreferences.edit {
            putString("AppList$userID", dataList.joinToString(",") { it.packageName })
        }
    }
}
