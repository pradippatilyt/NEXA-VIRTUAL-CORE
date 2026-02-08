package com.nexa.awesome.view.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.customview.customView
import com.nexa.awesome.NexaCore
import com.nexa.awesome.R
import com.nexa.awesome.app.App
import com.nexa.awesome.app.AppManager
import com.nexa.awesome.databinding.ActivityMainBinding
import com.nexa.awesome.util.Resolution
import com.nexa.awesome.util.ViewBindingEx.inflate
import com.nexa.awesome.view.apps.AppsFragment
import com.nexa.awesome.view.base.LoadingActivity
import com.nexa.awesome.view.fake.FakeManagerActivity
import com.nexa.awesome.view.list.ListActivity
import com.nexa.awesome.view.setting.SettingActivity
import com.nexa.awesome.view.setting.SettingCamera
import com.nexa.awesome.util.ObbCopyHelper
import com.nexa.awesome.util.ToastEx.toast

class MainActivity : LoadingActivity() {
    private val viewBinding: ActivityMainBinding by inflate()
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private val fragmentList = mutableListOf<AppsFragment>()
    private var currentUser = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.app_name)
        initViewPager()
        initFab()
        initToolbarSubTitle()
        initToolbarSubTitle()
        manageFiles()
        
        initToolbarSubTitle()
        initToolbarSubTitle()
        manageFiles()
        
        // Start AI Glitch Animation
        val glitchAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.ai_glitch)
        viewBinding.fab.startAnimation(glitchAnim)
        
        playStartupSound()
    }

    private fun playStartupSound() {
        try {
            // User needs to add 'startup.mp3' to res/raw
            // If file missing, this might throw or just fail silently at compilation if R.raw.startup doesn't exist.
            // Since I cannot create the file dynamically, I will use a resource identifier check.
            val resId = resources.getIdentifier("startup", "raw", packageName)
            if (resId != 0) {
                val mediaPlayer = android.media.MediaPlayer.create(this, resId)
                mediaPlayer.setOnCompletionListener { it.release() }
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkAndShowObbPopup() {
        MaterialDialog(this).show {
            customView(R.layout.dialog_game_obb)
            positiveButton(text = "INITIALIZE IMPORT") {
                // Show blocking progress dialog
                val progressDialog = MaterialDialog(this@MainActivity).show {
                    customView(R.layout.dialog_obb_copying)
                    cancelable(false)
                    cancelOnTouchOutside(false)
                }

                toast(R.string.copy_obb_start)
                ObbCopyHelper.copyGameObbs(this@MainActivity) { success, msg ->
                     progressDialog.dismiss()
                     toast(msg)
                }
            }
            negativeButton(text = "SKIP")
        }
    }

    private fun initToolbarSubTitle() {
        updateUserRemark(0)
        viewBinding.toolbarLayout.toolbar.getChildAt(1).setOnClickListener {
            MaterialDialog(this).show {
                title(res = R.string.userRemark)
                input(
                    hintRes = R.string.userRemark,
                    prefill = viewBinding.toolbarLayout.toolbar.subtitle
                ) { _, input ->
                    AppManager.mRemarkSharedPreferences.edit {
                        putString("Remark$currentUser", input.toString())
                        viewBinding.toolbarLayout.toolbar.subtitle = input
                    }
                }
                positiveButton(res = R.string.done)
                negativeButton(res = R.string.cancel)
            }
        }
    }

    private fun initViewPager() {
        val userList = NexaCore.get().users
        userList.forEach {
            fragmentList.add(AppsFragment.newInstance(it.id))
        }

        currentUser = userList.firstOrNull()?.id ?: 0
        fragmentList.add(AppsFragment.newInstance(userList.size))

        mViewPagerAdapter = ViewPagerAdapter(this)
        mViewPagerAdapter.replaceData(fragmentList)
        viewBinding.viewPager.adapter = mViewPagerAdapter

        viewBinding.dotsIndicator.attachTo(viewBinding.viewPager)
        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentUser = fragmentList[position].userID

                updateUserRemark(currentUser)
                showFloatButton(true)
            }
        })
    }

    private fun initFab() {
        viewBinding.fab.setOnClickListener {
            val userId = viewBinding.viewPager.currentItem
            val intent = Intent(this, ListActivity::class.java)

            intent.putExtra("userID", userId)
            apkPathResult.launch(intent)
        }
    }

    fun showFloatButton(show: Boolean) {
        val tranY: Float = Resolution.convertDpToPixel(120F, App.getContext())
        val time = 200L

        if (show) {
            viewBinding.fab.animate().translationY(0f).alpha(1f).setDuration(time)
                .start()
        } else {
            viewBinding.fab.animate().translationY(tranY).alpha(0f).setDuration(time)
                .start()
        }
    }

    fun scanUser() {
        val userList = NexaCore.get().users

        if (fragmentList.size == userList.size) {
            fragmentList.add(AppsFragment.newInstance(fragmentList.size))
        } else if (fragmentList.size > userList.size + 1) {
            fragmentList.removeLast()
        }
        mViewPagerAdapter.notifyDataSetChanged()
    }

    private fun updateUserRemark(userId: Int) {
        var remark = AppManager.mRemarkSharedPreferences.getString("Remark$userId", "User $userId")
        if (remark.isNullOrEmpty()) {
            remark = "User $userId"
        }
        viewBinding.toolbarLayout.toolbar.subtitle = remark
    }

    private val apkPathResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.let { data ->
                    val userId = data.getIntExtra("userID", 0)
                    val source = data.getStringExtra("source")

                    if (source != null) {
                        fragmentList[userId].installApk(source)
                    }
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_tg -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Jagdishvip/Black-box-4"))
                startActivity(intent)
            }

            R.id.main_setting -> {
                SettingActivity.start(this)
            }

            R.id.Camera_setting -> {
                SettingCamera.start(this)
            }

            R.id.fake_location -> {
                val intent = Intent(this, FakeManagerActivity::class.java)
                intent.putExtra("userID", currentUser)
                startActivity(intent)
            }
        }
        return true
    }
    
    companion object {
        const val PERMISSION_REQUEST_STORAGE = 1001
        const val REQUEST_MANAGE_UNKNOWN_APP_SOURCES = 1002
        const val REQUEST_OVERLAY_PERMISSION = 1003
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    // =============== PERMISSIONS LOGIC BELOW ===============
    private fun takeFilePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),PERMISSION_REQUEST_STORAGE)
        }
    }
    
    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestInstallUnknownAppPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_MANAGE_UNKNOWN_APP_SOURCES)
            } else {
                if (!isPermissionGranted()) {
                    takeFilePermissions()
                }
            }
        }
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            } else {
                requestInstallUnknownAppPermission()
            }
        }
    }
    
    private fun manageFiles() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            } else {
                requestOverlayPermission()
            }
        } else {
            requestOverlayPermission()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestOverlayPermission()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            requestInstallUnknownAppPermission()
        }
    }
    

    
}
