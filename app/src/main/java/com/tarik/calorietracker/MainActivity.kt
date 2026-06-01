package com.tarik.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tarik.calorietracker.ui.ContactUs
import com.tarik.calorietracker.ui.History
import com.tarik.calorietracker.ui.Profile
import com.tarik.calorietracker.ui.ProfileViewModel
import com.tarik.calorietracker.ui.Tracker
import com.tarik.calorietracker.ui.TrackerViewModel
import com.tarik.calorietracker.ui.components.ContactScreen
import com.tarik.calorietracker.ui.components.HistoryScreen
import com.tarik.calorietracker.ui.components.ProfileScreen
import com.tarik.calorietracker.ui.components.TrackerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val trackerViewModel: TrackerViewModel by viewModels()
        val profileViewModel: ProfileViewModel by viewModels()

        setContent {
            val profileState by profileViewModel.uiState.collectAsState()

            AppTheme(useDarkTheme = profileState.isDarkTheme) {
                val trackerState by trackerViewModel.uiState.collectAsState()

                MainContent(
                    trackerState = trackerState,
                    trackerAction = trackerViewModel::dispatch,
                    profileState = profileState,
                    profileAction = profileViewModel::dispatch
                )
            }
        }
    }
}

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
private fun MainContent(
    trackerState: TrackerViewModel.TrackerUiState,
    trackerAction: (TrackerViewModel.Action) -> Unit,
    profileState: ProfileViewModel.ProfileUiState,
    profileAction: (ProfileViewModel.Action) -> Unit
) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Tracker.route) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(Tracker, History, Profile, ContactUs)
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            currentRoute = screen.route
                            navController.navigate(screen.route) {

                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavigationManager(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            trackerState = trackerState,
            trackerAction = trackerAction,
            profileState = profileState,
            profileAction = profileAction
        )
    }
}

@Composable
private fun NavigationManager(
    navController: NavHostController,
    modifier: Modifier,
    trackerState: TrackerViewModel.TrackerUiState,
    trackerAction: (TrackerViewModel.Action) -> Unit,
    profileState: ProfileViewModel.ProfileUiState,
    profileAction: (ProfileViewModel.Action) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Tracker.route,
        modifier = modifier
    ) {
        composable(Tracker.route) {
            TrackerScreen(trackerState, trackerAction)
        }
        composable(History.route) {
            HistoryScreen(trackerState)
        }
        composable(Profile.route) {
            ProfileScreen(profileState, profileAction)
        }
        composable(ContactUs.route) {
            ContactScreen()
        }
    }
}