package com.example.amukisense.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.amukisense.AmukiSenseApp
import com.example.amukisense.ui.screens.*
import com.example.amukisense.ui.viewmodel.GpsViewModel
import com.example.amukisense.ui.viewmodel.SessionViewModel

@Composable
fun AppNavigation(
    gpsViewModel: GpsViewModel,
    sessionViewModel: SessionViewModel
) {
    // Observamos el estado de la sesión
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

    // Login Gate: Si está logueado, muestra la app. Si no, muestra el login.
    if (isLoggedIn) {
        MainScaffold(sessionViewModel, gpsViewModel)
    } else {
        LoginScreen(onSubmit = sessionViewModel::login)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    sessionVm: SessionViewModel,
    gpsVm: GpsViewModel
) {
    val nav      = rememberNavController()
    var selected by remember { mutableIntStateOf(0) }
    val username by sessionVm.username.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title   = { Text("AmukiSense — ${username ?: "?"}") },
                actions = {
                    IconButton(onClick = { sessionVm.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // Definición de las 6 pestañas requeridas por el laboratorio
                val tabs = listOf(
                    "gps"     to (Icons.Default.LocationOn   to "GNSS"),
                    "media"   to (Icons.Default.CameraAlt    to "Multimedia"),
                    "audio"   to (Icons.Default.Mic          to "Audio"),
                    "sync"    to (Icons.Default.CloudSync    to "Sync"),
                    "notif"   to (Icons.Default.Notifications to "Notif"),
                    "profile" to (Icons.Default.Person       to "Perfil")
                )
                tabs.forEachIndexed { idx, (route, iconLabel) ->
                    val (icon, label) = iconLabel
                    NavigationBarItem(
                        selected = selected == idx,
                        onClick  = { selected = idx; nav.navigate(route) },
                        icon     = { Icon(icon, contentDescription = null) },
                        label    = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = nav,
            startDestination = "gps",
            modifier         = Modifier.padding(padding)
        ) {
            composable("gps")     { GpsScreen(viewModel = gpsVm) }
            composable("media")   { MediaScreen() }
            composable("audio")   { AudioScreen() }
            composable("sync")    { SyncScreen() }
            composable("notif")   { NotificationsScreen() }
            composable("profile") { ProfileScreen(sessionVm = sessionVm, onLogout = sessionVm::logout) }
        }
    }
}