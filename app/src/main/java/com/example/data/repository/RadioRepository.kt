package com.example.data.repository

import android.util.Log
import com.example.data.api.ApiClient
import com.example.data.api.RadioBrowserApi
import com.example.data.db.AppDatabase
import com.example.data.db.CachedStationEntity
import com.example.data.db.FavoriteEntity
import com.example.data.db.RecentEntity
import com.example.model.FilterCriteria
import com.example.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RadioRepository(private val database: AppDatabase) {

    private val favoriteDao = database.favoriteDao()
    private val recentDao = database.recentDao()
    private val cachedStationDao = database.cachedStationDao()

    private val api: RadioBrowserApi get() = ApiClient.service
    private val fallbackApi: RadioBrowserApi get() = ApiClient.fallbackService

    // In-memory quick cache for fast responsiveness
    private val memoryStationCache = mutableMapOf<String, RadioStation>()

    init {
        // Pre-populate with curated world radio beacons
        DefaultCuratedStations.stations.forEach {
            memoryStationCache[it.stationUuid] = it
        }
    }

    val favorites: Flow<List<RadioStation>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toDomain() }
    }

    val recents: Flow<List<RadioStation>> = recentDao.getRecentStations().map { list ->
        list.map { it.toDomain() }
    }

    fun isFavorite(stationUuid: String): Flow<Boolean> {
        return favoriteDao.isFavoriteFlow(stationUuid)
    }

    suspend fun toggleFavorite(station: RadioStation): Boolean = withContext(Dispatchers.IO) {
        val exists = favoriteDao.isFavorite(station.stationUuid)
        if (exists) {
            favoriteDao.deleteFavorite(station.stationUuid)
            false
        } else {
            favoriteDao.insertFavorite(FavoriteEntity.fromDomain(station))
            true
        }
    }

    suspend fun recordPlayedStation(station: RadioStation) = withContext(Dispatchers.IO) {
        recentDao.insertRecent(RecentEntity.fromDomain(station))
        recentDao.trimToLimit()
    }

    suspend fun clearRecentStations() = withContext(Dispatchers.IO) {
        recentDao.clearAll()
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cachedStationDao.clearAll()
        memoryStationCache.clear()
        DefaultCuratedStations.stations.forEach {
            memoryStationCache[it.stationUuid] = it
        }
    }

    /**
     * Loads stations for the globe view.
     * Emits cached/curated stations first for instant display, then fetches updated stations from API.
     */
    fun getGlobeStations(filter: FilterCriteria = FilterCriteria()): Flow<List<RadioStation>> = flow {
        // 1. Emit currently cached or default stations
        val initialList = if (memoryStationCache.isNotEmpty()) {
            memoryStationCache.values.toList()
        } else {
            DefaultCuratedStations.stations
        }
        emit(filterStations(initialList, filter))

        // 2. Fetch from API
        try {
            val apiStations = fetchApiStations(filter)
            if (apiStations.isNotEmpty()) {
                apiStations.forEach { memoryStationCache[it.stationUuid] = it }
                // Persist into Room cache in background
                cachedStationDao.insertAll(apiStations.map { CachedStationEntity.fromDomain(it) })
                emit(filterStations(memoryStationCache.values.toList(), filter))
            }
        } catch (e: Exception) {
            Log.w("RadioRepository", "Primary API fetch failed, checking fallback: ${e.message}")
            try {
                val fallbackStations = fallbackApi.getTopVotedStations().map { it.toDomain() }
                    .filter { it.latitude != null && it.longitude != null }
                if (fallbackStations.isNotEmpty()) {
                    fallbackStations.forEach { memoryStationCache[it.stationUuid] = it }
                    emit(filterStations(memoryStationCache.values.toList(), filter))
                }
            } catch (fallbackEx: Exception) {
                Log.e("RadioRepository", "Fallback API fetch also failed: ${fallbackEx.message}")
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchApiStations(filter: FilterCriteria): List<RadioStation> {
        val list = mutableListOf<RadioStation>()
        try {
            val mainResults = api.searchStations(
                name = filter.query.ifBlank { null },
                country = filter.country.ifBlank { null },
                countryCode = filter.countryCode.ifBlank { null },
                tag = filter.genre.ifBlank { null },
                language = filter.language.ifBlank { null },
                hasGeoInfo = true,
                limit = 500,
                order = "votes"
            ).map { it.toDomain() }.filter { it.latitude != null && it.longitude != null }
            list.addAll(mainResults)
        } catch (e: Exception) {
            Log.w("RadioRepository", "Primary searchStations error: ${e.message}")
            try {
                val fallbackResults = fallbackApi.searchStations(
                    name = filter.query.ifBlank { null },
                    country = filter.country.ifBlank { null },
                    countryCode = filter.countryCode.ifBlank { null },
                    tag = filter.genre.ifBlank { null },
                    language = filter.language.ifBlank { null },
                    hasGeoInfo = true,
                    limit = 300,
                    order = "votes"
                ).map { it.toDomain() }.filter { it.latitude != null && it.longitude != null }
                list.addAll(fallbackResults)
            } catch (_: Exception) {}
        }

        // Enrich with top clicked and regional stations if global exploration
        if (filter.query.isBlank() && filter.genre.isBlank() && filter.country.isBlank()) {
            try {
                val topClicked = api.getTopClickedStations(300)
                    .map { it.toDomain() }
                    .filter { it.latitude != null && it.longitude != null }
                list.addAll(topClicked)
            } catch (_: Exception) {}

            try {
                val trStations = api.getStationsByCountryCode("TR", limit = 150)
                    .map { it.toDomain() }
                    .filter { it.latitude != null && it.longitude != null }
                list.addAll(trStations)
            } catch (_: Exception) {}
        }

        return list.distinctBy { it.stationUuid }
    }

    suspend fun searchStations(query: String, filter: FilterCriteria = FilterCriteria()): List<RadioStation> = withContext(Dispatchers.IO) {
        if (query.isBlank() && filter.genre.isBlank() && filter.country.isBlank()) {
            return@withContext memoryStationCache.values.toList().take(50)
        }
        try {
            val results = api.searchStations(
                name = query.ifBlank { null },
                country = filter.country.ifBlank { null },
                tag = filter.genre.ifBlank { null },
                language = filter.language.ifBlank { null },
                hasGeoInfo = if (filter.onlyWithCoords) true else null,
                limit = 100,
                order = "clickcount"
            ).map { it.toDomain() }

            results.forEach { memoryStationCache[it.stationUuid] = it }
            filterStations(results, filter)
        } catch (e: Exception) {
            Log.w("RadioRepository", "Online search failed, checking local cache: ${e.message}")
            val localResults = cachedStationDao.searchCached(query).map { it.toDomain() }
            if (localResults.isNotEmpty()) {
                filterStations(localResults, filter)
            } else {
                filterStations(memoryStationCache.values.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true) ||
                    it.tags.contains(query, ignoreCase = true) ||
                    it.state.contains(query, ignoreCase = true)
                }, filter)
            }
        }
    }

    suspend fun getStationsInRegion(cityOrCountry: String): List<RadioStation> = withContext(Dispatchers.IO) {
        try {
            val results = api.searchStations(
                country = cityOrCountry,
                limit = 40,
                order = "votes"
            ).map { it.toDomain() }
            if (results.isNotEmpty()) {
                results.forEach { memoryStationCache[it.stationUuid] = it }
                return@withContext results
            }
        } catch (e: Exception) {
            Log.w("RadioRepository", "getStationsInRegion failed: ${e.message}")
        }
        // Fallback filter
        memoryStationCache.values.filter {
            it.country.contains(cityOrCountry, ignoreCase = true) ||
            it.state.contains(cityOrCountry, ignoreCase = true)
        }.take(30)
    }

    private fun filterStations(stations: List<RadioStation>, filter: FilterCriteria): List<RadioStation> {
        return stations.filter { station ->
            val matchesWorking = !filter.onlyWorking || station.isWorking
            val matchesCoords = !filter.onlyWithCoords || (station.latitude != null && station.longitude != null)
            val matchesBitrate = filter.minBitrate == 0 || station.bitrate >= filter.minBitrate
            val matchesGenre = filter.genre.isBlank() || station.tags.contains(filter.genre, ignoreCase = true)
            val matchesCountry = filter.country.isBlank() || station.country.equals(filter.country, ignoreCase = true)
            val matchesLanguage = filter.language.isBlank() || station.language.contains(filter.language, ignoreCase = true)
            val matchesQuery = filter.query.isBlank() ||
                    station.name.contains(filter.query, ignoreCase = true) ||
                    station.state.contains(filter.query, ignoreCase = true) ||
                    station.country.contains(filter.query, ignoreCase = true) ||
                    station.tags.contains(filter.query, ignoreCase = true)

            matchesWorking && matchesCoords && matchesBitrate && matchesGenre && matchesCountry && matchesLanguage && matchesQuery
        }
    }
}
