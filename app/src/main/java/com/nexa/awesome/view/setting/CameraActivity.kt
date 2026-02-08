package com.nexa.awesome.view.setting

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.*
import com.nexa.awesome.R
import com.nexa.awesome.camera.MultiPreferences
import com.nexa.awesome.camera.VirtualCameraService
import com.nexa.awesome.settings.MethodType
import com.nexa.awesome.util.AppUtil
import com.nexa.awesome.util.PermissionUtils
import com.nexa.awesome.util.ToastUtils

class CameraActivity : BaseFragment() {

    private lateinit var btnProtectMethod: AppCompatButton
    private lateinit var btnSave: AppCompatButton
    private lateinit var tvProtectMethodText: AppCompatTextView
    private lateinit var tvTip: AppCompatTextView
    private lateinit var tvAudioText: AppCompatTextView
    private lateinit var etInput: AppCompatEditText
    private lateinit var switchAudio: SwitchCompat
    private lateinit var btnChooseVideo: AppCompatButton
    private lateinit var btnPreview: AppCompatButton
    private lateinit var btnPermissions: AppCompatButton

    private var selectedUri: Uri? = null
    private var methodType: Int = MethodType.TYPE_DISABLE_CAMERA
    private var mediaPlayer: MediaPlayer? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ToastUtils.showToast("Camera permission granted")
        } else {
            ToastUtils.showToast("Camera permission denied")
        }
    }

    private val openDocumentResult = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            selectedUri = it
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            etInput.setText(it.toString())
            initializeMediaPlayer(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.activity_camera, container, false)
        initView(view)
        return view
    }

    private fun initView(rootView: View) {
        btnProtectMethod = rootView.findViewById(R.id.protect_method_btn)
        btnSave = rootView.findViewById(R.id.protect_save)
        tvProtectMethodText = rootView.findViewById(R.id.protect_method_text)
        tvTip = rootView.findViewById(R.id.protect_tip)
        etInput = rootView.findViewById(R.id.protect_path)
        tvAudioText = rootView.findViewById(R.id.protect_audio)
        switchAudio = rootView.findViewById(R.id.protect_audio_switch)
        btnChooseVideo = rootView.findViewById(R.id.protect_video_select)
        btnPreview = rootView.findViewById(R.id.btn_preview)
        btnPermissions = rootView.findViewById(R.id.btn_permissions)

        btnChooseVideo.setOnClickListener {
            if (PermissionUtils.checkStoragePermission(requireContext())) {
                openDocumentResult.launch(arrayOf("video/*"))
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        btnProtectMethod.setOnClickListener { view ->
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.camera_menu)
                setOnMenuItemClickListener { item: MenuItem ->
                    methodType = when (item.itemId) {
                        R.id.protect_method_disable_camera -> MethodType.TYPE_DISABLE_CAMERA
                        R.id.protect_method_local -> MethodType.TYPE_LOCAL_VIDEO
                        R.id.protect_method_network -> MethodType.TYPE_NETWORK_VIDEO
                        else -> MethodType.TYPE_DISABLE_CAMERA
                    }
                    updateUI(methodType)
                    true
                }
                show()
            }
        }

        btnSave.setOnClickListener { saveSettings() }
        btnPreview.setOnClickListener { previewVideo() }
        btnPermissions.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }

        methodType = MultiPreferences.getInstance().getInt("method_type", MethodType.TYPE_DISABLE_CAMERA)
        updateUI(methodType)
        
        // Load saved video path if exists
        when (methodType) {
            MethodType.TYPE_LOCAL_VIDEO -> {
                val path = MultiPreferences.getInstance().getString("video_path_local", "")
                if (!path.isNullOrEmpty()) {
                    selectedUri = Uri.parse(path)
                    etInput.setText(path)
                }
            }
            MethodType.TYPE_NETWORK_VIDEO -> {
                val path = MultiPreferences.getInstance().getString("video_path_network", "")
                if (!path.isNullOrEmpty()) {
                    etInput.setText(path)
                }
            }
        }
    }

    private fun updateUI(type: Int) {
        when (type) {
            MethodType.TYPE_DISABLE_CAMERA -> {
                etInput.visibility = View.GONE
                btnChooseVideo.visibility = View.GONE
                tvAudioText.visibility = View.GONE
                switchAudio.visibility = View.GONE
                btnPreview.visibility = View.GONE
                tvProtectMethodText.text = "Camera Disabled"
            }
            MethodType.TYPE_LOCAL_VIDEO -> {
                etInput.visibility = View.VISIBLE
                etInput.isEnabled = false
                btnChooseVideo.visibility = View.VISIBLE
                tvAudioText.visibility = View.VISIBLE
                switchAudio.visibility = View.VISIBLE
                btnPreview.visibility = View.VISIBLE
                tvProtectMethodText.text = "Local Video"
            }
            MethodType.TYPE_NETWORK_VIDEO -> {
                etInput.visibility = View.VISIBLE
                etInput.isEnabled = true
                btnChooseVideo.visibility = View.GONE
                tvAudioText.visibility = View.VISIBLE
                switchAudio.visibility = View.VISIBLE
                btnPreview.visibility = View.VISIBLE
                tvProtectMethodText.text = "Network Video"
            }
        }
    }

    private fun saveSettings() {
        if (!PermissionUtils.checkCameraPermission(requireContext())) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }

        AppUtil.killAllApps()
        MultiPreferences.getInstance().setInt("method_type", methodType)

        when (methodType) {
            MethodType.TYPE_DISABLE_CAMERA -> {
                stopVirtualCamera()
                ToastUtils.showToast("Camera Disabled")
            }
            MethodType.TYPE_LOCAL_VIDEO -> {
                if (selectedUri == null) {
                    ToastUtils.showToast("Please select a video first")
                    return
                }
                MultiPreferences.getInstance().setString("video_path_local", selectedUri.toString())
                startVirtualCamera(selectedUri.toString(), isNetwork = false)
                ToastUtils.showToast("Local video saved")
            }
            MethodType.TYPE_NETWORK_VIDEO -> {
                val url = etInput.text.toString()
                if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
                    ToastUtils.showToast("Invalid URL")
                    return
                }
                MultiPreferences.getInstance().setString("video_path_network", url)
                startVirtualCamera(url, isNetwork = true)
                ToastUtils.showToast("Network video saved")
            }
        }
    }

    private fun startVirtualCamera(videoPath: String, isNetwork: Boolean) {
        val intent = Intent(requireContext(), VirtualCameraService::class.java).apply {
            putExtra("video_path", videoPath)
            putExtra("is_network", isNetwork)
            putExtra("enable_audio", switchAudio.isChecked)
        }
        requireContext().startService(intent)
    }

    private fun stopVirtualCamera() {
        val intent = Intent(requireContext(), VirtualCameraService::class.java)
        requireContext().stopService(intent)
    }

    private fun initializeMediaPlayer(uri: Uri) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(requireContext(), uri).apply {
            setOnPreparedListener { mp ->
                btnPreview.isEnabled = true
            }
            isLooping = true
        }
    }

    private fun previewVideo() {
        when (methodType) {
            MethodType.TYPE_LOCAL_VIDEO -> {
                if (selectedUri != null) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(selectedUri, "video/*")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    startActivity(intent)
                }
            }
            MethodType.TYPE_NETWORK_VIDEO -> {
                val url = etInput.text.toString()
                if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(url), "video/*")
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}