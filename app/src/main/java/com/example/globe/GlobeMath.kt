package com.example.globe

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val isVisible: Boolean,
    val depth: Float // positive if facing camera
)

object GlobeMath {
    private const val DEG_TO_RAD = (PI / 180.0).toFloat()
    private const val RAD_TO_DEG = (180.0 / PI).toFloat()

    /**
     * Projects a geographical coordinate (lat, lon in degrees) onto screen canvas
     * given center (cx, cy), radius R, and camera rotation (camLat, camLon in degrees).
     */
    fun project(
        lat: Double,
        lon: Double,
        camLat: Double,
        camLon: Double,
        centerX: Float,
        centerY: Float,
        radius: Float
    ): ProjectedPoint {
        val phi = (lat * DEG_TO_RAD).toFloat()
        val lambda = (lon * DEG_TO_RAD).toFloat()
        val phi0 = (camLat * DEG_TO_RAD).toFloat()
        val lambda0 = (camLon * DEG_TO_RAD).toFloat()

        val cosPhi = cos(phi)
        val sinPhi = sin(phi)
        val deltaLambda = lambda - lambda0

        val cosDeltaLambda = cos(deltaLambda)
        val sinDeltaLambda = sin(deltaLambda)

        val cosPhi0 = cos(phi0)
        val sinPhi0 = sin(phi0)

        // Orthographic projection formulas:
        // x = cos(phi) * sin(deltaLambda)
        // y = cos(phi0) * sin(phi) - sin(phi0) * cos(phi) * cos(deltaLambda)
        // z = sin(phi0) * sin(phi) + cos(phi0) * cos(phi) * cos(deltaLambda)
        val x = cosPhi * sinDeltaLambda
        val y = cosPhi0 * sinPhi - sinPhi0 * cosPhi * cosDeltaLambda
        val z = sinPhi0 * sinPhi + cosPhi0 * cosPhi * cosDeltaLambda

        val isVisible = z > -0.05f
        val sx = centerX + x * radius
        val sy = centerY - y * radius

        return ProjectedPoint(
            screenX = sx,
            screenY = sy,
            isVisible = isVisible,
            depth = z
        )
    }

    /**
     * Converts a screen touch (touchX, touchY) into a geographical coordinate (lat, lon)
     * if the touch falls within the globe disc. Returns null if touched outside the globe.
     */
    fun unproject(
        touchX: Float,
        touchY: Float,
        camLat: Double,
        camLon: Double,
        centerX: Float,
        centerY: Float,
        radius: Float
    ): Pair<Double, Double>? {
        val dx = (touchX - centerX) / radius
        val dy = (centerY - touchY) / radius
        val distSq = dx * dx + dy * dy
        if (distSq > 1.0f) return null // Outside globe circle

        val z = sqrt(1.0f - distSq)
        val phi0 = (camLat * DEG_TO_RAD).toFloat()
        val lambda0 = (camLon * DEG_TO_RAD).toFloat()

        val sinPhi0 = sin(phi0)
        val cosPhi0 = cos(phi0)

        // Invert orthographic projection
        val sinPhi = dy * cosPhi0 + z * sinPhi0
        val phi = asin(sinPhi.coerceIn(-1.0f, 1.0f))

        val yPrime = z * cosPhi0 - dy * sinPhi0
        val lambda = lambda0 + atan2(dx, yPrime)

        var resLon = (lambda * RAD_TO_DEG).toDouble()
        val resLat = (phi * RAD_TO_DEG).toDouble()

        // Normalize longitude into [-180, 180]
        while (resLon > 180.0) resLon -= 360.0
        while (resLon < -180.0) resLon += 360.0

        return Pair(resLat, resLon)
    }

    /**
     * Great circle angular distance in radians between two geo points.
     */
    fun angularDistanceRad(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = lat1 * DEG_TO_RAD
        val l1 = lon1 * DEG_TO_RAD
        val p2 = lat2 * DEG_TO_RAD
        val l2 = lon2 * DEG_TO_RAD

        val dLat = p2 - p1
        val dLon = l2 - l1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(p1) * cos(p2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return c.toDouble()
    }
}
