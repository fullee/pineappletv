package app.pineappletv.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

/**
 * 权限帮助类，提供静态方法检查权限状态
 */
object PermissionHelper {
    
    /**
     * 获取需要的存储权限列表
     */
    fun getStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+
                arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
            else -> {
                // Android 12 及以下
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
    
    /**
     * 获取需要的媒体权限列表
     */
    fun getMediaPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+
                arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }
            else -> {
                // Android 12 及以下
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }
    
    /**
     * 检查是否具有存储权限
     */
    fun hasStoragePermissions(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+
                Environment.isExternalStorageManager()
            }
            else -> {
                // Android 10 及以下
                getStoragePermissions().all { permission ->
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
    }
    
    /**
     * 检查是否具有媒体权限
     */
    fun hasMediaPermissions(context: Context): Boolean {
        return getMediaPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查是否具有所有必要权限
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasStoragePermissions(context) && hasMediaPermissions(context)
    }
    
    /**
     * 获取权限描述信息
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> "读取外部存储"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "写入外部存储"
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> "管理所有文件"
            Manifest.permission.READ_MEDIA_VIDEO -> "访问视频文件"
            Manifest.permission.READ_MEDIA_IMAGES -> "访问图片文件"
            Manifest.permission.READ_MEDIA_AUDIO -> "访问音频文件"
            Manifest.permission.ACCESS_MEDIA_LOCATION -> "访问媒体位置信息"
            else -> "未知权限"
        }
    }
}