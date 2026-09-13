package com.example.globe

import com.example.model.RadioStation
import com.example.model.StationCluster
import com.example.model.StationGenre
import kotlin.math.sqrt

object ClusterEngine {

    fun clusterStations(
        stations: List<RadioStation>,
        camLat: Double,
        camLon: Double,
        centerX: Float,
        centerY: Float,
        radius: Float,
        densityDpi: Float = 2.5f
    ): List<StationCluster> {
        val visibleWithScreen = mutableListOf<Pair<RadioStation, ProjectedPoint>>()

        for (station in stations) {
            val lat = station.latitude ?: continue
            val lon = station.longitude ?: continue

            val proj = GlobeMath.project(
                lat = lat,
                lon = lon,
                camLat = camLat,
                camLon = camLon,
                centerX = centerX,
                centerY = centerY,
                radius = radius
            )

            if (proj.isVisible && proj.depth > 0.05f) {
                visibleWithScreen.add(station to proj)
            }
        }

        if (visibleWithScreen.isEmpty()) return emptyList()

        // Cluster threshold in pixels based on zoom radius
        val baseThresholdDp = when {
            radius < 180f * densityDpi -> 54f
            radius < 320f * densityDpi -> 40f
            radius < 500f * densityDpi -> 26f
            else -> 16f
        }
        val thresholdPx = baseThresholdDp * densityDpi
        val thresholdPxSq = thresholdPx * thresholdPx

        val clusters = mutableListOf<MutableCluster>()

        for ((station, proj) in visibleWithScreen) {
            var foundCluster: MutableCluster? = null
            for (cluster in clusters) {
                val dx = cluster.screenX - proj.screenX
                val dy = cluster.screenY - proj.screenY
                if (dx * dx + dy * dy < thresholdPxSq) {
                    foundCluster = cluster
                    break
                }
            }

            if (foundCluster != null) {
                foundCluster.addStation(station, proj.screenX, proj.screenY)
            } else {
                val newCluster = MutableCluster(
                    id = "cluster_${clusters.size}_${station.stationUuid}",
                    initialStation = station,
                    initScreenX = proj.screenX,
                    initScreenY = proj.screenY,
                    initLat = station.latitude ?: 0.0,
                    initLon = station.longitude ?: 0.0
                )
                clusters.add(newCluster)
            }
        }

        return clusters.map { it.toImmutable() }
    }

    private class MutableCluster(
        val id: String,
        initialStation: RadioStation,
        initScreenX: Float,
        initScreenY: Float,
        val initLat: Double,
        val initLon: Double
    ) {
        val stations = mutableListOf(initialStation)
        var screenX: Float = initScreenX
        var screenY: Float = initScreenY

        fun addStation(station: RadioStation, sx: Float, sy: Float) {
            stations.add(station)
            // Weight cluster center slightly towards new station
            screenX = (screenX * (stations.size - 1) + sx) / stations.size
            screenY = (screenY * (stations.size - 1) + sy) / stations.size
        }

        fun toImmutable(): StationCluster {
            val genreCounts = mutableMapOf<StationGenre, Int>()
            stations.forEach { st ->
                genreCounts[st.genre] = (genreCounts[st.genre] ?: 0) + 1
            }
            val primaryGenre = genreCounts.maxByOrNull { it.value }?.key ?: StationGenre.POP

            return StationCluster(
                id = id,
                centerLat = initLat,
                centerLon = initLon,
                screenX = screenX,
                screenY = screenY,
                isVisible = true,
                stations = stations,
                primaryGenre = primaryGenre
            )
        }
    }
}
