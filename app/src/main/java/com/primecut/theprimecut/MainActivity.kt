package com.primecut.theprimecut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.primecut.theprimecut.ui.screen.HomeScreen
import com.primecut.theprimecut.ui.screen.ProfileScreen
import com.primecut.theprimecut.ui.screen.SettingsScreen
import com.primecut.theprimecut.ui.theme.ThePrimeCutTheme
import dagger.hilt.android.AndroidEntryPoint
import com.primecut.theprimecut.ui.screen.FoodListScreen
import dagger.hilt.android.HiltAndroidApp
import android.app.Application
import androidx.compose.material.icons.filled.Add
import com.primecut.theprimecut.ui.screen.MealEntryScreen

sealed class Screen(val title: String, val icon: ImageVector) {
    object Home : Screen("Home", Icons.Default.Home)
    object Profile : Screen("Profile", Icons.Default.Person)
    object Settings : Screen("Settings", Icons.Default.Settings)
    object FoodList : Screen("Food List", Icons.Default.Info)
    object MealEntry : Screen("Meal Entry", Icons.Default.Add)
}

@HiltAndroidApp
class PrimeCutApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThePrimeCutTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val tabs: List<Screen> = remember { listOf(Screen.Home, Screen.FoodList, Screen.MealEntry, Screen.Profile) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { currentScreen = Screen.Settings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },

                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Profile -> ProfileScreen()
                Screen.Settings -> SettingsScreen()
                Screen.FoodList -> FoodListScreen()
                Screen.MealEntry -> MealEntryScreen()
            }
        }
    }
}
