package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.RadioStation
import com.example.ui.components.StationItem
import com.example.ui.theme.WorldRadioTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun station_item_screenshot() {
        val sampleStation = RadioStation(
            stationUuid = "sample_1",
            name = "BBC Radio 1",
            url = "http://test.mp3",
            urlResolved = "http://test.mp3",
            country = "United Kingdom",
            state = "London",
            tags = "pop,top40",
            bitrate = 128,
            latitude = 51.5,
            longitude = -0.12
        )

        composeTestRule.setContent {
            WorldRadioTheme(darkTheme = true) {
                StationItem(
                    station = sampleStation,
                    isPlaying = false,
                    isLoading = false,
                    isFavorite = true,
                    onPlayClick = {},
                    onFavoriteClick = {},
                    onItemClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/station_item.png")
    }
}
