package app.pineappletv.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {
    
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            onPermissionResult?.invoke(allGranted)
        }
    
    private val manageStorageLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val hasManagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
            onPermissionResult?.invoke(hasManagePermission)
        }
    
    /**
     * 检查是否有存储权限
     */
    fun hasStoragePermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+
                hasPermission(Manifest.permission.READ_MEDIA_VIDEO) &&
                hasPermission(Manifest.permission.READ_MEDIA_IMAGES) &&
                hasPermission(Manifest.permission.READ_MEDIA_AUDIO)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+
                Environment.isExternalStorageManager()
            }
            else -> {
                // Android 10 及以下
                hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    
    /**
     * 检查是否有相册权限
     */
    fun hasMediaPermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+
                hasPermission(Manifest.permission.READ_MEDIA_VIDEO) &&
                hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                // Android 12 及以下
                hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    /**
     * 请求存储权限
     */
    fun requestStoragePermissions(callback: (Boolean) -> Unit) {
        onPermissionResult = callback
        
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+
                val permissions = arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
                requestPermissionLauncher.launch(permissions)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        manageStorageLauncher.launch(intent)
                    } catch (e: Exception) {
                        // 如果无法打开设置页面，回退到普通权限请求
                        val permissions = arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        requestPermissionLauncher.launch(permissions)
                    }
                } else {
                    callback(true)
                }
            }
            else -> {
                // Android 10 及以下
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissionLauncher.launch(permissions)
            }
        }
    }
    
    /**
     * 请求相册权限
     */
    fun requestMediaPermissions(callback: (Boolean) -> Unit) {
        onPermissionResult = callback
        
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+
                val permissions = arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                requestPermissionLauncher.launch(permissions)
            }
            else -> {
                // Android 12 及以下
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                requestPermissionLauncher.launch(permissions)
            }
        }
    }
    
    /**
     * 请求所有必要权限
     */
    fun requestAllPermissions(callback: (Boolean) -> Unit) {
        onPermissionResult = { storageGranted ->
            if (storageGranted) {
                // 存储权限获取成功后，检查相册权限
                if (hasMediaPermissions()) {
                    callback(true)
                } else {
                    requestMediaPermissions(callback)
                }
            } else {
                callback(false)
            }
        }
        
        if (hasStoragePermissions()) {
            // 如果已有存储权限，直接检查相册权限
            if (hasMediaPermissions()) {
                callback(true)
            } else {
                requestMediaPermissions(callback)
            }
        } else {
            requestStoragePermissions { granted ->
                onPermissionResult?.invoke(granted)
            }
        }
    }
    
    /**
     * 检查单个权限
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 是否应该显示权限说明
     */
    fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 打开应用设置页面
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }
}