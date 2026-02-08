package com.nexa.awesome.view.list

import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.bean.InstalledAppBean
import com.nexa.awesome.data.AppsRepository
import com.nexa.awesome.view.base.BaseViewModel

class ListViewModel(private val repo: AppsRepository) : BaseViewModel() {
    val appsLiveData = MutableLiveData<List<InstalledAppBean>>()
    val loadingLiveData = MutableLiveData<Boolean>()

    fun previewInstalledList() {
        launchOnUI{
            repo.previewInstallList()
        }
    }

    fun getInstallAppList(userID: Int) {
        launchOnUI {
            repo.getInstalledAppList(userID, loadingLiveData, appsLiveData)
        }
    }

    fun getInstalledModules() {
        launchOnUI {
            repo.getInstalledModuleList(loadingLiveData, appsLiveData)
        }
    }
}
