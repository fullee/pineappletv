package app.pineappletv.di

import app.pineappletv.data.database.DatabaseManager
import app.pineappletv.data.repository.VideoRepository
import app.pineappletv.data.scanner.VideoScanner
import app.pineappletv.ui.viewmodel.CollectionManagementViewModel
import app.pineappletv.ui.viewmodel.DirectorySelectionViewModel
import app.pineappletv.ui.viewmodel.FolderBrowserViewModel
import app.pineappletv.ui.viewmodel.MainViewModel
import app.pineappletv.ui.viewmodel.PlayerViewModel
import app.pineappletv.ui.viewmodel.SearchViewModel
import app.pineappletv.ui.viewmodel.VideoListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    
    // Database
    single { DatabaseManager(androidContext()) }
    
    // Scanner
    single { VideoScanner(androidContext()) }
    
    // Repository
    single { VideoRepository(get(), get()) }
    
    // ViewModels
    viewModel { DirectorySelectionViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { VideoListViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
    viewModel { CollectionManagementViewModel(get()) }
    viewModel { FolderBrowserViewModel(get()) }
}