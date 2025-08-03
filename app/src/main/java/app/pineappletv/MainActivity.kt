package app.pineappletv

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        var startDestination by remember { mutableStateOf("collection_management_initial") }
        var isCheckingData by remember { mutableStateOf(true) }
        
        LaunchedEffect(Unit) {
            try {
                val collections = videoRepository.getAllCollections().first()
                startDestination = if (collections.isEmpty()) {
                    "collection_management_initial"
                } else {
                    "main"
                }
            } catch (e: Exception) {
                // 如果出错，默认显示初始合集管理界面
                startDestination = "collection_management_initial"
            } finally {
                isCheckingData = false
            }
        }
        
        if (isCheckingData) {
            // 显示加载中界面
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在初始化...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            PineappleTVNavigation(startDestination = startDestination)
        }
    }
}