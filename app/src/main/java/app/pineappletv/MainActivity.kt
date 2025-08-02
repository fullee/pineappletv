package app.pineappletv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.ui.navigation.PineappleTVNavigation
import app.pineappletv.ui.theme.PineappleTVTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val videoRepository: VideoRepository by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PineappleTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                        
                        setContent {
                            PineappleTVTheme {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    PineappleTVNavigation(startDestination = startDestination)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}