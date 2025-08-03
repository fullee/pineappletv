package app.pineappletv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.pineappletv.ui.screen.CollectionManagementScreen
import app.pineappletv.ui.screen.FolderBrowserScreen
import app.pineappletv.ui.screen.MainScreen
import app.pineappletv.ui.screen.PlayerScreen
import app.pineappletv.ui.screen.SearchScreen
import app.pineappletv.ui.screen.VideoListScreen

@Composable
fun PineappleTVNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "collection_management_initial"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 首次启动的合集管理界面
        composable("collection_management_initial") {
            CollectionManagementScreen(
                onBackClick = {
                    // 首次启动时不允许返回，退出应用
                },
                onAddCollectionClick = {
                    navController.navigate("folder_browser")
                },
                isInitialSetup = true,
                onSetupComplete = {
                    navController.navigate("main") {
                        popUpTo("collection_management_initial") { inclusive = true }
                    }
                }
            )
        }
        
        composable("main") {
            MainScreen(
                onCollectionClick = { collectionId ->
                    navController.navigate("video_list/$collectionId")
                },
                onVideoClick = { videoId ->
                    navController.navigate("player/$videoId")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onDirectorySelectionClick = {

                },
                onSettingsClick = {
                    navController.navigate("collection_management")
                }
            )
        }
        
        composable("video_list/{collectionId}") { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
            VideoListScreen(
                collectionId = collectionId,
                onBackClick = {
                    navController.popBackStack()
                },
                onVideoClick = { videoId ->
                    navController.navigate("player/$videoId")
                }
            )
        }
        
        composable("search") {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onVideoClick = { videoId ->
                    navController.navigate("player/$videoId")
                }
            )
        }
        
        composable("player/{videoId}") { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")?.toLongOrNull() ?: 0L
            PlayerScreen(
                videoId = videoId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("collection_management") {
            CollectionManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddCollectionClick = {
                    navController.navigate("folder_browser")
                }
            )
        }
        
        composable("folder_browser") {
            FolderBrowserScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFolderSelected = {
                    // 选择完成后返回上一个页面（可能是初始设置或普通合集管理）
                    navController.popBackStack()
                }
            )
        }
    }
}