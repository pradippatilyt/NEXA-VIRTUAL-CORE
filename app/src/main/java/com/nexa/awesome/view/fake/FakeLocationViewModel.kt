package com.nexa.awesome.view.fake

import androidx.lifecycle.MutableLiveData
import com.nexa.awesome.entity.location.BLocation
import com.nexa.awesome.bean.FakeLocationBean
import com.nexa.awesome.data.FakeLocationRepository
import com.nexa.awesome.view.base.BaseViewModel

class FakeLocationViewModel(private val mRepo: FakeLocationRepository) : BaseViewModel() {
    val appsLiveData = MutableLiveData<List<FakeLocationBean>>()

    fun getInstallAppList(userID: Int) {
        launchOnUI {
            mRepo.getInstalledAppList(userID, appsLiveData)
        }
    }

    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        launchOnUI {
            mRepo.setPattern(userId, pkg, pattern)
        }
    }

    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        launchOnUI {
            mRepo.setLocation(userId, pkg, location)
        }
    }
}
