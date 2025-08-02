package app.pineappletv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.pineappletv.ui.screen.*

@Composable
fun PineappleTVNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "directory_selection"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("directory_selection") {
            DirectorySelectionScreen(
                onDirectorySelected = {
                    navController.navigate("main") {
                        popUpTo("directory_selection") { inclusive = true }
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
    }
}