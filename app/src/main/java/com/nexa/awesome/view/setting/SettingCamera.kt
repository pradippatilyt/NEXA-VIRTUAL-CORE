package com.nexa.awesome.view.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.nexa.awesome.R
import com.nexa.awesome.databinding.ActivitySettingBinding
import com.nexa.awesome.util.ViewBindingEx.inflate
import com.nexa.awesome.view.base.BaseActivity

class SettingCamera : BaseActivity() {
    private val viewBinding: ActivitySettingBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.Camera_setting, true)
        supportFragmentManager.beginTransaction().replace(R.id.fragment, CameraActivity()).commit()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingCamera::class.java)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            context.startActivity(intent)
        }
    }
}
