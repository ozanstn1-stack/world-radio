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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
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
import com.example.globe.WorldMapView
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
    var isFlatMapMode by remember { mutableStateOf(true) } // Default to 1:1 real world map
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
                            radiusDp = 350f
                        )
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Interactive 1:1 Real World Map or 3D Globe
        if (isFlatMapMode) {
            WorldMapView(
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
        } else {
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
        }

        // 2. Top Bar (Search + View Switcher + Filter + Regions + Genre Legend)
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
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        text = if (filterCriteria.genre.isNotBlank()) "Tür: ${filterCriteria.genre}"
                        else if (filterCriteria.country.isNotBlank()) "Ülke: ${filterCriteria.country}"
                        else "${stations.size} Canlı Radyo Yayını",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    )

                    // Toggle Map/Globe Mode
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { isFlatMapMode = !isFlatMapMode }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isFlatMapMode) Icons.Default.Public else Icons.Default.Map,
                                contentDescription = "Mod Değiştir",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFlatMapMode) "3B Küre" else "Harita",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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

            // Quick Region Shortcuts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val regions = listOf(
                    "🇹🇷 Türkiye" to (39.0 to 35.0),
                    "🇪🇺 Avrupa" to (50.0 to 15.0),
                    "🇺🇸 Amerika" to (38.0 to -98.0),
                    "🌏 Asya" to (35.0 to 105.0),
                    "🌍 Afrika" to (5.0 to 20.0),
                    "🇦🇺 Okyanusya" to (-25.0 to 135.0)
                )
                regions.forEach { (label, coords) ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clip(CircleShape).clickable {
                            cameraTarget = CameraTarget(lat = coords.first, lon = coords.second, radiusDp = 350f)
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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

        // 3. Floating Controls on Right Side (Zoom In, Zoom Out, Turkey Jump, View Mode, Location, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Zoom In
            FloatingActionButton(
                onClick = {
                    val currentR = cameraTarget?.radiusDp ?: 250f
                    cameraTarget = CameraTarget(
                        lat = cameraTarget?.lat ?: 39.0,
                        lon = cameraTarget?.lon ?: 35.0,
                        radiusDp = (currentR * 1.4f).coerceIn(140f, 650f)
                    )
                },
                modifier = Modifier.size(40.dp).testTag("zoom_in_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom Out
            FloatingActionButton(
                onClick = {
                    val currentR = cameraTarget?.radiusDp ?: 250f
                    cameraTarget = CameraTarget(
                        lat = cameraTarget?.lat ?: 39.0,
                        lon = cameraTarget?.lon ?: 35.0,
                        radiusDp = (currentR * 0.7f).coerceIn(140f, 650f)
                    )
                },
                modifier = Modifier.size(40.dp).testTag("zoom_out_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Quick Turkey focus
            FloatingActionButton(
                onClick = {
                    cameraTarget = CameraTarget(lat = 39.0, lon = 35.0, radiusDp = 380f)
                },
                modifier = Modifier.size(40.dp).testTag("turkey_focus_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Text(
                    text = "TR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

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
                                        radiusDp = 350f
                                    )
                                }
                            }
                        } catch (_: SecurityException) {}
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.size(40.dp).testTag("my_location_button"),
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

            // Recenter Globe / Map
            FloatingActionButton(
                onClick = {
                    cameraTarget = CameraTarget(lat = 30.0, lon = 20.0, radiusDp = 180f)
                },
                modifier = Modifier.size(40.dp).testTag("recenter_globe_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Recenter",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
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
