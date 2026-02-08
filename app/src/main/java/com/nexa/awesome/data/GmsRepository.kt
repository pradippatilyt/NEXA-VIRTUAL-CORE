package com.nexa.awesome.data

import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.NexaCore
import com.nexa.awesome.R
import com.nexa.awesome.app.AppManager
import com.nexa.awesome.bean.GmsBean
import com.nexa.awesome.bean.GmsInstallBean
import com.nexa.awesome.util.ResUtil.getString

class GmsRepository {
    fun getGmsInstalledList(mInstalledLiveData: MutableLiveData<List<GmsBean>>) {
        val userList = arrayListOf<GmsBean>()

        NexaCore.get().users.forEach {
            val userId = it.id
            val userName = AppManager.mRemarkSharedPreferences.getString("Remark$userId", "User $userId") ?: ""
            val isInstalled = NexaCore.get().isInstallGms(userId)
            val bean = GmsBean(userId, userName, isInstalled)
            userList.add(bean)
        }
        mInstalledLiveData.postValue(userList)
    }

    fun installGms(userID: Int, mUpdateInstalledLiveData: MutableLiveData<GmsInstallBean>) {
        val installResult = NexaCore.get().installGms(userID)
        val result = if (installResult.success) {
            getString(R.string.install_success)
        } else {
            getString(R.string.install_fail, installResult.msg)
        }

        val bean = GmsInstallBean(userID, installResult.success, result)
        mUpdateInstalledLiveData.postValue(bean)
    }

    fun uninstallGms(userID: Int, mUpdateInstalledLiveData: MutableLiveData<GmsInstallBean>) {
        var isSuccess = false
        if (NexaCore.get().isInstallGms(userID)) {
            isSuccess = NexaCore.get().uninstallGms(userID)
        }

        val result = if (isSuccess) {
            getString(R.string.uninstall_success)
        } else {
            getString(R.string.uninstall_fail)
        }

        val bean = GmsInstallBean(userID, isSuccess, result)
        mUpdateInstalledLiveData.postValue(bean)
    }
}
