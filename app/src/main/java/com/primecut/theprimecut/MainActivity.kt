package com.primecut.theprimecut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.ui.screen.HomeScreen
import com.primecut.theprimecut.ui.screen.ProfileScreen
import com.primecut.theprimecut.ui.screen.SettingsScreen
import com.primecut.theprimecut.ui.theme.ThePrimeCutTheme
import com.primecut.theprimecut.ui.screen.FoodListScreen
import android.app.Application
import androidx.compose.material.icons.filled.Add
import com.primecut.theprimecut.ui.screen.MealEntryScreen
import com.primecut.theprimecut.ui.screen.OverviewScreen
import com.primecut.theprimecut.di.AppContainer
import com.primecut.theprimecut.util.AppSession
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory


sealed class Screen(val title: String, val icon: ImageVector) {
    object Home : Screen("Home", Icons.Default.Home)
    object Profile : Screen("Profile", Icons.Default.Person)
    object Settings : Screen("Settings", Icons.Default.Settings)
    object FoodList : Screen("Food List", Icons.Default.Info)
    object MealEntry : Screen("Meal Entry", Icons.Default.Add)
    object Overview : Screen("Overview", Icons.Default.Groups)
}

class PrimeCutApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        AppSession.init(this)
        container = AppContainer(this)
    }
}

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
fun MainScreen(
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val tabs: List<Screen> = remember { listOf(Screen.Home, Screen.FoodList, Screen.MealEntry, Screen.Overview) }

    val profile by userProfileViewModel.userProfile.collectAsState()
    val allProfiles by userProfileViewModel.allProfiles.collectAsState()

    val userNames = remember(allProfiles) { allProfiles.map { it.userName } }
    val pagerState = rememberPagerState(pageCount = { userNames.size })
    var initialSyncDone by remember { mutableStateOf(false) }

    // Sync Pager to ViewModel (User swiping)
    LaunchedEffect(pagerState.currentPage) {
        if (initialSyncDone && userNames.isNotEmpty() && pagerState.currentPage < userNames.size) {
            val selectedUser = userNames[pagerState.currentPage]
            if (selectedUser != (profile?.userName ?: "")) {
                userProfileViewModel.loadProfile(selectedUser)
            }
        }
    }

    // Sync ViewModel to Pager (Initial and external changes)
    LaunchedEffect(profile?.userName, userNames) {
        val currentProfile = profile
        if (userNames.isNotEmpty() && currentProfile != null) {
            val targetPage = userNames.indexOf(currentProfile.userName)
            if (targetPage != -1 && targetPage != pagerState.currentPage) {
                pagerState.scrollToPage(targetPage)
            }
            initialSyncDone = true
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.Profile && currentScreen != Screen.Settings) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val userName = userNames.getOrNull(page) ?: "The Prime Cut"
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            if (userNames.size > 1) {
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(userNames.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentScreen = Screen.Settings }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { currentScreen = Screen.Profile }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        IconButton(onClick = { currentScreen = Screen.Home }) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Back Home")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
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
                Screen.Home -> HomeScreen(
                    onProfileClick = { currentScreen = Screen.Profile },
                    onSettingsClick = { currentScreen = Screen.Settings }
                )
                Screen.Profile -> ProfileScreen()
                Screen.Settings -> SettingsScreen()
                Screen.FoodList -> FoodListScreen()
                Screen.MealEntry -> MealEntryScreen()
                Screen.Overview -> OverviewScreen(currentProfile = profile)
            }
        }
    }
}
