package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.RadioApp
import com.example.ui.theme.WorldRadioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themePreference by remember { mutableStateOf<Boolean?>(null) }
            val isDark = themePreference ?: isSystemInDarkTheme()

            WorldRadioTheme(darkTheme = isDark) {
                RadioApp(
                    isDarkTheme = themePreference,
                    onThemeChange = { themePreference = it }
                )
            }
        }
    }
}
