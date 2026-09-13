package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.globe.ClusterEngine
import com.example.globe.GlobeMath
import com.example.model.RadioStation
import com.example.model.StationGenre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("World Radio", appName)
    }

    @Test
    fun `globe math projects front-facing point as visible`() {
        val proj = GlobeMath.project(
            lat = 0.0,
            lon = 0.0,
            camLat = 0.0,
            camLon = 0.0,
            centerX = 200f,
            centerY = 200f,
            radius = 150f
        )
        assertTrue(proj.isVisible)
        assertTrue(proj.depth > 0.9f)
        assertEquals(200f, proj.screenX, 1f)
        assertEquals(200f, proj.screenY, 1f)
    }

    @Test
    fun `globe math projects back-facing point as not visible`() {
        val proj = GlobeMath.project(
            lat = 0.0,
            lon = 180.0,
            camLat = 0.0,
            camLon = 0.0,
            centerX = 200f,
            centerY = 200f,
            radius = 150f
        )
        assertTrue(!proj.isVisible || proj.depth < 0f)
    }

    @Test
    fun `globe unproject maps center back to camera coords`() {
        val geo = GlobeMath.unproject(
            touchX = 250f,
            touchY = 250f,
            camLat = 20.0,
            camLon = 40.0,
            centerX = 250f,
            centerY = 250f,
            radius = 200f
        )
        assertNotNull(geo)
        assertEquals(20.0, geo!!.first, 0.5)
        assertEquals(40.0, geo.second, 0.5)
    }

    @Test
    fun `station genre determines correct category`() {
        val rockGenre = StationGenre.fromTags("rock,alternative,indie")
        assertEquals(StationGenre.ROCK, rockGenre)

        val jazzGenre = StationGenre.fromTags("jazz,blues,smooth")
        assertEquals(StationGenre.JAZZ, jazzGenre)

        val popGenre = StationGenre.fromTags("top40,pop,hits")
        assertEquals(StationGenre.POP, popGenre)
    }

    @Test
    fun `cluster engine clusters close stations`() {
        val st1 = RadioStation(
            stationUuid = "st1",
            name = "London Station 1",
            url = "http://test1.mp3",
            urlResolved = "http://test1.mp3",
            latitude = 51.5074,
            longitude = -0.1278
        )
        val st2 = RadioStation(
            stationUuid = "st2",
            name = "London Station 2",
            url = "http://test2.mp3",
            urlResolved = "http://test2.mp3",
            latitude = 51.5080,
            longitude = -0.1280
        )

        val clusters = ClusterEngine.clusterStations(
            stations = listOf(st1, st2),
            camLat = 51.5,
            camLon = -0.1,
            centerX = 300f,
            centerY = 300f,
            radius = 200f
        )

        assertEquals(1, clusters.size)
        assertEquals(2, clusters.first().count)
    }
}
