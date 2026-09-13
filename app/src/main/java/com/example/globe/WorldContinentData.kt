package com.example.globe

/**
 * Detailed geographic coastline polygons for 1:1 real world map and 3D globe rendering.
 * Coordinates are in degrees (latitude, longitude).
 */
object WorldContinentData {

    data class GeoPolygon(
        val name: String,
        val points: List<Pair<Double, Double>>,
        val isBorderOrIsland: Boolean = false
    )

    data class GeoCity(
        val name: String,
        val country: String,
        val lat: Double,
        val lon: Double
    )

    // Key reference cities on the map for geographical orientation
    val landmarkCities: List<GeoCity> = listOf(
        GeoCity("Istanbul", "TR", 41.0082, 28.9784),
        GeoCity("Ankara", "TR", 39.9334, 32.8597),
        GeoCity("Izmir", "TR", 38.4237, 27.1428),
        GeoCity("London", "GB", 51.5074, -0.1278),
        GeoCity("Paris", "FR", 48.8566, 2.3522),
        GeoCity("Berlin", "DE", 52.5200, 13.4050),
        GeoCity("Rome", "IT", 41.9028, 12.4964),
        GeoCity("Madrid", "ES", 40.4168, -3.7038),
        GeoCity("Athens", "GR", 37.9838, 23.7275),
        GeoCity("New York", "US", 40.7128, -74.0060),
        GeoCity("Los Angeles", "US", 34.0522, -118.2437),
        GeoCity("Tokyo", "JP", 35.6762, 139.6503),
        GeoCity("Sydney", "AU", -33.8688, 151.2093),
        GeoCity("Cairo", "EG", 30.0444, 31.2357),
        GeoCity("Rio de Janeiro", "BR", -22.9068, -43.1729),
        GeoCity("Moscow", "RU", 55.7558, 37.6173),
        GeoCity("Dubai", "AE", 25.2048, 55.2708),
        GeoCity("Mumbai", "IN", 19.0760, 72.8777)
    )

