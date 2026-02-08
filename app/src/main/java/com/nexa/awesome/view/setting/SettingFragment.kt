package com.nexa.awesome.view.setting

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.nexa.awesome.NexaCore
import com.nexa.awesome.R
import com.nexa.awesome.app.AppManager
import com.nexa.awesome.util.ToastEx.toast
import com.nexa.awesome.view.gms.GmsManagerActivity
import com.nexa.awesome.view.xp.XpActivity
import com.nexa.awesome.util.ObbCopyHelper

class SettingFragment : PreferenceFragmentCompat() {

    private lateinit var accessibilityServiceEnable: SwitchPreferenceCompat
    private lateinit var xpEnable: SwitchPreferenceCompat
    private lateinit var xpModule: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.bg_quantum_field)
        
        // Robustly find and clear RecyclerView background
        view.findViewById<androidx.recyclerview.widget.RecyclerView>(androidx.preference.R.id.recycler_view)?.background = null
        
        initPrefs()
    }

    private fun initPrefs() {
        xpEnable = findPreference("xp_enable")!!
        xpEnable.isChecked = NexaCore.get().isXPEnable

        xpEnable.setOnPreferenceChangeListener { _, newValue ->
            NexaCore.get().isXPEnable = (newValue == true)
            true
        }
        // xp模块跳转
        xpModule = findPreference("xp_module")!!
        xpModule.setOnPreferenceClickListener {
            val intent = Intent(requireActivity(), XpActivity::class.java)
            requireContext().startActivity(intent)
            true
        }

        initGms()
        invalidHideState{
            val xpHidePreference: Preference = (findPreference("xp_hide")!!)
            val hideXposed = AppManager.mBlackBoxLoader.hideXposed()
            xpHidePreference.setDefaultValue(hideXposed)
            xpHidePreference
        }

        invalidHideState{
            val rootHidePreference: Preference = (findPreference("root_hide")!!)
            val hideRoot = AppManager.mBlackBoxLoader.hideRoot()
            rootHidePreference.setDefaultValue(hideRoot)
            rootHidePreference
        }

        invalidHideState {
            val daemonPreference: Preference = (findPreference("daemon_enable")!!)
            val mDaemonEnable = AppManager.mBlackBoxLoader.daemonEnable()
            daemonPreference.setDefaultValue(mDaemonEnable)
            daemonPreference
        }
        
        accessibilityServiceEnable = findPreference("accessibility_service_enable")!!
        accessibilityServiceEnable.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true && !isAccessibilityServiceEnabled()) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                toast("Please enable Accessibility Service manually.")
                false
            } else {
                true
            }
        }
        
        
        

        findPreference<Preference>("copy_obb")?.setOnPreferenceClickListener {
            toast(R.string.copy_obb_start)
            ObbCopyHelper.copyGameObbs(requireContext()) { _, msg ->
                toast(msg)
            }
            true
        }
    }

    private fun initGms() {
        val gmsManagerPreference: Preference = (findPreference("gms_manager")!!)

        if (NexaCore.get().isSupportGms) {
            gmsManagerPreference.setOnPreferenceClickListener {
                GmsManagerActivity.start(requireContext())
                true
            }
        } else {
            gmsManagerPreference.summary = getString(R.string.no_gms)
            gmsManagerPreference.isEnabled = false
        }
    }

    private fun invalidHideState(block: () -> Preference) {
        val pref = block()
        pref.setOnPreferenceChangeListener { preference, newValue ->
            val tmpHide = (newValue == true)
            when (preference.key) {
                "xp_hide" -> {
                    AppManager.mBlackBoxLoader.invalidHideXposed(tmpHide)
                }

                "root_hide" -> {
                    AppManager.mBlackBoxLoader.invalidHideRoot(tmpHide)
                }

                "daemon_enable" -> {
                    AppManager.mBlackBoxLoader.invalidDaemonEnable(tmpHide)
                }
            }

            toast(R.string.restart_module)
            return@setOnPreferenceChangeListener true
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { service ->
            service.resolveInfo.serviceInfo.packageName == requireContext().packageName &&
            service.resolveInfo.serviceInfo.name == OverlayAccessibilityService::class.java.name
        }
    }
}
