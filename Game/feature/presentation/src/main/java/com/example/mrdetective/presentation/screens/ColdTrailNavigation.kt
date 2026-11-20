package com.example.mrdetective.presentation.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mrdetective.presentation.ui.components.AppButton
import com.example.mrdetective.presentation.ui.components.AppButtonStyle



private enum class Tab(val route: String, val label: String, val icon: Int) {
    Cases("cases", "Дела", R.drawable.ic_cases),
    Profile("profile", "Профиль", R.drawable.ic_profile),
}

@Composable
fun ColdTrailNavigation(auth: AuthViewModel, cases: () -> CasesViewModel) {
    val state by auth.state.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    if (state.signedIn) {
        val viewModel = cases()
        DisposableEffect(viewModel) {
            onDispose { viewModel.reset() }
        }
        SignedInNavigation(viewModel, state, auth::signOut)
    } else {
        Scaffold(containerColor = colors.bg, contentColor = colors.text) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (state.checkingSession) {
                    SplashScreen()
                } else {
                    AuthScreen(state, auth::submit, auth::clearMessage)
                }
            }
        }
    }
}

@Composable
private fun SignedInNavigation(viewModel: CasesViewModel, auth: AuthUiState, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    LaunchedEffect(route, viewModel) {
        if (route == Tab.Cases.route) viewModel.reload()
    }
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
                                        tint = if (selected) colors.text else colors.text2)
                                    Text(tab.label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController,
            startDestination = Tab.Cases.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                slideInHorizontally(tween(360)) { width -> width }
            },
            exitTransition = {
                slideOutHorizontally(tween(360)) { width -> -width }
            },
            popEnterTransition = {
                slideInHorizontally(tween(360)) { width -> -width }
            },
            popExitTransition = {
                slideOutHorizontally(tween(360)) { width -> width }
            },
        ) {
            composable(Tab.Cases.route) {
                CasesRoute(viewModel) { navController.navigate(InvestigationRoute(it)) }
            }
            composable<InvestigationRoute> {
                InvestigationScreen(hiltViewModel(), { navController.popBackStack() }, viewModel::updateCaseStatus) { caseId, suspectId ->
                    navController.navigate(InterrogationRoute(caseId, suspectId))
                }
            }
            composable<InterrogationRoute> {
                InterrogationScreen(hiltViewModel()) { navController.popBackStack() }
            }
            composable(Tab.Profile.route) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp)) {
                    Text("Профиль", style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(bottom = 16.dp))
                    auth.message?.let { Text(it, color = colors.error) }
                    AppButton(
                        text = if (auth.busy) "Выходим…" else "Выйти из аккаунта",
                        onClick = onSignOut,
                        enabled = !auth.busy,
                        style = AppButtonStyle.Outlined,
                    )
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
                color = colors.accent,
                trackColor = colors.surface2,
            )
            Spacer(Modifier.height(12.dp))
            Text("Проверяем сеанс…", style = MaterialTheme.typography.bodySmall)
        }
    }
}
