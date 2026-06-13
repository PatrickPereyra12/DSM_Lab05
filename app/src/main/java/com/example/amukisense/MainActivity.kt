package com.example.amukisense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amukisense.ui.navigation.AppNavigation
import com.example.amukisense.ui.theme.AppTheme
import com.example.amukisense.ui.viewmodel.GpsViewModel
import com.example.amukisense.ui.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = applicationContext as AmukiSenseApp
            val sessionVm: SessionViewModel = viewModel(
                factory = SessionViewModel.Factory(app.sessionManager)
            )
            val gpsVm: GpsViewModel = viewModel(
                factory = GpsViewModel.Factory(app.gpsRepository)
            )

            // Evaluación de la preferencia del tema oscuro almacenada en DataStore
            val isDarkModePref by sessionVm.isDarkMode.collectAsState()
            val darkTheme      = isDarkModePref ?: isSystemInDarkTheme()

            // Lanzamiento de la interfaz utilizando el tema y el enrutador
            AppTheme(darkTheme = darkTheme) {
                AppNavigation(
                    gpsViewModel = gpsVm,
                    sessionViewModel = sessionVm
                )
            }
        }
    }
}