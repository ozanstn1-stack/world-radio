package com.example.globe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.model.StationGenre
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

enum class GoogleMapTheme(val label: String) {
    DARK("Gece Modu"),
    SATELLITE("Uydu"),
    TERRAIN("Arazi"),
    NORMAL("Klasik")
}

private const val GOOGLE_MAPS_DARK_STYLE_JSON = """
[
  { "elementType": "geometry", "stylers": [{ "color": "#0d131f" }] },
  { "elementType": "labels.text.stroke", "stylers": [{ "color": "#0d131f" }] },
  { "elementType": "labels.text.fill", "stylers": [{ "color": "#94a3b8" }] },
  { "featureType": "administrative.country", "elementType": "geometry.stroke", "stylers": [{ "color": "#334155" }] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#e2e8f0" }] },
  { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
  { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#1e293b" }] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#0f172a" }] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [{ "color": "#293548" }] },
  { "featureType": "transit", "stylers": [{ "visibility": "off" }] },
  { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#060911" }] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#38bdf8" }] }
]
"""

@Composable
fun GoogleMapView(
    stations: List<RadioStation>,
    selectedStation: RadioStation?,
    playingStation: RadioStation?,
    cameraTarget: CameraTarget?,
    onStationSelected: (RadioStation) -> Unit,
    onPlayStation: ((RadioStation) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Default world center: Ankara/Mediterranean overview
    val initialLatLng = remember {
        if (selectedStation?.latitude != null && selectedStation.longitude != null) {
            LatLng(selectedStation.latitude, selectedStation.longitude)
        } else {
            LatLng(39.0, 35.0)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 3.5f)
    }

    var currentTheme by remember { mutableStateOf(GoogleMapTheme.DARK) }
    var showThemeMenu by remember { mutableStateOf(false) }

    // Map properties according to selected theme
    val mapProperties = remember(currentTheme) {
        when (currentTheme) {
            GoogleMapTheme.DARK -> MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = MapStyleOptions(GOOGLE_MAPS_DARK_STYLE_JSON),
                isBuildingEnabled = false
            )
            GoogleMapTheme.SATELLITE -> MapProperties(
                mapType = MapType.SATELLITE,
                mapStyleOptions = null,
                isBuildingEnabled = false
            )
            GoogleMapTheme.TERRAIN -> MapProperties(
                mapType = MapType.TERRAIN,
                mapStyleOptions = null,
                isBuildingEnabled = false
            )
            GoogleMapTheme.NORMAL -> MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = null,
                isBuildingEnabled = false
            )
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    // Camera animation when cameraTarget changes
    LaunchedEffect(cameraTarget?.token) {
        cameraTarget?.let { target ->
            val targetZoom = when {
                target.radiusDp != null && target.radiusDp > 300f -> 8.5f
                target.radiusDp != null && target.radiusDp > 200f -> 6.5f
                else -> 7.0f
            }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(target.lat, target.lon),
                    targetZoom
                ),
                durationMs = 900
            )
        }
    }

    // Cache bitmap descriptors per genre to keep map rendering fast
    val markerDescriptorCache = remember { mutableMapOf<String, BitmapDescriptor>() }

    fun getMarkerDescriptor(
        genre: StationGenre,
        isPlaying: Boolean,
        isSelected: Boolean
    ): BitmapDescriptor {
        val key = "${genre.name}_${isPlaying}_${isSelected}"
        return markerDescriptorCache.getOrPut(key) {
            createMarkerBitmap(context, genre.color.toArgb(), isPlaying, isSelected)
        }
    }

    // Filter valid geo stations
    val geoStations = remember(stations) {
        stations.filter { it.latitude != null && it.longitude != null }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("google_map_container"),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = {
                // Map background click deselects
            }
        ) {
            // Render markers for all valid stations
            geoStations.forEach { station ->
                val lat = station.latitude ?: return@forEach
                val lon = station.longitude ?: return@forEach
                val isSelected = station.stationUuid == selectedStation?.stationUuid
                val isPlaying = station.stationUuid == playingStation?.stationUuid

                val markerState = remember(station.stationUuid, lat, lon) {
                    MarkerState(position = LatLng(lat, lon))
                }

                val icon = remember(station.genre, isPlaying, isSelected) {
                    getMarkerDescriptor(station.genre, isPlaying, isSelected)
                }

                Marker(
                    state = markerState,
                    title = station.name,
                    snippet = "${station.genre.displayName} • ${station.locationTitle}",
                    icon = icon,
                    zIndex = if (isPlaying) 10f else if (isSelected) 8f else 1f,
                    onClick = {
                        onStationSelected(station)
                        true
                    }
                )
            }
        }

        // Overlay Map Controls (Zoom, Map Style, Center World)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Map Layers / Theme Switcher
            SmallFloatingActionButton(
                onClick = { showThemeMenu = !showThemeMenu },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.testTag("map_theme_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Harita Katmanları",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom In (+)
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
                modifier = Modifier.testTag("map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yakınlaş", modifier = Modifier.size(20.dp))
            }

            // Zoom Out (-)
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
                modifier = Modifier.testTag("map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Uzaklaş", modifier = Modifier.size(20.dp))
            }

            // Global Overview (Reset to world center)
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(25.0, 20.0), 2.5f)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
                modifier = Modifier.testTag("map_reset_overview")
            ) {
                Icon(Icons.Default.Public, contentDescription = "Tüm Dünya", modifier = Modifier.size(20.dp))
            }
        }

        // Map Layers Popup Menu
        AnimatedVisibility(
            visible = showThemeMenu,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 }),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 64.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.width(170.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Harita Görünümü",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    GoogleMapTheme.values().forEach { theme ->
                        val isSelected = theme == currentTheme
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    currentTheme = theme
                                    showThemeMenu = false
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = when (theme) {
                                    GoogleMapTheme.DARK -> Icons.Default.DarkMode
                                    GoogleMapTheme.SATELLITE -> Icons.Default.Public
                                    GoogleMapTheme.TERRAIN -> Icons.Default.Terrain
                                    GoogleMapTheme.NORMAL -> Icons.Default.LightMode
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Active Station Banner at the Bottom of Map
        if (selectedStation != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .testTag("map_station_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Genre Badge
                    Surface(
                        shape = CircleShape,
                        color = selectedStation.genre.color.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = selectedStation.genre.symbol,
                                fontWeight = FontWeight.Bold,
                                color = selectedStation.genre.color,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedStation.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedStation.locationTitle} • ${selectedStation.genre.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (onPlayStation != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { onPlayStation(selectedStation) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Oynat",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates custom high-performance radio beacon bitmap markers for Google Maps.
 */
private fun createMarkerBitmap(
    context: Context,
    colorInt: Int,
    isPlaying: Boolean,
    isSelected: Boolean
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val baseRadius = if (isSelected) 18f else if (isPlaying) 15f else 11f
    val outerGlowRadius = baseRadius * 1.7f
    val canvasSize = (outerGlowRadius * 2 * density).toInt().coerceAtLeast(32)

    val bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val cx = canvasSize / 2f
    val cy = canvasSize / 2f

    // 1. Outer Glow / Ripple if playing or selected
    if (isPlaying || isSelected) {
        paint.color = colorInt
        paint.alpha = if (isPlaying) 70 else 100
        canvas.drawCircle(cx, cy, outerGlowRadius * density, paint)
    }

    // 2. White Border Ring
    paint.color = android.graphics.Color.WHITE
    paint.alpha = 240
    canvas.drawCircle(cx, cy, (baseRadius + 2.5f) * density, paint)

    // 3. Inner Genre Core
    paint.color = colorInt
    paint.alpha = 255
    canvas.drawCircle(cx, cy, baseRadius * density, paint)

    // 4. Center Dot
    paint.color = android.graphics.Color.WHITE
    paint.alpha = 230
    canvas.drawCircle(cx, cy, (baseRadius * 0.35f) * density, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
