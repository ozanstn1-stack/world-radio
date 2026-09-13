package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.globe.CameraTarget
import com.example.globe.GlobeCanvas
import com.example.model.FilterCriteria
import com.example.model.PlaybackState
import com.example.model.RadioStation
import com.example.model.StationCluster
import com.example.model.StationGenre
import com.example.ui.components.StationItem
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobeScreen(
    stations: List<RadioStation>,
    playbackState: PlaybackState,
    favorites: List<RadioStation>,
    filterCriteria: FilterCriteria,
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStation by remember { mutableStateOf<RadioStation?>(null) }
    var selectedCluster by remember { mutableStateOf<StationCluster?>(null) }
    var cameraTarget by remember { mutableStateOf<CameraTarget?>(null) }

    val playingStation = when (playbackState) {
        is PlaybackState.Playing -> playbackState.station
        is PlaybackState.Paused -> playbackState.station
        is PlaybackState.Loading -> playbackState.station
        is PlaybackState.Error -> playbackState.station
        PlaybackState.Idle -> null
    }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        cameraTarget = CameraTarget(
                            lat = loc.latitude,
                            lon = loc.longitude,
                            radiusDp = 280f
                        )
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Interactive 3D Globe
        GlobeCanvas(
            stations = stations,
            selectedStation = selectedStation,
            playingStation = playingStation,
            cameraTarget = cameraTarget,
            onStationSelected = { station ->
                selectedStation = station
                cameraTarget = CameraTarget(
                    lat = station.latitude ?: 0.0,
                    lon = station.longitude ?: 0.0
                )
            },
            onClusterSelected = { cluster ->
                selectedCluster = cluster
                cameraTarget = CameraTarget(
                    lat = cluster.centerLat,
                    lon = cluster.centerLon
                )
            }
        )

        // 2. Top Bar (Search + Filter + Quick Genre Indicators)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search stations",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = if (filterCriteria.genre.isNotBlank()) "Genre: ${filterCriteria.genre}"
                        else if (filterCriteria.country.isNotBlank()) "Country: ${filterCriteria.country}"
                        else "Explore ${stations.size} World Stations...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    )

                    BadgedBox(
                        badge = {
                            val activeFilterCount = (if (filterCriteria.genre.isNotBlank()) 1 else 0) +
                                    (if (filterCriteria.country.isNotBlank()) 1 else 0) +
                                    (if (filterCriteria.minBitrate > 0) 1 else 0)
                            if (activeFilterCount > 0) {
                                Badge { Text("$activeFilterCount") }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenFilter,
                            modifier = Modifier.testTag("globe_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Genre Color Legend bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StationGenre.entries.forEach { genre ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(genre.color)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = genre.displayName.split("/")[0].trim(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // 3. Floating Controls on Right Side (Zoom In, Zoom Out, My Location)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // My Location
            FloatingActionButton(
                onClick = {
                    val hasFine = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFine) {
                        try {
                            val fused = LocationServices.getFusedLocationProviderClient(context)
                            fused.lastLocation.addOnSuccessListener { loc ->
                                if (loc != null) {
                                    cameraTarget = CameraTarget(
                                        lat = loc.latitude,
                                        lon = loc.longitude,
                                        radiusDp = 320f
                                    )
                                }
                            }
                        } catch (_: SecurityException) {}
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.size(44.dp).testTag("my_location_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Recenter Globe
            FloatingActionButton(
                onClick = {
                    cameraTarget = CameraTarget(lat = 30.0, lon = 20.0, radiusDp = 180f)
                },
                modifier = Modifier.size(44.dp).testTag("recenter_globe_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Recenter Globe",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 4. Station Preview Card (pops up at bottom when marker clicked)
        AnimatedVisibility(
            visible = selectedStation != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 14.dp, end = 14.dp)
        ) {
            selectedStation?.let { st ->
                val isPlaying = playingStation?.stationUuid == st.stationUuid && playbackState is PlaybackState.Playing
                val isLoading = playingStation?.stationUuid == st.stationUuid && playbackState is PlaybackState.Loading
                val isFav = favorites.any { it.stationUuid == st.stationUuid }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Station on Globe",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = st.genre.color,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            IconButton(
                                onClick = { selectedStation = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close preview",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        StationItem(
                            station = st,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            isFavorite = isFav,
                            onPlayClick = { onPlayStation(st) },
                            onFavoriteClick = { onToggleFavorite(st) },
                            onItemClick = { onPlayStation(st) }
                        )
                    }
                }
            }
        }

        // 5. Cluster Stations BottomSheet
        selectedCluster?.let { cluster ->
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { selectedCluster = null },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "${cluster.count} Stations in this Region",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().height(320.dp)
                    ) {
                        items(cluster.stations, key = { it.stationUuid }) { st ->
                            val isPlaying = playingStation?.stationUuid == st.stationUuid && playbackState is PlaybackState.Playing
                            val isLoading = playingStation?.stationUuid == st.stationUuid && playbackState is PlaybackState.Loading
                            val isFav = favorites.any { it.stationUuid == st.stationUuid }

                            StationItem(
                                station = st,
                                isPlaying = isPlaying,
                                isLoading = isLoading,
                                isFavorite = isFav,
                                onPlayClick = { onPlayStation(st) },
                                onFavoriteClick = { onToggleFavorite(st) },
                                onItemClick = {
                                    selectedCluster = null
                                    selectedStation = st
                                    onPlayStation(st)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
