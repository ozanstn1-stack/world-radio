package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterCriteria
import com.example.model.PlaybackState
import com.example.model.RadioStation
import com.example.ui.components.RecentlyPlayedSection
import com.example.ui.components.StationGridCard
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    playbackState: PlaybackState,
    favorites: List<RadioStation>,
    recents: List<RadioStation> = emptyList(),
    onSearch: suspend (String, FilterCriteria) -> List<RadioStation>,
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onOpenFilter: () -> Unit,
    onSeeAllRecents: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedGenreChip by remember { mutableStateOf("") }

    val quickGenres = listOf("Pop", "Rock", "Jazz", "Classical", "Electronic", "News", "Dance", "Chillout")
    val quickCities = listOf("London", "New York", "Istanbul", "Tokyo", "Paris", "Berlin")

    val playingStation = when (playbackState) {
        is PlaybackState.Playing -> playbackState.station
        is PlaybackState.Paused -> playbackState.station
        is PlaybackState.Loading -> playbackState.station
        is PlaybackState.Error -> playbackState.station
        PlaybackState.Idle -> null
    }

    // Debounced search query (350ms delay) to avoid unnecessary Radio Browser API requests
    LaunchedEffect(query, selectedGenreChip) {
        isSearching = true
        delay(350)
        val criteria = FilterCriteria(genre = selectedGenreChip)
        searchResults = onSearch(query, criteria)
        isSearching = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input"),
            placeholder = { Text("Search station, city, country, or genre...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = onOpenFilter) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filters")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recently Played horizontal quick-play section when no search query is active
        if (query.isBlank() && selectedGenreChip.isBlank() && recents.isNotEmpty()) {
            RecentlyPlayedSection(
                recents = recents,
                playbackState = playbackState,
                onPlayStation = onPlayStation,
                onSeeAllClick = onSeeAllRecents
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Quick Genre Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickGenres.forEach { genre ->
                val isSelected = selectedGenreChip.equals(genre, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedGenreChip = if (isSelected) "" else genre
                    },
                    label = { Text(genre) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick City Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickCities.forEach { city ->
                val isSelected = query.equals(city, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        query = if (isSelected) "" else city
                    },
                    label = { Text("📍 $city") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content / Grid / Loading
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (query.isNotBlank() || selectedGenreChip.isNotBlank()) "No stations found matching your query"
                        else "Type something or choose a tag to discover stations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "${searchResults.size} Stations Found",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // LazyVerticalGrid displaying radio stations in adaptive grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("stations_vertical_grid")
            ) {
                items(searchResults, key = { it.stationUuid }) { station ->
                    val isPlaying = playingStation?.stationUuid == station.stationUuid && playbackState is PlaybackState.Playing
                    val isLoading = playingStation?.stationUuid == station.stationUuid && playbackState is PlaybackState.Loading
                    val isFav = favorites.any { it.stationUuid == station.stationUuid }

                    StationGridCard(
                        station = station,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        isFavorite = isFav,
                        onPlayClick = { onPlayStation(station) },
                        onFavoriteClick = { onToggleFavorite(station) },
                        onItemClick = { onPlayStation(station) }
                    )
                }
            }
        }
    }
}

