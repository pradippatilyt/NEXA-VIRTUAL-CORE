package com.nexa.awesome.data

import android.content.pm.ApplicationInfo
import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.NexaCore
import com.nexa.awesome.entity.location.BLocation
import com.nexa.awesome.fake.frameworks.BLocationManager
import com.nexa.awesome.utils.Slog
import com.nexa.awesome.bean.FakeLocationBean

class FakeLocationRepository {
    private val TAG: String = "FakeLocationRepository"

    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        BLocationManager.get().setPattern(userId, pkg, pattern)
    }

    private fun getPattern(userId: Int, pkg: String): Int {
        return BLocationManager.get().getPattern(userId, pkg)
    }

    private fun getLocation(userId: Int, pkg: String): BLocation? {
        return BLocationManager.get().getLocation(userId, pkg)
    }

    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        BLocationManager.get().setLocation(userId, pkg, location)
    }

    fun getInstalledAppList(userID: Int, appsFakeLiveData: MutableLiveData<List<FakeLocationBean>>) {
        val installedList = mutableListOf<FakeLocationBean>()
        val installedApplications: List<ApplicationInfo> = NexaCore.get().getInstalledApplications(0, userID)
        // List<ApplicationInfo> -> List<FakeLocationBean>
        for (installedApplication in installedApplications) {
            /*val file = File(installedApplication.sourceDir)
            if ((installedApplication.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue
            }

            if (!AbiUtils.isSupport(file)) {
                continue
            }

            val isXpModule = NexaCore.get().isXposedModule(file)*/
            val info = FakeLocationBean(
                userID,
                installedApplication.loadLabel(NexaCore.getPackageManager()).toString(),
                installedApplication.loadIcon(NexaCore.getPackageManager()),
                installedApplication.packageName,
                getPattern(userID, installedApplication.packageName),
                getLocation(userID, installedApplication.packageName)
            )

            installedList.add(info)
        }

        Slog.d(TAG, installedList.joinToString(","))
        appsFakeLiveData.postValue(installedList)
    }
}
