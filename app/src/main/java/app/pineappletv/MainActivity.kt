package app.pineappletv

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.ui.navigation.PineappleTVNavigation
import app.pineappletv.ui.screen.PermissionScreen
import app.pineappletv.ui.theme.PineappleTVTheme
import app.pineappletv.utils.PermissionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val videoRepository: VideoRepository by inject()
    private lateinit var permissionManager: PermissionManager
    private var hasPermissions by mutableStateOf(false)
    private var showPermissionScreen by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检测设备类型并设置屏幕方向
        setupScreenOrientation()
        
        permissionManager = PermissionManager(this)
        
        // 首先检查权限
        checkAndRequestPermissions()
    }
    
    private fun setupScreenOrientation() {
        // 检查是否是TV设备
        val isTV = packageManager.hasSystemFeature("android.software.leanback")
        
        if (isTV) {
            // TV设备强制横屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // TV设备使用Leanback主题
            setTheme(R.style.Theme_PineappleTV_TV)
        } else {
            // 手机设备允许自由旋转，但优先竖屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            // 手机设备使用Material3主题
            setTheme(R.style.Theme_PineappleTV)
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (permissionManager.hasStoragePermissions() && permissionManager.hasMediaPermissions()) {
            // 权限已获取，初始化应用
            hasPermissions = true
            initializeApp()
        } else {
            // 显示权限请求界面
            showPermissionScreen = true
            setupPermissionScreen()
        }
    }
    
    private fun setupPermissionScreen() {
        setContent {
            PineappleTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showPermissionScreen) {
                        PermissionScreen(
                            onRequestPermissions = {
                                requestPermissions()
                            },
                            onOpenSettings = {
                                permissionManager.openAppSettings()
                            }
                        )
                    } else if (hasPermissions) {
                        initializeMainApp()
                    }
                }
            }
        }
    }
    
    private fun requestPermissions() {
        permissionManager.requestAllPermissions { granted ->
            if (granted) {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                hasPermissions = true
                showPermissionScreen = false
                initializeApp()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun initializeApp() {
        setContent {
            PineappleTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showPermissionScreen) {
                        PermissionScreen(
                            onRequestPermissions = {
                                requestPermissions()
                            },
                            onOpenSettings = {
                                permissionManager.openAppSettings()
                            }
                        )
                    } else if (hasPermissions) {
                        initializeMainApp()
                    }
                }
            }
        }
    }
    
    @Composable
    private fun initializeMainApp() {
        // 检查是否已有数据来决定起始页面
        var startDestination = "directory_selection"
        
        lifecycleScope.launch {
            try {
                val collections = videoRepository.getAllCollections().first()
                if (collections.isNotEmpty()) {
                    startDestination = "main"
                }
            } catch (e: Exception) {
                // 如果出错，保持默认的目录选择页面
            }
        }
        
        PineappleTVNavigation(startDestination = startDestination)
    }
}