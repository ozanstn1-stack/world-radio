package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface RadioBrowserApi {

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("country") country: String? = null,
        @Query("countrycode") countryCode: String? = null,
        @Query("tag") tag: String? = null,
        @Query("language") language: String? = null,
        @Query("has_geo_info") hasGeoInfo: Boolean? = true,
        @Query("is_https") isHttps: Boolean? = null,
        @Query("limit") limit: Int = 100,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStationDto>

    @GET("json/stations/topvote/100")
    suspend fun getTopVotedStations(): List<RadioStationDto>

    @GET("json/stations/topclick/100")
    suspend fun getTopClickedStations(): List<RadioStationDto>

    @GET("json/countries")
    suspend fun getCountries(
        @Query("order") order: String = "stationcount",
        @Query("reverse") reverse: Boolean = true
    ): List<CountryDto>
}
