package com.example.data.api

import com.example.model.RadioStation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RadioStationDto(
    @Json(name = "stationuuid") val stationuuid: String,
    @Json(name = "name") val name: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "url_resolved") val urlResolved: String?,
    @Json(name = "homepage") val homepage: String?,
    @Json(name = "favicon") val favicon: String?,
    @Json(name = "tags") val tags: String?,
    @Json(name = "country") val country: String?,
    @Json(name = "countrycode") val countrycode: String?,
    @Json(name = "state") val state: String?,
    @Json(name = "language") val language: String?,
    @Json(name = "votes") val votes: Int?,
    @Json(name = "codec") val codec: String?,
    @Json(name = "bitrate") val bitrate: Int?,
    @Json(name = "geo_lat") val geoLat: Double?,
    @Json(name = "geo_long") val geoLong: Double?,
    @Json(name = "clickcount") val clickcount: Int?,
    @Json(name = "lastcheckok") val lastcheckok: Int?
) {
    fun toDomain(): RadioStation {
        return RadioStation(
            stationUuid = stationuuid,
            name = name?.trim()?.ifBlank { "Radio Station" } ?: "Radio Station",
            url = url.orEmpty(),
            urlResolved = urlResolved.orEmpty().ifBlank { url.orEmpty() },
            homepage = homepage.orEmpty(),
            favicon = favicon.orEmpty(),
            tags = tags.orEmpty(),
            country = country.orEmpty(),
            countryCode = countrycode.orEmpty(),
            state = state.orEmpty(),
            language = language.orEmpty(),
            votes = votes ?: 0,
            codec = codec.orEmpty(),
            bitrate = bitrate ?: 0,
            latitude = geoLat,
            longitude = geoLong,
            clickCount = clickcount ?: 0,
            isWorking = (lastcheckok ?: 1) == 1
        )
    }
}

@JsonClass(generateAdapter = true)
data class CountryDto(
    @Json(name = "name") val name: String,
    @Json(name = "iso_3166_1") val isoCode: String?,
    @Json(name = "stationcount") val stationCount: Int?
)
