package com.example.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.RadioApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PRIMARY_BASE_URL = "https://de1.api.radio-browser.info/"
    private const val FALLBACK_BASE_URL = "https://all.api.radio-browser.info/"
    private const val CACHE_SIZE_BYTES = 20L * 1024L * 1024L // 20 MB

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun isNetworkAvailable(): Boolean {
        return try {
            val app = RadioApplication.instance
            val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(network) ?: return false
            actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Interceptor to inject caching headers:
     * - Online: caches fresh responses for 5 minutes (max-age=300).
     * - Offline: serves stale cached responses up to 7 days (max-stale=604800).
     */
    private val cacheInterceptor = Interceptor { chain ->
        var request = chain.request()
        val online = isNetworkAvailable()

        if (!online) {
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=" + (60 * 60 * 24 * 7))
                .build()
        }

        val originalResponse = chain.proceed(request)

        if (online) {
            originalResponse.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "public, max-age=" + (60 * 5))
                .build()
        } else {
            originalResponse.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "public, only-if-cached, max-stale=" + (60 * 60 * 24 * 7))
                .build()
        }
    }

    /**
     * Global Error Handling Interceptor for Network Timeouts and API Rate Limits (HTTP 429).
     */
    private val globalErrorInterceptor = Interceptor { chain ->
        val request = chain.request()

        val response: Response = try {
            chain.proceed(request)
        } catch (e: SocketTimeoutException) {
            Log.e("ApiClient", "Network timeout connecting to ${request.url}: ${e.message}")
            throw IOException("Radio Browser connection timed out. Please try again.", e)
        } catch (e: UnknownHostException) {
            Log.e("ApiClient", "DNS lookup failure for ${request.url.host}: ${e.message}")
            throw IOException("Unable to reach radio servers. Check your internet connection.", e)
        }

        when (response.code) {
            429 -> {
                val retryAfter = response.header("Retry-After") ?: "60"
                Log.w("ApiClient", "API Rate limit exceeded (429). Retry after: $retryAfter seconds.")
            }
            in 500..599 -> {
                Log.w("ApiClient", "Radio Browser server error: ${response.code} on ${request.url}")
            }
        }

        response
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val cache = try {
            val cacheDir = File(RadioApplication.instance.cacheDir, "radio_api_cache")
            Cache(cacheDir, CACHE_SIZE_BYTES)
        } catch (e: Exception) {
            Log.w("ApiClient", "Could not initialize OkHttp disk cache: ${e.message}")
            null
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "WorldRadioGlobe/1.0 (Android; info@worldradio.app)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(globalErrorInterceptor)
            .addInterceptor(cacheInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)

        if (cache != null) {
            builder.cache(cache)
        }

        builder.build()
    }

    val service: RadioBrowserApi by lazy {
        Retrofit.Builder()
            .baseUrl(PRIMARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RadioBrowserApi::class.java)
    }

    val fallbackService: RadioBrowserApi by lazy {
        Retrofit.Builder()
            .baseUrl(FALLBACK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RadioBrowserApi::class.java)
    }
}

