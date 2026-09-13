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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.model.StationCluster
import com.example.model.StationGenre
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class CameraTarget(
    val lat: Double,
    val lon: Double,
    val radiusDp: Float? = null,
    val token: Long = System.currentTimeMillis()
)

@Composable
fun GlobeCanvas(
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

    var camLat by remember { mutableFloatStateOf(30f) }
    var camLon by remember { mutableFloatStateOf(10f) }
    var globeRadiusDp by remember { mutableFloatStateOf(170f) }

    val animCamLat = remember { Animatable(30f) }
    val animCamLon = remember { Animatable(10f) }
    val animRadius = remember { Animatable(170f) }

    // Sync animation values
    LaunchedEffect(cameraTarget) {
        if (cameraTarget != null) {
            launch {
                animCamLat.animateTo(
                    cameraTarget.lat.toFloat().coerceIn(-80f, 80f),
                    animationSpec = tween(durationMillis = 900)
                )
            }
            launch {
                // Shortest angular turn
                var targetLon = cameraTarget.lon.toFloat()
                var currentLon = animCamLon.value
                var diff = (targetLon - currentLon) % 360f
                if (diff > 180f) diff -= 360f
                if (diff < -180f) diff += 360f

                animCamLon.animateTo(
                    currentLon + diff,
                    animationSpec = tween(durationMillis = 900)
                )
            }
            if (cameraTarget.radiusDp != null) {
                launch {
                    animRadius.animateTo(
                        cameraTarget.radiusDp.coerceIn(130f, 650f),
                        animationSpec = tween(durationMillis = 900)
                    )
                }
            }
        }
    }

    // Static starry background stars
    val stars = remember {
        val rand = Random(42)
        List(120) {
            Triple(rand.nextFloat(), rand.nextFloat(), rand.nextFloat() * 1.5f + 0.5f)
        }
    }

    // Pulse animation for playing station
    val pulseAnim = remember { Animatable(0f) }
    LaunchedEffect(playingStation != null) {
        while (true) {
            pulseAnim.animateTo(1f, animationSpec = tween(1500))
            pulseAnim.snapTo(0f)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f

        val currentLat = if (animCamLat.isRunning) animCamLat.value else camLat
        val currentLon = if (animCamLon.isRunning) animCamLon.value else camLon
        val currentRadiusDp = if (animRadius.isRunning) animRadius.value else globeRadiusDp
        val currentRadiusPx = with(density) { currentRadiusDp.dp.toPx() }

        // Compute clusters
        val clusters = remember(stations, currentLat, currentLon, currentRadiusPx) {
            ClusterEngine.clusterStations(
                stations = stations,
                camLat = currentLat.toDouble(),
                camLon = currentLon.toDouble(),
                centerX = centerX,
                centerY = centerY,
                radius = currentRadiusPx,
                densityDpi = density.density
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val sensitivity = 120f / currentRadiusPx
                        val newLat = (camLat + pan.y * sensitivity).coerceIn(-82f, 82f)
                        val newLon = (camLon - pan.x * sensitivity) % 360f

                        camLat = newLat
                        camLon = newLon
                        globeRadiusDp = (globeRadiusDp * zoom).coerceIn(130f, 650f)

                        coroutineScope.launch {
                            animCamLat.snapTo(camLat)
                            animCamLon.snapTo(camLon)
                            animRadius.snapTo(globeRadiusDp)
                        }
                    }
                }
                .pointerInput(clusters, currentLat, currentLon, currentRadiusPx) {
                    detectTapGestures { tapOffset ->
                        // Check if hit any cluster/marker
                        val hitRadiusPx = 28f * density.density
                        val hitRadiusSq = hitRadiusPx * hitRadiusPx
                        var tappedCluster: StationCluster? = null

                        for (cluster in clusters) {
                            val dx = cluster.screenX - tapOffset.x
                            val dy = cluster.screenY - tapOffset.y
                            if (dx * dx + dy * dy < hitRadiusSq) {
                                tappedCluster = cluster
                                break
                            }
                        }

                        if (tappedCluster != null) {
                            if (tappedCluster.isSingleStation) {
                                tappedCluster.singleStation?.let { onStationSelected(it) }
                            } else {
                                onClusterSelected(tappedCluster)
                            }
                        } else {
                            // Tapped globe: rotate towards tapped coordinates
                            val geo = GlobeMath.unproject(
                                touchX = tapOffset.x,
                                touchY = tapOffset.y,
                                camLat = currentLat.toDouble(),
                                camLon = currentLon.toDouble(),
                                centerX = centerX,
                                centerY = centerY,
                                radius = currentRadiusPx
                            )
                            if (geo != null) {
                                coroutineScope.launch {
                                    animCamLat.animateTo(geo.first.toFloat().coerceIn(-80f, 80f), tween(500))
                                }
                                coroutineScope.launch {
                                    var target = geo.second.toFloat()
                                    var curr = animCamLon.value
                                    var diff = (target - curr) % 360f
                                    if (diff > 180f) diff -= 360f
                                    if (diff < -180f) diff += 360f
                                    animCamLon.animateTo(curr + diff, tween(500))
                                }
                            }
                        }
                    }
                }
        ) {
            // 1. Draw Space Background & Stars
            drawSpaceBackground(widthPx, heightPx, stars)

            // 2. Atmosphere glow behind Earth
            drawAtmosphereGlow(centerX, centerY, currentRadiusPx)

            // 3. Globe Sphere Disc
            val spherePath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        centerX - currentRadiusPx,
                        centerY - currentRadiusPx,
                        centerX + currentRadiusPx,
                        centerY + currentRadiusPx
                    )
                )
            }

            // Clip all surface features to sphere disc
            clipPath(spherePath) {
                // Ocean base gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF133E68),
                            Color(0xFF0C2442),
                            Color(0xFF061426),
                            Color(0xFF020710)
                        ),
                        center = Offset(centerX - currentRadiusPx * 0.35f, centerY - currentRadiusPx * 0.35f),
                        radius = currentRadiusPx * 1.3f
                    ),
                    radius = currentRadiusPx,
                    center = Offset(centerX, centerY)
                )

                // Graticule grid (Parallels & Meridians)
                drawGraticule(
                    centerX, centerY, currentRadiusPx,
                    currentLat.toDouble(), currentLon.toDouble()
                )

                // Continents Landmasses & Coastlines
                drawContinents(
                    centerX, centerY, currentRadiusPx,
                    currentLat.toDouble(), currentLon.toDouble()
                )

                // 3D Rim Lighting / Shadow (terminator depth)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x22000000),
                            Color(0x88000511),
                            Color(0xCC000308)
                        ),
                        center = Offset(centerX + currentRadiusPx * 0.2f, centerY + currentRadiusPx * 0.2f),
                        radius = currentRadiusPx
                    ),
                    radius = currentRadiusPx,
                    center = Offset(centerX, centerY)
                )
            }

            // Globe Horizon border ring
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.65f),
                radius = currentRadiusPx,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.8f)
            )

            // 4. Station Clusters & Markers (drawn on top of the globe)
            drawStationClusters(
                clusters = clusters,
                selectedStation = selectedStation,
                playingStation = playingStation,
                pulseProgress = pulseAnim.value,
                textMeasurer = textMeasurer,
                density = density.density
            )
        }
    }
}