    val continents: List<GeoPolygon> = listOf(
        // ==========================================
        // 1. ANATOLIA & TURKEY (High Fidelity)
        // ==========================================
        GeoPolygon(
            "Turkey (Anatolia & Thrace)",
            listOf(
                // Black Sea coast (West to East)
                41.2 to 28.0, 41.8 to 28.0, 42.0 to 30.0, 41.7 to 32.3, 42.0 to 35.0,
                41.3 to 36.3, 41.0 to 37.5, 41.0 to 39.7, 41.4 to 41.5,
                // Eastern border
                41.5 to 43.5, 40.0 to 44.5, 39.4 to 44.3, 38.5 to 44.8, 37.1 to 44.3,
                // Southern border & Mediterranean
                37.3 to 42.2, 36.8 to 38.0, 36.7 to 36.2, 35.9 to 36.0, 36.7 to 35.5,
                36.5 to 34.3, 36.1 to 33.0, 36.2 to 30.5, 36.8 to 28.3,
                // Aegean coast
                37.0 to 27.3, 38.4 to 26.3, 39.3 to 26.0, 40.0 to 26.2,
                // Dardanelles, Marmara & Thrace
                40.4 to 26.7, 40.9 to 27.5, 41.0 to 28.9, 41.2 to 28.0
            )
        ),

        // ==========================================
        // 2. EUROPE & SCANDINAVIA
        // ==========================================
        GeoPolygon(
            "Europe & Iberia",
            listOf(
                // Iberia (Spain & Portugal)
                36.0 to -5.6, 36.5 to -2.0, 38.0 to 0.0, 40.0 to 0.7, 42.3 to 3.2,
                // South France & Italy
                43.3 to 4.5, 43.5 to 7.0, 44.4 to 8.9, 44.0 to 10.0, 42.0 to 11.8,
                41.0 to 14.3, 39.0 to 16.5, 38.0 to 15.6, 40.0 to 17.5, 41.2 to 16.8,
                43.6 to 13.5, 45.4 to 12.3, 45.8 to 13.6,
                // Balkans & Greece
                44.8 to 14.0, 42.5 to 18.5, 41.3 to 19.4, 39.6 to 20.0, 38.2 to 21.7,
                36.4 to 22.5, 37.9 to 23.7, 39.5 to 22.8, 40.5 to 23.0, 40.8 to 25.8,
                // Black Sea Western coast
                42.5 to 27.5, 44.2 to 28.6, 45.3 to 29.7, 46.5 to 31.0, 45.3 to 33.5,
                44.5 to 34.0, 45.3 to 36.5, 47.0 to 39.0, 50.0 to 40.0,
                // Eastern Europe & Baltic & Scandinavia
                55.0 to 35.0, 59.0 to 30.0, 60.5 to 28.0, 65.0 to 25.0, 70.0 to 28.0,
                71.2 to 25.8, 69.5 to 18.0, 68.0 to 14.0, 63.5 to 8.5, 60.0 to 5.0,
                58.0 to 7.0, 55.5 to 12.5, 54.0 to 10.0, 53.5 to 7.0, 51.5 to 3.0,
                // France, Bay of Biscay & Atlantic
                49.5 to 0.0, 48.6 to -4.7, 47.2 to -2.2, 45.7 to -1.2, 43.4 to -1.7,
                43.5 to -5.0, 43.7 to -7.9, 42.0 to -8.9, 39.0 to -9.4, 37.0 to -9.0,
                36.0 to -5.6
            )
        ),

        // ==========================================
        // 3. BRITISH ISLES & IRELAND
        // ==========================================
        GeoPolygon(
            "Great Britain",
            listOf(
                50.0 to -5.2, 50.6 to -2.0, 50.8 to 0.8, 51.5 to 1.4, 52.8 to 1.7,
                53.5 to 0.0, 55.0 to -1.5, 57.0 to -2.0, 58.6 to -3.1, 58.5 to -5.0,
                57.5 to -5.8, 56.0 to -5.5, 54.5 to -3.5, 53.3 to -4.5, 51.5 to -4.5,
                50.3 to -4.8, 50.0 to -5.2
            )
        ),
        GeoPolygon(
            "Ireland",
            listOf(
                51.5 to -9.5, 51.8 to -8.0, 52.2 to -6.3, 53.3 to -6.0, 54.5 to -5.8,
                55.3 to -7.3, 54.5 to -8.5, 53.0 to -9.9, 52.0 to -10.3, 51.5 to -9.5
            )
        ),

        // ==========================================
        // 4. MEDITERRANEAN ISLANDS
        // ==========================================
        GeoPolygon(
            "Crete & Cyprus",
            listOf(
                35.3 to 23.6, 35.5 to 24.5, 35.2 to 26.2, 35.0 to 25.0, 35.3 to 23.6
            ),
            isBorderOrIsland = true
        ),
        GeoPolygon(
            "Cyprus",
            listOf(
                35.0 to 32.3, 35.4 to 34.0, 35.7 to 34.6, 35.0 to 34.0, 34.7 to 33.0, 35.0 to 32.3
            ),
            isBorderOrIsland = true
        ),
        GeoPolygon(
            "Sicily & Sardinia",
            listOf(
                38.2 to 13.0, 38.3 to 15.5, 37.0 to 15.3, 36.7 to 14.5, 37.6 to 12.5, 38.2 to 13.0
            ),
            isBorderOrIsland = true
        ),

        // ==========================================
        // 5. AFRICA & MADAGASCAR
        // ==========================================
        GeoPolygon(
            "Africa",
            listOf(
                // North Africa (Gibraltar to Suez)
                35.8 to -5.5, 36.0 to 0.0, 37.0 to 10.0, 34.0 to 11.0, 32.5 to 15.0,
                31.0 to 17.0, 32.0 to 20.0, 32.0 to 24.5, 31.5 to 30.0, 31.3 to 32.3,
                // Red Sea & Horn of Africa
                28.0 to 34.0, 22.0 to 37.0, 15.5 to 40.0, 12.0 to 43.5, 11.8 to 51.2,
                8.0 to 50.0, 2.0 to 45.0,
                // East Coast
                -4.0 to 39.5, -11.0 to 40.5, -16.0 to 40.0, -26.0 to 33.0, -32.0 to 28.5,
                // South Africa
                -34.5 to 20.0, -34.0 to 18.5, -28.0 to 16.0,
                // West Coast
                -16.0 to 11.5, -5.0 to 12.0, 4.0 to 9.0, 5.0 to 5.0, 5.0 to 0.0,
                4.5 to -7.5, 7.0 to -11.5, 9.5 to -13.5, 14.5 to -17.2, 21.0 to -17.0,
                28.0 to -13.0, 33.0 to -9.0, 35.8 to -5.5
            )
        ),
        GeoPolygon(
            "Madagascar",
            listOf(
                -12.0 to 49.3, -15.5 to 50.5, -20.0 to 48.5, -25.0 to 47.0, -25.5 to 45.0,
                -22.0 to 43.3, -16.0 to 44.0, -12.0 to 49.3
            ),
            isBorderOrIsland = true
        ),

        // ==========================================
        // 6. ARABIAN PENINSULA & MIDDLE EAST
        // ==========================================
        GeoPolygon(
            "Arabian Peninsula",
            listOf(
                29.5 to 35.0, 28.0 to 34.5, 22.0 to 39.0, 15.0 to 42.5, 12.6 to 43.5,
                12.8 to 45.0, 14.5 to 49.0, 17.0 to 54.0, 22.5 to 59.8, 25.0 to 56.5,
                24.5 to 54.5, 26.0 to 50.5, 28.5 to 48.5, 30.0 to 48.0, 29.5 to 35.0
            )
        ),

        // ==========================================
        // 7. ASIA & INDIA & INDOCHINA
        // ==========================================
        GeoPolygon(
            "Asia Mainland",
            listOf(
                // Caspian & Persian Gulf to India
                30.0 to 48.5, 26.0 to 57.0, 25.0 to 62.0, 24.0 to 68.0,
                // India
                23.0 to 70.0, 20.5 to 72.8, 15.5 to 73.8, 8.5 to 77.0, 8.2 to 77.5,
                13.0 to 80.2, 17.0 to 82.3, 21.5 to 87.0, 22.0 to 90.0,
                // Indochina & Southeast Asia
                21.0 to 92.0, 16.0 to 94.5, 16.0 to 97.5, 12.0 to 99.0, 6.0 to 102.0,
                1.3 to 104.0, 5.0 to 103.0, 10.0 to 100.0, 12.0 to 101.5, 8.5 to 105.0,
                13.0 to 109.5, 19.0 to 106.0, 21.5 to 108.0,
                // China & East Asia
                22.5 to 114.0, 24.5 to 118.5, 28.0 to 121.5, 31.5 to 121.5, 35.0 to 119.5,
                38.0 to 118.0, 39.0 to 122.0,
                // Korea
                38.0 to 125.0, 35.0 to 126.0, 35.0 to 129.0, 38.0 to 128.5,
                // Far East & Siberia
                43.0 to 132.0, 48.0 to 140.5, 53.0 to 142.0, 59.0 to 150.0, 57.0 to 156.5,
                52.0 to 158.0, 60.0 to 164.0, 66.0 to 170.0, 70.0 to -179.0, 72.0 to 140.0,
                77.0 to 105.0, 73.0 to 70.0, 68.0 to 60.0, 55.0 to 60.0, 45.0 to 50.0,
                38.0 to 48.0, 30.0 to 48.5
            )
        ),
        GeoPolygon(
            "Japan",
            listOf(
                31.0 to 130.5, 33.5 to 130.0, 35.0 to 133.0, 37.5 to 137.5, 41.5 to 140.0,
                44.0 to 141.5, 45.5 to 142.0, 44.0 to 145.0, 42.0 to 143.5, 38.0 to 141.5,
                35.0 to 140.0, 33.5 to 135.5, 31.5 to 131.5, 31.0 to 130.5
            ),
            isBorderOrIsland = true
        ),
        GeoPolygon(
            "Sri Lanka",
            listOf(
                9.8 to 80.2, 8.5 to 81.3, 6.0 to 80.5, 6.8 to 79.8, 9.8 to 80.2
            ),
            isBorderOrIsland = true
        ),
        GeoPolygon(
            "Indonesia (Java & Sumatra)",
            listOf(
                5.5 to 95.5, 0.0 to 101.0, -5.5 to 105.5, -6.5 to 106.5, -8.0 to 110.0,
                -8.5 to 114.0, -7.0 to 112.5, -6.0 to 106.0, -4.0 to 103.0, 2.0 to 98.0, 5.5 to 95.5
            ),
            isBorderOrIsland = true
        ),

        // ==========================================
        // 8. NORTH AMERICA
        // ==========================================
        GeoPolygon(
            "North America",
            listOf(
                // Central America & Gulf
                8.0 to -77.5, 9.0 to -83.0, 14.0 to -88.0, 18.0 to -90.0, 21.5 to -87.0,
                19.0 to -91.0, 18.5 to -94.0, 22.0 to -97.5, 26.0 to -97.0, 29.5 to -94.5,
                29.0 to -89.0, 30.0 to -84.0, 25.0 to -80.5,
                // East Coast
                30.5 to -81.5, 35.0 to -75.5, 37.0 to -76.0, 40.5 to -73.8, 42.0 to -70.5,
                44.5 to -66.0, 46.5 to -60.0, 47.5 to -53.0, 52.0 to -55.5, 58.0 to -62.0,
                60.0 to -64.0, 62.0 to -76.0, 58.0 to -80.0, 52.0 to -82.0, 57.0 to -92.0,
                // Arctic & Alaska
                64.0 to -85.0, 69.0 to -90.0, 71.0 to -115.0, 71.0 to -156.0, 65.5 to -168.0,
                60.0 to -166.0, 58.5 to -160.0, 55.0 to -163.0, 58.0 to -136.0,
                // Pacific Coast
                54.0 to -130.0, 49.0 to -124.0, 46.0 to -124.0, 38.0 to -123.0, 33.0 to -117.5,
                28.0 to -114.5, 23.0 to -110.0, 24.5 to -107.0, 16.0 to -94.0, 14.0 to -92.0,
                8.5 to -83.0, 8.0 to -77.5
            )
        ),
        GeoPolygon(
            "Greenland",
            listOf(
                60.0 to -44.0, 65.0 to -38.0, 70.0 to -22.0, 77.0 to -19.0,
                82.0 to -30.0, 81.0 to -60.0, 76.0 to -68.0, 70.0 to -53.0,
                65.0 to -52.0, 60.0 to -44.0
            ),
            isBorderOrIsland = true
        ),
        GeoPolygon(
            "Iceland",
            listOf(
                63.5 to -19.0, 64.0 to -14.0, 66.0 to -14.5, 66.5 to -18.0,
                66.0 to -23.0, 64.5 to -22.0, 63.5 to -19.0
            ),
            isBorderOrIsland = true
        ),

        // ==========================================
        // 9. SOUTH AMERICA
        // ==========================================
        GeoPolygon(
            "South America",
            listOf(
                12.0 to -72.0, 11.0 to -63.0, 6.0 to -55.0, 2.0 to -50.0, -2.5 to -44.0,
                -5.0 to -35.0, -8.0 to -35.0, -13.0 to -38.5, -20.0 to -40.0, -23.0 to -43.0,
                -30.0 to -50.0, -34.5 to -53.5, -35.0 to -57.5, -41.0 to -63.0, -47.0 to -66.0,
                -52.0 to -68.5, -55.0 to -66.5, -52.0 to -74.0, -42.0 to -74.0, -33.0 to -72.0,
                -23.0 to -70.5, -15.0 to -75.0, -5.0 to -81.0, 0.0 to -80.0, 7.5 to -77.5,
                11.5 to -74.0, 12.0 to -72.0
            )
        ),

        // ==========================================
        // 10. AUSTRALIA & NEW ZEALAND
        // ==========================================
        GeoPolygon(
            "Australia",
            listOf(
                -12.0 to 132.0, -12.0 to 136.0, -15.0 to 136.0, -11.0 to 142.0, -18.0 to 146.0,
                -25.0 to 153.0, -32.0 to 152.5, -34.0 to 151.0, -38.0 to 147.0, -38.0 to 140.0,
                -32.0 to 132.0, -34.5 to 119.0, -32.0 to 115.5, -22.0 to 114.0, -17.0 to 122.0,
                -14.0 to 126.0, -12.0 to 132.0
            )
        ),
        GeoPolygon(
            "Tasmania",
            listOf(
                -41.0 to 145.0, -41.0 to 148.0, -43.5 to 147.5, -43.5 to 145.5, -41.0 to 145.0
            ),
            isBorderOrIsland = true
        ),
        GeoPolygon(
            "New Zealand",
            listOf(
                -35.0 to 173.0, -37.5 to 178.0, -41.5 to 175.0, -46.5 to 169.0,
                -46.0 to 166.5, -42.0 to 171.5, -39.0 to 174.0, -35.0 to 173.0
            ),
            isBorderOrIsland = true
        )
    )
}
