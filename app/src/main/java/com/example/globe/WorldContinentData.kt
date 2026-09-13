package com.example.globe

/**
 * Simplified geographic coastline polygons for 3D globe rendering.
 * Coordinates are in degrees (latitude, longitude).
 */
object WorldContinentData {

    data class GeoPolygon(val name: String, val points: List<Pair<Double, Double>>)

    val continents: List<GeoPolygon> = listOf(
        // Europe & Scandinavia
        GeoPolygon(
            "Europe",
            listOf(
                36.0 to -5.5, 36.5 to -2.0, 38.0 to 0.0, 42.0 to 3.0, 43.5 to 7.0,
                44.0 to 9.5, 41.0 to 9.0, 37.0 to 15.0, 38.0 to 16.0, 40.0 to 18.0,
                38.0 to 24.0, 41.0 to 28.5, 44.0 to 29.0, 46.5 to 30.5, 45.0 to 36.5,
                47.0 to 39.0, 50.0 to 40.0, 55.0 to 38.0, 60.0 to 30.0, 65.0 to 25.0,
                70.0 to 28.0, 71.0 to 24.0, 68.0 to 14.0, 62.0 to 5.0, 58.0 to 7.0,
                55.0 to 8.5, 53.5 to 7.0, 51.0 to 2.0, 49.0 to -1.5, 48.0 to -4.5,
                46.0 to -1.0, 43.5 to -1.8, 43.5 to -8.5, 41.0 to -9.0, 37.0 to -9.0,
                36.0 to -5.5
            )
        ),

        // British Isles
        GeoPolygon(
            "British Isles",
            listOf(
                50.0 to -5.0, 50.5 to 1.5, 53.0 to 0.5, 55.0 to -1.5, 58.5 to -3.0,
                58.5 to -5.0, 56.0 to -6.0, 54.5 to -5.0, 51.5 to -4.5, 50.0 to -5.0
            )
        ),

        // Africa
        GeoPolygon(
            "Africa",
            listOf(
                36.0 to -5.5, 37.0 to 10.0, 32.5 to 15.0, 32.0 to 20.0, 31.5 to 32.0,
                28.0 to 34.0, 22.0 to 37.0, 12.0 to 44.0, 11.5 to 51.0, 2.0 to 45.0,
                -5.0 to 39.0, -11.0 to 40.5, -25.0 to 33.0, -34.5 to 20.0, -34.0 to 18.5,
                -28.0 to 16.0, -16.0 to 12.0, -5.0 to 12.0, 4.0 to 9.0, 5.0 to 1.0,
                4.5 to -7.5, 9.5 to -13.5, 14.5 to -17.0, 21.0 to -17.0, 28.0 to -13.0,
                35.0 to -6.0, 36.0 to -5.5
            )
        ),

        // Asia
        GeoPolygon(
            "Asia",
            listOf(
                41.0 to 28.5, 36.5 to 36.0, 31.0 to 35.0, 28.0 to 34.0, 22.0 to 39.0,
                15.0 to 43.0, 12.5 to 54.0, 24.0 to 57.0, 25.0 to 62.0, 24.0 to 68.0,
                19.0 to 73.0, 8.0 to 77.5, 13.0 to 80.0, 21.5 to 87.0, 21.0 to 92.0,
                16.0 to 96.0, 8.0 to 98.5, 1.5 to 104.0, 7.0 to 105.0, 13.0 to 109.0,
                21.0 to 108.0, 22.5 to 114.0, 29.0 to 122.0, 35.0 to 119.5, 39.0 to 124.0,
                38.5 to 128.5, 43.0 to 132.0, 52.0 to 141.0, 60.0 to 162.0, 66.0 to 170.0,
                70.0 to -179.0, 72.0 to 140.0, 77.0 to 105.0, 73.0 to 70.0, 68.0 to 60.0,
                55.0 to 60.0, 50.0 to 50.0, 45.0 to 36.5, 41.0 to 28.5
            )
        ),

        // Japan
        GeoPolygon(
            "Japan",
            listOf(
                31.0 to 130.5, 33.5 to 130.0, 35.5 to 133.5, 37.5 to 138.5, 41.5 to 141.0,
                45.5 to 142.0, 44.0 to 145.0, 42.0 to 143.5, 38.0 to 141.5, 35.0 to 140.0,
                33.5 to 135.5, 31.5 to 131.5, 31.0 to 130.5
            )
        ),

        // North America
        GeoPolygon(
            "North America",
            listOf(
                7.5 to -77.5, 8.5 to -83.0, 15.0 to -88.0, 18.0 to -94.0, 22.0 to -97.5,
                26.0 to -97.0, 29.0 to -94.0, 29.5 to -85.0, 25.0 to -80.5, 30.5 to -81.5,
                35.0 to -75.5, 41.0 to -72.0, 44.5 to -66.0, 47.0 to -60.0, 52.0 to -55.5,
                60.0 to -64.0, 62.0 to -78.0, 58.0 to -94.0, 68.0 to -90.0, 70.0 to -130.0,
                71.0 to -156.0, 65.0 to -168.0, 58.5 to -162.0, 55.0 to -160.0, 58.0 to -136.0,
                54.0 to -130.0, 49.0 to -125.0, 42.0 to -124.5, 32.5 to -117.0, 23.0 to -110.0,
                24.5 to -107.0, 16.0 to -93.0, 14.0 to -91.0, 8.5 to -83.0, 7.5 to -77.5
            )
        ),

        // South America
        GeoPolygon(
            "South America",
            listOf(
                12.0 to -72.0, 10.5 to -61.5, 6.0 to -55.0, 2.0 to -50.0, -2.5 to -44.0,
                -5.0 to -35.0, -13.0 to -38.5, -23.0 to -43.0, -32.0 to -52.0, -35.0 to -57.0,
                -41.0 to -63.0, -52.0 to -68.0, -55.0 to -66.0, -52.0 to -74.0, -42.0 to -74.0,
                -30.0 to -71.5, -18.0 to -71.0, -5.0 to -81.0, 1.0 to -79.5, 7.5 to -77.5,
                11.5 to -74.0, 12.0 to -72.0
            )
        ),

        // Australia
        GeoPolygon(
            "Australia",
            listOf(
                -12.0 to 132.0, -12.0 to 136.0, -15.0 to 136.0, -11.0 to 142.0, -18.0 to 146.0,
                -25.0 to 153.0, -34.0 to 151.0, -38.0 to 147.0, -38.0 to 140.0, -32.0 to 132.0,
                -34.5 to 119.0, -32.0 to 115.5, -22.0 to 114.0, -17.0 to 122.0, -14.0 to 126.0,
                -12.0 to 132.0
            )
        ),

        // New Zealand
        GeoPolygon(
            "New Zealand",
            listOf(
                -35.0 to 173.0, -37.5 to 178.0, -41.5 to 175.0, -46.5 to 169.0,
                -46.0 to 166.5, -42.0 to 171.5, -39.0 to 174.0, -35.0 to 173.0
            )
        ),

        // Madagascar
        GeoPolygon(
            "Madagascar",
            listOf(
                -12.0 to 49.0, -15.5 to 50.5, -25.0 to 47.0, -25.5 to 45.0,
                -20.0 to 44.0, -16.0 to 44.0, -12.0 to 49.0
            )
        ),

        // Greenland
        GeoPolygon(
            "Greenland",
            listOf(
                60.0 to -44.0, 65.0 to -38.0, 70.0 to -22.0, 77.0 to -19.0,
                82.0 to -30.0, 81.0 to -60.0, 76.0 to -68.0, 70.0 to -53.0,
                65.0 to -52.0, 60.0 to -44.0
            )
        )
    )
}