private fun DrawScope.drawSpaceBackground(
    widthPx: Float,
    heightPx: Float,
    stars: List<Triple<Float, Float, Float>>
) {
    // Deep space gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF080D1A),
                Color(0xFF04060C),
                Color(0xFF020308)
            )
        )
    )

    // Twinkling stars
    for (star in stars) {
        val x = star.first * widthPx
        val y = star.second * heightPx
        val size = star.third
        drawCircle(
            color = Color.White.copy(alpha = 0.45f + (star.third / 4f)),
            radius = size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawAtmosphereGlow(cx: Float, cy: Float, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x4400E5FF),
                Color(0x2238BDF8),
                Color(0x11818CF8),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = radius * 1.25f
        ),
        radius = radius * 1.25f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawGraticule(
    cx: Float,
    cy: Float,
    radius: Float,
    camLat: Double,
    camLon: Double
) {
    val gridColor = Color(0xFF38BDF8).copy(alpha = 0.16f)

    // Parallels (every 30 degrees)
    val lats = listOf(-60.0, -30.0, 0.0, 30.0, 60.0)
    for (lat in lats) {
        val points = mutableListOf<Offset>()
        for (lon in -180..180 step 6) {
            val p = GlobeMath.project(lat, lon.toDouble(), camLat, camLon, cx, cy, radius)
            if (p.isVisible && p.depth > 0) {
                points.add(Offset(p.screenX, p.screenY))
            } else if (points.size > 1) {
                drawPoints(points, PointMode.Polygon, gridColor, strokeWidth = 1f)
                points.clear()
            }
        }
        if (points.size > 1) {
            drawPoints(points, PointMode.Polygon, gridColor, strokeWidth = if (lat == 0.0) 1.5f else 1f)
        }
    }

    // Meridians (every 45 degrees)
    for (lon in -180 until 180 step 45) {
        val points = mutableListOf<Offset>()
        for (lat in -85..85 step 5) {
            val p = GlobeMath.project(lat.toDouble(), lon.toDouble(), camLat, camLon, cx, cy, radius)
            if (p.isVisible && p.depth > 0) {
                points.add(Offset(p.screenX, p.screenY))
            } else if (points.size > 1) {
                drawPoints(points, PointMode.Polygon, gridColor, strokeWidth = 1f)
                points.clear()
            }
        }
        if (points.size > 1) {
            drawPoints(points, PointMode.Polygon, gridColor, strokeWidth = 1f)
        }
    }
}

private fun DrawScope.drawContinents(
    cx: Float,
    cy: Float,
    radius: Float,
    camLat: Double,
    camLon: Double
) {
    val landFillColor = Color(0xFF1E3A5F)
    val coastColor = Color(0xFF539BD8).copy(alpha = 0.75f)

    for (continent in WorldContinentData.continents) {
        val path = Path()
        var started = false
        var anyVisible = false

        for ((lat, lon) in continent.points) {
            val p = GlobeMath.project(lat, lon, camLat, camLon, cx, cy, radius)
            if (p.isVisible && p.depth > 0) {
                anyVisible = true
                if (!started) {
                    path.moveTo(p.screenX, p.screenY)
                    started = true
                } else {
                    path.lineTo(p.screenX, p.screenY)
                }
            } else {
                started = false
            }
        }

        if (anyVisible) {
            // Draw land fill
            drawPath(path = path, color = landFillColor, style = Fill)
            // Draw glowing coastline
            drawPath(path = path, color = coastColor, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawStationClusters(
    clusters: List<StationCluster>,
    selectedStation: RadioStation?,
    playingStation: RadioStation?,
    pulseProgress: Float,
    textMeasurer: TextMeasurer,
    density: Float
) {
    for (cluster in clusters) {
        val isSingle = cluster.isSingleStation
        val station = cluster.singleStation
        val isPlaying = station != null && playingStation?.stationUuid == station.stationUuid
        val isSelected = station != null && selectedStation?.stationUuid == station.stationUuid
        val color = cluster.primaryGenre.color

        val x = cluster.screenX
        val y = cluster.screenY

        if (isSingle) {
            // Single Station Marker
            val baseRadius = 7f * density

            // Radar beacon pulse if playing or selected
            if (isPlaying || isSelected) {
                val beaconRadius = baseRadius + (18f * density * pulseProgress)
                val beaconAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.8f
                drawCircle(
                    color = color.copy(alpha = beaconAlpha),
                    radius = beaconRadius,
                    center = Offset(x, y),
                    style = Stroke(width = 2f * density)
                )
            }

            // Glow backing
            drawCircle(
                color = color.copy(alpha = 0.35f),
                radius = baseRadius + 4f * density,
                center = Offset(x, y)
            )

            // Solid Station Pin Circle
            drawCircle(
                color = color,
                radius = baseRadius,
                center = Offset(x, y)
            )

            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = baseRadius * 0.45f,
                center = Offset(x, y)
            )
        } else {
            // Cluster Badge
            val count = cluster.count
            val countStr = if (count > 999) "${count / 1000}k" else count.toString()
            val badgeRadius = (12f + min(count.toFloat() / 20f, 8f)) * density

            // Outer soft glow
            drawCircle(
                color = color.copy(alpha = 0.25f),
                radius = badgeRadius + 4f * density,
                center = Offset(x, y)
            )

            // Badge disk
            drawCircle(
                color = Color(0xFF0F172A),
                radius = badgeRadius,
                center = Offset(x, y)
            )

            // Colored Border
            drawCircle(
                color = color,
                radius = badgeRadius,
                center = Offset(x, y),
                style = Stroke(width = 2.2f * density)
            )

            // Count text
            val textLayoutResult = textMeasurer.measure(
                text = countStr,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            val textOffset = Offset(
                x = x - (textLayoutResult.size.width / 2f),
                y = y - (textLayoutResult.size.height / 2f)
            )
            drawText(textLayoutResult, topLeft = textOffset)
        }
    }
}
