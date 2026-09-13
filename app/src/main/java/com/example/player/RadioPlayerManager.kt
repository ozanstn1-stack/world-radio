package com.example.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.PlaybackState
import com.example.model.RadioStation
import com.example.service.RadioPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioPlayerManager(
    private val context: Context,
    private val onStationPlayed: (RadioStation) -> Unit = {}
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private var currentStation: RadioStation? = null

    private val player: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            val station = currentStation ?: return
            when (state) {
                Player.STATE_BUFFERING -> {
                    _playbackState.value = PlaybackState.Loading(station)
                }
                Player.STATE_READY -> {
                    val isPlaying = player.playWhenReady
                    val nowPlaying = extractNowPlaying(player.mediaMetadata)
                    _playbackState.value = if (isPlaying) {
                        PlaybackState.Playing(station, nowPlaying)
                    } else {
                        PlaybackState.Paused(station, nowPlaying)
                    }
                }
                Player.STATE_ENDED -> {
                    _playbackState.value = PlaybackState.Paused(station)
                }
                Player.STATE_IDLE -> {
                    // Handled if error occurs
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val station = currentStation ?: return
            val nowPlaying = extractNowPlaying(player.mediaMetadata)
            _playbackState.value = if (isPlaying) {
                PlaybackState.Playing(station, nowPlaying)
            } else {
                PlaybackState.Paused(station, nowPlaying)
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val station = currentStation ?: return
            val nowPlaying = extractNowPlaying(mediaMetadata)
            val currentState = _playbackState.value
            if (currentState is PlaybackState.Playing) {
                _playbackState.value = currentState.copy(nowPlaying = nowPlaying)
            } else if (currentState is PlaybackState.Paused) {
                _playbackState.value = currentState.copy(nowPlaying = nowPlaying)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("RadioPlayerManager", "Stream playback error: ${error.errorCodeName}, ${error.message}")
            val station = currentStation ?: return
            val errorMsg = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Network connection failed. Check your internet."
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "Radio stream is currently unavailable (Server Error)."
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
                PlaybackException.ERROR_CODE_DECODING_FAILED -> "Stream format unsupported or corrupted."
                else -> "Cannot connect to stream. The station may be offline."
            }
            _playbackState.value = PlaybackState.Error(station, errorMsg)
        }
    }

    private fun extractNowPlaying(metadata: MediaMetadata): String {
        val title = metadata.title?.toString()?.trim().orEmpty()
        val artist = metadata.artist?.toString()?.trim().orEmpty()
        val displayTitle = metadata.displayTitle?.toString()?.trim().orEmpty()

        return when {
            artist.isNotBlank() && title.isNotBlank() -> "$artist - $title"
            title.isNotBlank() -> title
            displayTitle.isNotBlank() -> displayTitle
            else -> "Now Playing information unavailable"
        }
    }

    fun play(station: RadioStation) {
        currentStation = station
        _playbackState.value = PlaybackState.Loading(station)

        coroutineScope.launch {
            try {
                // Ensure service is triggered for foreground lifecycle
                val serviceIntent = Intent(context, RadioPlaybackService::class.java)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.w("RadioPlayerManager", "Could not start service: ${e.message}")
                }

                val mediaMetadata = MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setSubtitle(station.locationTitle)
                    .setArtist(station.country)
                    .setArtworkUri(if (station.favicon.isNotBlank()) Uri.parse(station.favicon) else null)
                    .build()

                val streamUrl = station.streamPlayUrl
                val mediaItem = MediaItem.Builder()
                    .setUri(streamUrl)
                    .setMediaMetadata(mediaMetadata)
                    .build()

                player.stop()
                player.clearMediaItems()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true

                onStationPlayed(station)
            } catch (e: Exception) {
                Log.e("RadioPlayerManager", "Failed to start play: ${e.message}")
                _playbackState.value = PlaybackState.Error(station, "Failed to start stream: ${e.localizedMessage}")
            }
        }
    }

    fun resume() {
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        when (current) {
            is PlaybackState.Playing -> pause()
            is PlaybackState.Paused -> resume()
            is PlaybackState.Error -> currentStation?.let { play(it) }
            else -> {}
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        player.volume = clamped
    }

    fun retry() {
        currentStation?.let { play(it) }
    }

    fun release() {
        player.removeListener(playerListener)
        player.release()
    }
}
