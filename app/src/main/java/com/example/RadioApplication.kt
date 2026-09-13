package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.RadioRepository
import com.example.player.RadioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RadioApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: RadioRepository
        private set

    lateinit var playerManager: RadioPlayerManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        repository = RadioRepository(database)

        playerManager = RadioPlayerManager(this) { playedStation ->
            applicationScope.launch {
                repository.recordPlayedStation(playedStation)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        playerManager.release()
    }

    companion object {
        lateinit var instance: RadioApplication
            private set
    }
}
