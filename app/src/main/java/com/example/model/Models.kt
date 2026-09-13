package com.example.model

import androidx.compose.ui.graphics.Color

enum class StationGenre(val displayName: String, val color: Color, val symbol: String) {
    POP("Pop / Top 40", Color(0xFFF43F5E), "P"),
    ROCK("Rock", Color(0xFFF97316), "R"),
    JAZZ("Jazz & Blues", Color(0xFFA855F7), "J"),
    CLASSICAL("Classical", Color(0xFF38BDF8), "C"),
    NEWS("News & Talk", Color(0xFF10B981), "N"),
    ELECTRONIC("Electronic / Dance", Color(0xFFEC4899), "E"),
    LOCAL("Local & World", Color(0xFF94A3B8), "W");

    companion object {
        fun fromTags(tags: String?): StationGenre {
            if (tags.isNullOrBlank()) return LOCAL
            val lower = tags.lowercase()
            return when {
                lower.contains("pop") || lower.contains("top40") || lower.contains("hits") -> POP
                lower.contains("rock") || lower.contains("metal") || lower.contains("alternative") || lower.contains("punk") -> ROCK
                lower.contains("jazz") || lower.contains("blues") || lower.contains("soul") -> JAZZ
                lower.contains("classic") || lower.contains("symphony") || lower.contains("instrumental") -> CLASSICAL
                lower.contains("news") || lower.contains("talk") || lower.contains("info") || lower.contains("podcast") || lower.contains("sport") -> NEWS
                lower.contains("electro") || lower.contains("dance") || lower.contains("techno") || lower.contains("house") || lower.contains("trance") || lower.contains("edm") -> ELECTRONIC
                else -> LOCAL
            }
        }
    }
}

data class RadioStation(
    val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String = "",
    val favicon: String = "",
    val tags: String = "",
    val country: String = "",
    val countryCode: String = "",
    val state: String = "",
    val language: String = "",
    val votes: Int = 0,
    val codec: String = "",
    val bitrate: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val clickCount: Int = 0,
    val isWorking: Boolean = true,
    val nowPlaying: String = ""
) {
    val genre: StationGenre
        get() = StationGenre.fromTags(tags)

    val locationTitle: String
        get() {
            val parts = mutableListOf<String>()
            if (state.isNotBlank()) parts.add(state)
            if (country.isNotBlank()) parts.add(country)
            return if (parts.isNotEmpty()) parts.joinToString(", ") else "World"
        }

    val streamPlayUrl: String
        get() = if (urlResolved.isNotBlank()) urlResolved else url
}

data class StationCluster(
    val id: String,
    val centerLat: Double,
    val centerLon: Double,
    val screenX: Float = 0f,
    val screenY: Float = 0f,
    val isVisible: Boolean = true,
    val stations: List<RadioStation>,
    val primaryGenre: StationGenre = StationGenre.POP
) {
    val count: Int get() = stations.size
    val isSingleStation: Boolean get() = count == 1
    val singleStation: RadioStation? get() = stations.firstOrNull()
}

data class FilterCriteria(
    val query: String = "",
    val country: String = "",
    val countryCode: String = "",
    val genre: String = "",
    val language: String = "",
    val minBitrate: Int = 0,
    val onlyWorking: Boolean = true,
    val onlyWithCoords: Boolean = true
)

sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Loading(val station: RadioStation) : PlaybackState
    data class Playing(val station: RadioStation, val nowPlaying: String = "") : PlaybackState
    data class Paused(val station: RadioStation, val nowPlaying: String = "") : PlaybackState
    data class Error(val station: RadioStation, val message: String) : PlaybackState
}
