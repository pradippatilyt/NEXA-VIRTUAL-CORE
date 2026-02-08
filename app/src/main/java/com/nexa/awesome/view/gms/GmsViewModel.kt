package com.nexa.awesome.view.gms

import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.bean.GmsBean
import com.nexa.awesome.bean.GmsInstallBean
import com.nexa.awesome.data.GmsRepository
import com.nexa.awesome.view.base.BaseViewModel

class GmsViewModel(private val mRepo: GmsRepository) : BaseViewModel() {
    val mInstalledLiveData = MutableLiveData<List<GmsBean>>()
    val mUpdateInstalledLiveData = MutableLiveData<GmsInstallBean>()

    fun getInstalledUser() {
        launchOnUI {
            mRepo.getGmsInstalledList(mInstalledLiveData)
        }
    }

    fun installGms(userID: Int) {
        launchOnUI {
            mRepo.installGms(userID, mUpdateInstalledLiveData)
        }
    }

    fun uninstallGms(userID: Int) {
        launchOnUI {
            mRepo.uninstallGms(userID, mUpdateInstalledLiveData)
        }
    }
}
