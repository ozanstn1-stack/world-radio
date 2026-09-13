package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RadioStation

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String,
    val favicon: String,
    val tags: String,
    val country: String,
    val countryCode: String,
    val state: String,
    val language: String,
    val votes: Int,
    val codec: String,
    val bitrate: Int,
    val latitude: Double?,
    val longitude: Double?,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): RadioStation = RadioStation(
        stationUuid = stationUuid,
        name = name,
        url = url,
        urlResolved = urlResolved,
        homepage = homepage,
        favicon = favicon,
        tags = tags,
        country = country,
        countryCode = countryCode,
        state = state,
        language = language,
        votes = votes,
        codec = codec,
        bitrate = bitrate,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(station: RadioStation): FavoriteEntity = FavoriteEntity(
            stationUuid = station.stationUuid,
            name = station.name,
            url = station.url,
            urlResolved = station.urlResolved,
            homepage = station.homepage,
            favicon = station.favicon,
            tags = station.tags,
            country = station.country,
            countryCode = station.countryCode,
            state = station.state,
            language = station.language,
            votes = station.votes,
            codec = station.codec,
            bitrate = station.bitrate,
            latitude = station.latitude,
            longitude = station.longitude
        )
    }
}

@Entity(tableName = "recent_stations")
data class RecentEntity(
    @PrimaryKey val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String,
    val favicon: String,
    val tags: String,
    val country: String,
    val countryCode: String,
    val state: String,
    val language: String,
    val votes: Int,
    val codec: String,
    val bitrate: Int,
    val latitude: Double?,
    val longitude: Double?,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): RadioStation = RadioStation(
        stationUuid = stationUuid,
        name = name,
        url = url,
        urlResolved = urlResolved,
        homepage = homepage,
        favicon = favicon,
        tags = tags,
        country = country,
        countryCode = countryCode,
        state = state,
        language = language,
        votes = votes,
        codec = codec,
        bitrate = bitrate,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(station: RadioStation): RecentEntity = RecentEntity(
            stationUuid = station.stationUuid,
            name = station.name,
            url = station.url,
            urlResolved = station.urlResolved,
            homepage = station.homepage,
            favicon = station.favicon,
            tags = station.tags,
            country = station.country,
            countryCode = station.countryCode,
            state = station.state,
            language = station.language,
            votes = station.votes,
            codec = station.codec,
            bitrate = station.bitrate,
            latitude = station.latitude,
            longitude = station.longitude,
            playedAt = System.currentTimeMillis()
        )
    }
}

@Entity(tableName = "cached_stations")
data class CachedStationEntity(
    @PrimaryKey val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String,
    val favicon: String,
    val tags: String,
    val country: String,
    val countryCode: String,
    val state: String,
    val language: String,
    val votes: Int,
    val codec: String,
    val bitrate: Int,
    val latitude: Double?,
    val longitude: Double?,
    val clickCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): RadioStation = RadioStation(
        stationUuid = stationUuid,
        name = name,
        url = url,
        urlResolved = urlResolved,
        homepage = homepage,
        favicon = favicon,
        tags = tags,
        country = country,
        countryCode = countryCode,
        state = state,
        language = language,
        votes = votes,
        codec = codec,
        bitrate = bitrate,
        latitude = latitude,
        longitude = longitude,
        clickCount = clickCount
    )

    companion object {
        fun fromDomain(station: RadioStation): CachedStationEntity = CachedStationEntity(
            stationUuid = station.stationUuid,
            name = station.name,
            url = station.url,
            urlResolved = station.urlResolved,
            homepage = station.homepage,
            favicon = station.favicon,
            tags = station.tags,
            country = station.country,
            countryCode = station.countryCode,
            state = station.state,
            language = station.language,
            votes = station.votes,
            codec = station.codec,
            bitrate = station.bitrate,
            latitude = station.latitude,
            longitude = station.longitude,
            clickCount = station.clickCount
        )
    }
}
