package com.nexa.awesome.view.apps

import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.nexa.awesome.NexaCore
import com.nexa.awesome.R
import com.nexa.awesome.bean.AppInfo
import com.nexa.awesome.databinding.FragmentAppsBinding
import com.nexa.awesome.util.InjectionUtil
import com.nexa.awesome.util.ShortcutUtil
import com.nexa.awesome.util.ToastEx.toast
import com.nexa.awesome.util.ViewBindingEx.inflate
import com.nexa.awesome.view.base.LoadingActivity
import com.nexa.awesome.view.main.MainActivity
import java.util.*
import kotlin.math.abs

class AppsFragment : Fragment() {
    var userID: Int = 0
    private lateinit var viewModel: AppsViewModel
    private lateinit var mAdapter: RVAdapter<AppInfo>
    private val viewBinding: FragmentAppsBinding by inflate()
    private var popupMenu: PopupMenu? = null
    // Store package name being installed to check for OBB later
    private var installingPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, InjectionUtil.getAppsFactory())[AppsViewModel::class.java]
        userID = requireArguments().getInt("userID", 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding.stateView.showEmpty()
        mAdapter = RVAdapter<AppInfo>(requireContext(), AppsAdapter())
			.bind(viewBinding.recyclerView)

        viewBinding.recyclerView.adapter = mAdapter
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        val touchCallBack = AppsTouchCallBack { from, to ->
            onItemMove(from, to)
            viewModel.updateSortLiveData.postValue(true)
        }

        val itemTouchHelper = ItemTouchHelper(touchCallBack)
        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        mAdapter.setItemClickListener { _, data, _ ->
            showLoading()
            viewModel.launchApk(data.packageName, userID)
        }

        interceptTouch()
        setOnLongClick()
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Force Red Void Background
        view.setBackgroundResource(R.drawable.bg_quantum_field)
        viewBinding.recyclerView.background = null
        viewBinding.stateView.background = null
        
        
        initData()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getInstalledApps(userID)
    }

    /**
     * 拖拽优化
     */
    private fun interceptTouch() {
        val point = Point()

        viewBinding.recyclerView.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_UP -> {
                    if (!isMove(point, e)) {
                        popupMenu?.show()
                    }

                    popupMenu = null
                    point.set(0, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (point.x == 0 && point.y == 0) {
                        point.x = e.rawX.toInt()
                        point.y = e.rawY.toInt()
                    }

                    isDownAndUp(point, e)
                    if (isMove(point, e)) {
                        popupMenu?.dismiss()
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun isMove(point: Point, e: MotionEvent): Boolean {
        val max = 40
        val x = point.x
        val y = point.y

        val xU = abs(x - e.rawX)
        val yU = abs(y - e.rawY)
        return xU > max || yU > max
    }

    private fun isDownAndUp(point: Point, e: MotionEvent) {
        val min = 10
        val y = point.y
        val yU = y - e.rawY

        if (abs(yU) > min) {
            (requireActivity() as MainActivity).showFloatButton(yU < 0)
        }
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mAdapter.getItems(), i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mAdapter.getItems(), i, i - 1)
            }
        }
        mAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    private fun setOnLongClick() {
        mAdapter.setItemLongClickListener { view, data, _ ->
            // Use ContextThemeWrapper to force the RedPopupMenu style
            val wrapper = android.view.ContextThemeWrapper(requireContext(), R.style.RedPopupMenu)
            popupMenu = PopupMenu(wrapper, view).also {
                it.inflate(R.menu.app_menu)
                it.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.app_remove -> {
                            if (data.isXpModule) {
                                toast(R.string.uninstall_module_toast)
                            } else {
                                unInstallApk(data)
                            }
                        }

                        R.id.app_open -> {
                            showLoading()
                            viewModel.launchApk(data.packageName, userID)
                        }

                        R.id.app_clear -> {
                            clearApk(data)
                        }

                        R.id.app_stop -> {
                            stopApk(data)
                        }

                        R.id.app_shortcut -> {
                            ShortcutUtil.createShortcut(requireContext(), userID, data)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                it.show()
            }
        }
    }

    /*interceptTouch()
        setOnLongClick()
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getInstalledApps(userID)
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mAdapter.getItems(), i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mAdapter.getItems(), i, i - 1)
            }
        }
        mAdapter.notifyItemMoved(fromPosition, toPosition)
    }*/

    private fun initData() {
        viewBinding.stateView.showLoading()
        viewModel.getInstalledApps(userID)
        viewModel.appsLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                mAdapter.setItems(it)
                if (it.isEmpty()) {
                    viewBinding.stateView.showEmpty()
                } else {
                    viewBinding.stateView.showContent()
                }
            }
        }

        viewModel.resultLiveData.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it)) {
                hideLoading()
                requireContext().toast(it)
                viewModel.getInstalledApps(userID)
                scanUser()
                
                // Show OBB popup ONLY if the installed app has OBB files
                if (it == getString(R.string.install_success)) {
                    val pkg = installingPackageName
                    if (pkg != null) {
                        val hasObb = com.nexa.awesome.util.ObbCopyHelper.hasObb(requireContext(), pkg)
                        if (hasObb) {
                             (requireActivity() as? MainActivity)?.checkAndShowObbPopup()
                        }
                    }
                    installingPackageName = null
                }
            }
        }

        viewModel.launchLiveData.observe(viewLifecycleOwner) {
            it?.run {
                hideLoading()
                if (!it) {
                    toast(R.string.start_fail)
                }
            }
        }

        viewModel.updateSortLiveData.observe(viewLifecycleOwner) {
            if (this::mAdapter.isInitialized) {
                viewModel.updateApkOrder(userID, mAdapter.getItems())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.resultLiveData.value = null
        viewModel.launchLiveData.value = null
    }

    private fun unInstallApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.uninstall_app)
            message(text = getString(R.string.uninstall_app_hint, info.name))
            positiveButton(R.string.done) {
                showLoading()
                viewModel.unInstall(info.packageName, userID)
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * 强行停止软件
     * @param info AppInfo
     */
    private fun stopApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_stop)
            message(text = getString(R.string.app_stop_hint, info.name))
            positiveButton(R.string.done) {
                NexaCore.get().stopPackage(info.packageName, userID)
                toast(getString(R.string.is_stop, info.name))
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * 清除软件数据
     * @param info AppInfo
     */
    private fun clearApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_clear)
            message(text = getString(R.string.app_clear_hint, info.name))
            positiveButton(R.string.done) {
                showLoading()
                viewModel.clearApkData(info.packageName, userID)
            }
            negativeButton(R.string.cancel)
        }
    }

    fun installApk(source: String) {
        // Handle case where source IS the package name (from ListActivity)
        if (!source.contains("/") && source.contains(".")) {
            installingPackageName = source
        } else {
            // Attempt to parse package name from APK path
            try {
                val pm = requireContext().packageManager
                val info = pm.getPackageArchiveInfo(source, 0)
                installingPackageName = info?.packageName
                
                if (installingPackageName == null) {
                   // Fallback for file paths
                   if (source.endsWith(".apk", true)) {
                       val fileName = java.io.File(source).name
                       val nameWithoutExt = fileName.substringBeforeLast(".")
                       if (nameWithoutExt.contains(".")) {
                           installingPackageName = nameWithoutExt
                       }
                   }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                installingPackageName = null
            }
        }
        
        showLoading()
        viewModel.install(source, userID)
    }

    private fun scanUser() {
        (requireActivity() as MainActivity).scanUser()
    }

    private fun showLoading() {
        if (requireActivity() is LoadingActivity) {
            (requireActivity() as LoadingActivity).showLoading()
        }
    }

    private fun hideLoading() {
        if (requireActivity() is LoadingActivity) {
            (requireActivity() as LoadingActivity).hideLoading()
        }
    }

    companion object {
        fun newInstance(userID: Int): AppsFragment {
            val fragment = AppsFragment()
            val bundle = bundleOf("userID" to userID)

            fragment.arguments = bundle
            return fragment
        }
    }
}
