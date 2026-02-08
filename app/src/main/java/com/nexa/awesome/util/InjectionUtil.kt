package com.nexa.awesome.util

import com.nexa.awesome.data.AppsRepository
import com.nexa.awesome.data.FakeLocationRepository
import com.nexa.awesome.data.GmsRepository
import com.nexa.awesome.data.XpRepository
import com.nexa.awesome.view.apps.AppsFactory
import com.nexa.awesome.view.fake.FakeLocationFactory
import com.nexa.awesome.view.gms.GmsFactory
import com.nexa.awesome.view.list.ListFactory
import com.nexa.awesome.view.xp.XpFactory

object InjectionUtil {
    private val appsRepository = AppsRepository()
    private val xpRepository = XpRepository()
    private val gmsRepository = GmsRepository()
    private val fakeLocationRepository = FakeLocationRepository()

    fun getAppsFactory() : AppsFactory {
        return AppsFactory(appsRepository)
    }

    fun getListFactory(): ListFactory {
        return ListFactory(appsRepository)
    }

    fun getXpFactory():XpFactory{
        return XpFactory(xpRepository)
    }

    fun getGmsFactory():GmsFactory{
        return GmsFactory(gmsRepository)
    }

    fun getFakeLocationFactory():FakeLocationFactory{
        return FakeLocationFactory(fakeLocationRepository)
    }
}
