package com.example.mrdetective.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import kotlinx.coroutines.delay

private const val SPLASH = "splash"

private enum class Tab(val route: String, val label: String, val icon: Int) {
    Cases("cases", "Дела", R.drawable.ic_cases),
    Profile("profile", "Профиль", R.drawable.ic_profile),
}

@Composable
fun ColdTrailNavigation(viewModel: CasesViewModel) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val colors = AppTheme.colors
    Scaffold(
        containerColor = colors.bg,
        contentColor = colors.text,
        bottomBar = {
            if (Tab.entries.any { it.route == route }) {
                Surface(
                    modifier = Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
                    color = colors.surface,
                    shape = AppShapes.control,
                    border = BorderStroke(1.dp, colors.outline),
                ) {
                    Row(Modifier.fillMaxWidth().padding(4.dp).selectableGroup()) {
                        Tab.entries.forEach { tab ->
                            val selected = route == tab.route
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = AppShapes.control,
                                color = if (selected) colors.surface2 else colors.surface,
                            ) {
                                Column(
                                    modifier = Modifier.selectable(selected, role = Role.Tab) {
                                        if (!selected) {
                                            navController.navigate(tab.route) {
                                                popUpTo(Tab.Cases.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }.heightIn(min = 48.dp).padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(painterResource(tab.icon), null, Modifier.size(16.dp),
                                        tint = if (selected) colors.onAccent else colors.text2)
                                    Text(tab.label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = SPLASH, modifier = Modifier.padding(padding)) {
            composable(SPLASH) {
                SplashScreen()
                LaunchedEffect(Unit) {
                    delay(900)
                    navController.navigate(Tab.Cases.route) {
                        popUpTo(SPLASH) { inclusive = true }
                    }
                }
            }
            composable(Tab.Cases.route) { CasesRoute(viewModel) }
            composable(Tab.Profile.route) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp)) {
                    Text("Профиль", style = MaterialTheme.typography.displayLarge)
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    val colors = AppTheme.colors
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Холодный след", style = MaterialTheme.typography.displayLarge)
            Text("Каждая деталь оставляет след", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(56.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(190.dp),
                color = colors.onAccent,
                trackColor = colors.surface2,
            )
            Spacer(Modifier.height(12.dp))
            Text("Проверяем материалы дела…", style = MaterialTheme.typography.bodySmall)
        }
    }
}
