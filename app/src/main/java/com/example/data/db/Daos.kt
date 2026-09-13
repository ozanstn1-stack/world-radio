package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE stationUuid = :stationUuid LIMIT 1")
    fun getFavoriteByUuid(stationUuid: String): Flow<FavoriteEntity?>

    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stationUuid = :stationUuid)")
    fun isFavoriteFlow(stationUuid: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stationUuid = :stationUuid)")
    suspend fun isFavorite(stationUuid: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE stationUuid = :stationUuid")
    suspend fun deleteFavorite(stationUuid: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_stations ORDER BY playedAt DESC LIMIT 50")
    fun getRecentStations(): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)

    @Query("DELETE FROM recent_stations")
    suspend fun clearAll()
}

@Dao
interface CachedStationDao {
    @Query("SELECT * FROM cached_stations WHERE latitude IS NOT NULL AND longitude IS NOT NULL LIMIT 500")
    fun getCachedGeoStations(): Flow<List<CachedStationEntity>>

    @Query("SELECT * FROM cached_stations WHERE name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR country LIKE '%' || :query || '%' OR state LIKE '%' || :query || '%' LIMIT 100")
    suspend fun searchCached(query: String): List<CachedStationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<CachedStationEntity>)

    @Query("DELETE FROM cached_stations WHERE cachedAt < :expiryTime")
    suspend fun clearExpired(expiryTime: Long)

    @Query("DELETE FROM cached_stations")
    suspend fun clearAll()
}
