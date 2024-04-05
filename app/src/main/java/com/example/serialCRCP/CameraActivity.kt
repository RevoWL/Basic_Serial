package com.example.serialCRCP

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.serialCRCP.databinding.ActivityAnteriorRecordingBinding
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraActivity
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.ICaptureCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.camera.bean.CameraRequest.RenderMode
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.CaptureMediaView
import com.jiangdg.ausbc.widget.IAspectRatio
import java.io.File


class AnteriorEyeRecordingActivity : CameraActivity() {

    private lateinit var binding: ActivityAnteriorRecordingBinding

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


    override fun getCameraView(): IAspectRatio? {
        return AspectRatioTextureView(this)
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return binding.cameraViewContainer
    }

    override fun getRootView(layoutInflater: LayoutInflater): View? {

        checkPermissions()
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), "MyApp"
        )
        // Create the storage directory if it does not exist
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("APP", "failed to create directory APPRootFolderName")
            }
        }

        binding = ActivityAnteriorRecordingBinding.inflate(getLayoutInflater())

        binding.maxDuration.setOnClickListener {
            captureImage()

        }

        binding.captureBtn.setCaptureMode(CaptureMediaView.CaptureMode.MODE_CAPTURE_PIC)
        binding.captureBtn.setCaptureViewTheme(CaptureMediaView.CaptureViewTheme.THEME_WHITE)
        binding.captureBtn.setOnViewClickListener(object :
            CaptureMediaView.OnViewClickListener {
            override fun onViewClick(mode: CaptureMediaView.CaptureMode?) {
                captureImage()
            }

        })

        return binding.root
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        if (code === ICameraStateCallBack.State.ERROR) {
            unRegisterMultiCamera()
            registerMultiCamera()
        }
//        else if (code === ICameraStateCallBack.State.OPENED) {
//            binding.countTimer.text = getFocus().toString()
//
//            binding.maxDuration.text = getZoom().toString()
//        }

    }

    fun captureImage() {
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        println(path)
        captureImage(
            object : ICaptureCallBack {
                override fun onBegin() {
                }

                override fun onComplete(path: String?) {
                }

                override fun onError(error: String?) {
                }

            }, "$path/MyAPP/something2.png"
        )

    }

    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewHeight(1080)
            .setPreviewWidth(1920)
            .setRenderMode(RenderMode.NORMAL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAspectRatioShow(false)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .setAudioSource(CameraRequest.AudioSource.NONE)
            .create()
    }


    private fun checkPermissions() {

        // Check Require Permission
        if (!isRequiredPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                100
            )
        }

        // Check Special Permission (Storage Access)
        if (!Environment.isExternalStorageManager()) {
            // Request the MANAGE_EXTERNAL_STORAGE permission
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.setData(Uri.parse("package:$packageName"))
            startActivityForResult(intent, 101)
        }
    }

    private fun isRequiredPermissionGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (!isRequiredPermissionGranted()) {
                Toast.makeText(
                    this,
                    "Please allow the required permission to continue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}


