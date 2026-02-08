package com.nexa.awesome.data

import android.net.Uri
import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.NexaCore
import com.nexa.awesome.NexaCore.getPackageManager
import com.nexa.awesome.R
import com.nexa.awesome.bean.XpModuleInfo
import com.nexa.awesome.util.ResUtil.getString

class XpRepository {
    fun getInstallModules(modulesLiveData: MutableLiveData<List<XpModuleInfo>>) {
        val moduleList = NexaCore.get().installedXPModules
        val result = mutableListOf<XpModuleInfo>()

        moduleList.forEach {
            val info = XpModuleInfo(it.name, it.desc, it.packageName, it.packageInfo.versionName, it.enable, it.application.loadIcon(getPackageManager()))
            result.add(info)
        }
        modulesLiveData.postValue(result)
    }

    fun installModule(source: String, resultLiveData: MutableLiveData<String>) {
        val nexaCore = NexaCore.get()
        val installResult = if (URLUtil.isValidUrl(source)) {
            val uri = Uri.parse(source)
            nexaCore.installXPModule(uri)
        } else {
            // source == packageName
            nexaCore.installXPModule(source)
        }

        if (installResult.success) {
            resultLiveData.postValue(getString(R.string.install_success))
        } else {
            resultLiveData.postValue(getString(R.string.install_fail, installResult.msg))
        }
    }

    fun unInstallModule(packageName: String, resultLiveData: MutableLiveData<String>) {
        NexaCore.get().uninstallXPModule(packageName)
        resultLiveData.postValue(getString(R.string.remove_success))
    }
}
