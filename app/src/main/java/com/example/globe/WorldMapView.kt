package com.example.globe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.model.StationCluster
import com.example.model.StationGenre
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

/**
 * 1:1 Interactive 2D Real World Map (Equirectangular / Mercator projection).
 * Provides precise, natural finger tracking, smooth zoom, realistic coastlines,
 * landmark cities, and interactive radio beacon clusters.
 */
@Composable
fun WorldMapView(
    stations: List<RadioStation>,
    selectedStation: RadioStation?,
    playingStation: RadioStation?,
    cameraTarget: CameraTarget?,
    onStationSelected: (RadioStation) -> Unit,
    onClusterSelected: (StationCluster) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    // Map offset in pixels from center
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }
    val animZoom = remember { Animatable(1.0f) }

    // Pulse animation for playing station
    val pulseAnim = remember { Animatable(0f) }
    LaunchedEffect(playingStation != null) {
        while (true) {
            pulseAnim.animateTo(1f, animationSpec = tween(1400))
            pulseAnim.snapTo(0f)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val baseMapWidth = widthPx * 1.4f
        val baseMapHeight = baseMapWidth * 0.55f

        val currentZoom = if (animZoom.isRunning) animZoom.value else zoomScale
        val mapW = baseMapWidth * currentZoom
        val mapH = baseMapHeight * currentZoom

        val currentOffsetX = if (animOffsetX.isRunning) animOffsetX.value else offsetX
        val currentOffsetY = if (animOffsetY.isRunning) animOffsetY.value else offsetY

        val centerX = widthPx / 2f + currentOffsetX
        val centerY = heightPx / 2f + currentOffsetY

        // Handle camera navigation requests (e.g. focusing a station or region)
        LaunchedEffect(cameraTarget) {
            if (cameraTarget != null) {
                val targetZoom = if (cameraTarget.radiusDp != null) {
                    (cameraTarget.radiusDp / 170f).coerceIn(1.0f, 6.0f)
                } else {
                    max(currentZoom, 2.5f)
                }

                val targetMapW = baseMapWidth * targetZoom
                val targetMapH = baseMapHeight * targetZoom

                // Convert target lat/lon to offsets so target is centered at (widthPx/2, heightPx/2)
                val targetNormX = (cameraTarget.lon.toFloat() + 180f) / 360f
                val targetNormY = (90f - cameraTarget.lat.toFloat()) / 180f

                val newTargetOffX = (0.5f - targetNormX) * targetMapW
                val newTargetOffY = (0.5f - targetNormY) * targetMapH

                launch {
                    animZoom.animateTo(targetZoom, tween(600))
                }
                launch {
                    animOffsetX.animateTo(newTargetOffX, tween(600))
                }
                launch {
                    animOffsetY.animateTo(newTargetOffY, tween(600))
                }
                zoomScale = targetZoom
                offsetX = newTargetOffX
                offsetY = newTargetOffY
            }
        }

        // Project stations into map screen coordinates
        val visibleStationPoints = remember(stations, centerX, centerY, mapW, mapH, currentZoom) {
            val left = centerX - mapW / 2f
            val top = centerY - mapH / 2f

            stations.mapNotNull { st ->
                val lat = st.latitude ?: return@mapNotNull null
                val lon = st.longitude ?: return@mapNotNull null

                val normX = (lon.toFloat() + 180f) / 360f
                val normY = (90f - lat.toFloat()) / 180f

                val sx = left + normX * mapW
                val sy = top + normY * mapH

                if (sx >= -100f && sx <= widthPx + 100f && sy >= -100f && sy <= heightPx + 100f) {
                    MapPoint(station = st, screenX = sx, screenY = sy)
                } else null
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Smooth, 1:1 direct finger tracking with no runaway speed
                        val newZoom = (zoomScale * zoom).coerceIn(0.85f, 7.5f)
                        val zoomFactor = newZoom / zoomScale

                        // Direct 1:1 pan
                        val newOffX = offsetX * zoomFactor + pan.x
                        val newOffY = offsetY * zoomFactor + pan.y

                        zoomScale = newZoom
                        offsetX = newOffX
                        offsetY = newOffY

                        coroutineScope.launch {
                            animZoom.snapTo(zoomScale)
                            animOffsetX.snapTo(offsetX)
                            animOffsetY.snapTo(offsetY)
                        }
                    }
                }
                .pointerInput(visibleStationPoints) {
                    detectTapGestures { tapOffset ->
                        val hitRadius = 24f * density.density
                        val hitRadiusSq = hitRadius * hitRadius
                        var tappedStation: RadioStation? = null

                        for (pt in visibleStationPoints) {
                            val dx = pt.screenX - tapOffset.x
                            val dy = pt.screenY - tapOffset.y
                            if (dx * dx + dy * dy < hitRadiusSq) {
                                tappedStation = pt.station
                                break
                            }
                        }

                        if (tappedStation != null) {
                            onStationSelected(tappedStation)
                        }
                    }
                }
        ) {
            val mapLeft = centerX - mapW / 2f
            val mapTop = centerY - mapH / 2f
            val mapRight = mapLeft + mapW
            val mapBottom = mapTop + mapH

            // 1. Ocean Background Canvas
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07111E),
                        Color(0xFF0C1B2E),
                        Color(0xFF081424)
                    )
                )
            )

            // Ocean Realm Map Container
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF132F4C),
                        Color(0xFF0D2137),
                        Color(0xFF091626)
                    ),
                    center = Offset(centerX, centerY),
                    radius = mapW * 0.7f
                ),
                topLeft = Offset(mapLeft, mapTop),
                size = androidx.compose.ui.geometry.Size(mapW, mapH)
            )

            // 2. Graticule Grid Lines (Equator, Tropics, Prime Meridian)
            drawWorldGrid(mapLeft, mapTop, mapW, mapH, textMeasurer)

            // 3. World Continents & High-Fidelity Coastlines
            drawDetailedContinents(mapLeft, mapTop, mapW, mapH)

            // 4. Reference Landmark Cities
            drawCityLandmarks(mapLeft, mapTop, mapW, mapH, currentZoom, textMeasurer, density.density)

            // 5. Radio Station Beacon Markers
            drawStationMarkers(
                points = visibleStationPoints,
                selectedStation = selectedStation,
                playingStation = playingStation,
                pulseProgress = pulseAnim.value,
                density = density.density,
                zoom = currentZoom,
                textMeasurer = textMeasurer
            )

            // Map Border Frame
            drawRect(
                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                topLeft = Offset(mapLeft, mapTop),
                size = androidx.compose.ui.geometry.Size(mapW, mapH),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

private data class MapPoint(
    val station: RadioStation,
    val screenX: Float,
    val screenY: Float
)

private fun DrawScope.drawWorldGrid(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    textMeasurer: TextMeasurer
) {
    val gridColor = Color(0xFF38BDF8).copy(alpha = 0.14f)
    val axisColor = Color(0xFF38BDF8).copy(alpha = 0.35f)

    // Parallels (every 30 degrees: -60, -30, 0, 30, 60)
    for (lat in -60..60 step 30) {
        val normY = (90f - lat) / 180f
        val y = top + normY * height
        val isEquator = lat == 0
        drawLine(
            color = if (isEquator) axisColor else gridColor,
            start = Offset(left, y),
            end = Offset(left + width, y),
            strokeWidth = if (isEquator) 1.5f else 1f
        )
    }

    // Meridians (every 45 degrees: -180, -135, -90, -45, 0, 45, 90, 135, 180)
    for (lon in -180..180 step 45) {
        val normX = (lon + 180f) / 360f
        val x = left + normX * width
        val isPrime = lon == 0
        drawLine(
            color = if (isPrime) axisColor else gridColor,
            start = Offset(x, top),
            end = Offset(x, top + height),
            strokeWidth = if (isPrime) 1.5f else 1f
        )
    }
}

private fun DrawScope.drawDetailedContinents(
    left: Float,
    top: Float,
    width: Float,
    height: Float
) {
    val landFillColor = Color(0xFF1E385B)
    val coastColor = Color(0xFF38BDF8).copy(alpha = 0.85f)
    val borderOrIslandColor = Color(0xFF4FC3F7).copy(alpha = 0.90f)

    for (polygon in WorldContinentData.continents) {
        if (polygon.points.isEmpty()) continue

        val path = Path()
        var first = true

        for ((lat, lon) in polygon.points) {
            val normX = (lon.toFloat() + 180f) / 360f
            val normY = (90f - lat.toFloat()) / 180f
            val sx = left + normX * width
            val sy = top + normY * height

            if (first) {
                path.moveTo(sx, sy)
                first = false
            } else {
                path.lineTo(sx, sy)
            }
        }
        path.close()

        // Land fill
        drawPath(path = path, color = landFillColor, style = Fill)

        // Glowing coastline stroke
        drawPath(
            path = path,
            color = if (polygon.isBorderOrIsland) borderOrIslandColor else coastColor,
            style = Stroke(
                width = if (polygon.name.contains("Turkey")) 2.2f else 1.5f,
                cap = StrokeCap.Round
            )
        )
    }
}

private fun DrawScope.drawCityLandmarks(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    zoom: Float,
    textMeasurer: TextMeasurer,
    density: Float
) {
    // Only show landmark city labels when zoomed in slightly to keep clean
    if (zoom < 1.3f) return

    val labelColor = Color.White.copy(alpha = 0.75f)
    val dotColor = Color(0xFFFFD54F)

    for (city in WorldContinentData.landmarkCities) {
        val normX = (city.lon.toFloat() + 180f) / 360f
        val normY = (90f - city.lat.toFloat()) / 180f
        val sx = left + normX * width
        val sy = top + normY * height

        // Small yellow anchor dot
        drawCircle(
            color = dotColor,
            radius = 3.5f * density,
            center = Offset(sx, sy)
        )

        // City name text
        val textLayout = textMeasurer.measure(
            text = city.name,
            style = TextStyle(
                color = labelColor,
                fontSize = (10f * density).sp,
                fontWeight = FontWeight.Medium
            )
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(sx + 5f * density, sy - 6f * density)
        )
    }
}

private fun DrawScope.drawStationMarkers(
    points: List<MapPoint>,
    selectedStation: RadioStation?,
    playingStation: RadioStation?,
    pulseProgress: Float,
    density: Float,
    zoom: Float,
    textMeasurer: TextMeasurer
) {
    for (pt in points) {
        val st = pt.station
        val isPlaying = playingStation?.stationUuid == st.stationUuid
        val isSelected = selectedStation?.stationUuid == st.stationUuid
        val genreColor = st.genre.color

        val x = pt.screenX
        val y = pt.screenY

        val baseRadius = if (isPlaying || isSelected) 8f * density else 5f * density

        // Pulse radar rings for currently playing station
        if (isPlaying) {
            val pulseRadius = baseRadius + (22f * density * pulseProgress)
            val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.9f
            drawCircle(
                color = genreColor.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(x, y),
                style = Stroke(width = 2.5f * density)
            )
        }

        // Halo / selection backing
        if (isSelected || isPlaying) {
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = baseRadius + 4f * density,
                center = Offset(x, y)
            )
        }

        // Outer genre color dot
        drawCircle(
            color = genreColor,
            radius = baseRadius,
            center = Offset(x, y)
        )

        // Inner white beacon core
        drawCircle(
            color = Color.White,
            radius = baseRadius * 0.45f,
            center = Offset(x, y)
        )

        // Show station label when zoomed in or when selected/playing
        if (zoom >= 2.6f || isSelected || isPlaying) {
            val labelText = if (st.name.length > 18) st.name.take(16) + ".." else st.name
            val layout = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = if (isPlaying) Color(0xFFFFD54F) else Color.White,
                    fontSize = (9f * density).sp,
                    fontWeight = if (isPlaying || isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
            // Draw background pill for readability
            val pillPadding = 3f * density
            drawRoundRect(
                color = Color(0xDD0B1320),
                topLeft = Offset(x - layout.size.width / 2f - pillPadding, y - baseRadius - layout.size.height - pillPadding),
                size = androidx.compose.ui.geometry.Size(layout.size.width + pillPadding * 2, layout.size.height + pillPadding * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density, 4f * density)
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(x - layout.size.width / 2f, y - baseRadius - layout.size.height)
            )
        }
    }
}
