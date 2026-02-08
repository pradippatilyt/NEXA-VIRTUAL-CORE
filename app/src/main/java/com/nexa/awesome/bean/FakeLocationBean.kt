package com.nexa.awesome.bean

import android.graphics.drawable.Drawable
import com.nexa.awesome.entity.location.BLocation

data class FakeLocationBean(
    val userID: Int,
    val name: String,
    val icon: Drawable,
    val packageName: String,
    var fakeLocationPattern: Int,
    var fakeLocation: BLocation?
)
