package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.RadioApplication
import com.example.model.FilterCriteria
import com.example.model.PlaybackState
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FilterBottomSheet
import com.example.ui.screens.GlobeScreen
import com.example.ui.screens.RecentScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

enum class NavigationTab(val title: String) {
    GLOBE("Globe"),
    SEARCH("Search"),
    FAVORITES("Favorites"),
    RECENT("Recent"),
    SETTINGS("Settings")
}

@Composable
fun RadioApp(
    isDarkTheme: Boolean?,
    onThemeChange: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = RadioApplication.instance
    val repository = app.repository
    val playerManager = app.playerManager
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(NavigationTab.GLOBE) }
    var filterCriteria by remember { mutableStateOf(FilterCriteria()) }
    var isFilterOpen by remember { mutableStateOf(false) }
    var isFullScreenPlayerOpen by remember { mutableStateOf(false) }

    // Reactive streams
    val playbackState by playerManager.playbackState.collectAsState()
    val volume by playerManager.volume.collectAsState()
    val favorites by repository.favorites.collectAsState(initial = emptyList())
    val recents by repository.recents.collectAsState(initial = emptyList())

    // Globe stations
    val globeStations by remember(filterCriteria) {
        repository.getGlobeStations(filterCriteria)
    }.collectAsState(initial = emptyList())

    val isMiniPlayerVisible = playbackState !is PlaybackState.Idle

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Floating Mini Player above Bottom Bar
                AnimatedVisibility(
                    visible = isMiniPlayerVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onPlayPauseClick = { playerManager.togglePlayPause() },
                        onExpandClick = { isFullScreenPlayerOpen = true }
                    )
                }

                // Bottom Navigation
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.GLOBE,
                        onClick = { currentTab = NavigationTab.GLOBE },
                        icon = { Icon(imageVector = Icons.Default.Public, contentDescription = "Globe") },
                        label = { Text("Globe") },
                        modifier = Modifier.testTag("tab_globe")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.SEARCH,
                        onClick = { currentTab = NavigationTab.SEARCH },
                        icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        modifier = Modifier.testTag("tab_search")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.FAVORITES,
                        onClick = { currentTab = NavigationTab.FAVORITES },
                        icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        modifier = Modifier.testTag("tab_favorites")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.RECENT,
                        onClick = { currentTab = NavigationTab.RECENT },
                        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Recent") },
                        label = { Text("Recent") },
                        modifier = Modifier.testTag("tab_recent")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.SETTINGS,
                        onClick = { currentTab = NavigationTab.SETTINGS },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                NavigationTab.GLOBE -> {
                    GlobeScreen(
                        stations = globeStations,
                        playbackState = playbackState,
                        favorites = favorites,
                        filterCriteria = filterCriteria,
                        onPlayStation = { station -> playerManager.play(station) },
                        onToggleFavorite = { station ->
                            coroutineScope.launch { repository.toggleFavorite(station) }
                        },
                        onOpenSearch = { currentTab = NavigationTab.SEARCH },
                        onOpenFilter = { isFilterOpen = true }
                    )
                }
                NavigationTab.SEARCH -> {
                    SearchScreen(
                        playbackState = playbackState,
                        favorites = favorites,
                        recents = recents,
                        onSearch = { query, criteria ->
                            repository.searchStations(query, criteria)
                        },
                        onPlayStation = { station -> playerManager.play(station) },
                        onToggleFavorite = { station ->
                            coroutineScope.launch { repository.toggleFavorite(station) }
                        },
                        onOpenFilter = { isFilterOpen = true },
                        onSeeAllRecents = { currentTab = NavigationTab.RECENT }
                    )
                }
                NavigationTab.FAVORITES -> {
                    FavoritesScreen(
                        favorites = favorites,
                        playbackState = playbackState,
                        onPlayStation = { station -> playerManager.play(station) },
                        onToggleFavorite = { station ->
                            coroutineScope.launch { repository.toggleFavorite(station) }
                        }
                    )
                }
                NavigationTab.RECENT -> {
                    RecentScreen(
                        recents = recents,
                        favorites = favorites,
                        playbackState = playbackState,
                        onPlayStation = { station -> playerManager.play(station) },
                        onToggleFavorite = { station ->
                            coroutineScope.launch { repository.toggleFavorite(station) }
                        },
                        onClearHistory = {
                            coroutineScope.launch { repository.clearRecentStations() }
                        }
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange,
                        onClearCache = { repository.clearCache() }
                    )
                }
            }
        }
    }

    // Full Screen Player Modal Sheet
    if (isFullScreenPlayerOpen) {
        val currentStation = when (playbackState) {
            is PlaybackState.Playing -> (playbackState as PlaybackState.Playing).station
            is PlaybackState.Paused -> (playbackState as PlaybackState.Paused).station
            is PlaybackState.Loading -> (playbackState as PlaybackState.Loading).station
            is PlaybackState.Error -> (playbackState as PlaybackState.Error).station
            PlaybackState.Idle -> null
        }
        val isFav = currentStation != null && favorites.any { it.stationUuid == currentStation.stationUuid }

        FullScreenPlayer(
            playbackState = playbackState,
            isFavorite = isFav,
            volume = volume,
            onVolumeChange = { playerManager.setVolume(it) },
            onPlayPauseClick = { playerManager.togglePlayPause() },
            onFavoriteClick = {
                currentStation?.let { st ->
                    coroutineScope.launch { repository.toggleFavorite(st) }
                }
            },
            onRetryClick = { playerManager.retry() },
            onExploreRegionClick = { region ->
                filterCriteria = filterCriteria.copy(country = region)
                currentTab = NavigationTab.GLOBE
            },
            onDismiss = { isFullScreenPlayerOpen = false }
        )
    }

    // Filter Modal Sheet
    if (isFilterOpen) {
        FilterBottomSheet(
            currentFilter = filterCriteria,
            onApply = { newCriteria ->
                filterCriteria = newCriteria
            },
            onDismiss = { isFilterOpen = false }
        )
    }
}
