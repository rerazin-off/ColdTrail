package com.example.mrdetective.presentation.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween

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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mrdetective.domain.usecases.clues.ClueChallengeKind
import com.example.mrdetective.presentation.R
import com.example.mrdetective.presentation.ui.theme.AppWindow
import com.example.mrdetective.presentation.ui.theme.LocalAppWindow
import com.example.mrdetective.presentation.ui.theme.isMediumWidth
import com.example.mrdetective.presentation.ui.theme.AppShapes
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.usesWideLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle



private enum class Tab(val route: String, val label: Int, val icon: Int) {
    Cases("cases", R.string.cases, R.drawable.ic_cases),
    Profile("profile", R.string.profile, R.drawable.ic_profile),
}

@Composable
fun ColdTrailNavigation(auth: AuthViewModel, cases: () -> CasesViewModel) = AppWindow {
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
    val showingInventory = entry?.destination?.hasRoute<InventoryRoute>() == true
    val showingMorse = entry?.destination?.hasRoute<MorseRoute>() == true
    val caseId = entry?.let { current ->
        when {
            current.destination.hasRoute<InvestigationRoute>() -> current.toRoute<InvestigationRoute>().caseId
            showingInventory -> current.toRoute<InventoryRoute>().caseId
            showingMorse -> current.toRoute<MorseRoute>().caseId
            current.destination.hasRoute<CodeChallengeRoute>() -> current.toRoute<CodeChallengeRoute>().caseId
            else -> null
        }
    }
    LaunchedEffect(route, viewModel) {
        if (route == Tab.Cases.route) viewModel.reload()
    }
    val colors = AppTheme.colors
    val sideNavigation = usesWideLayout()
    val verticalTransitions = LocalAppWindow.current.isMediumWidth ||
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val forwardSlide = if (verticalTransitions) SlideDirection.Up else SlideDirection.Left
    val backSlide = if (verticalTransitions) SlideDirection.Down else SlideDirection.Right
    val showNavigation = caseId != null || Tab.entries.any { it.route == route }
    val navigationItems: @Composable (Modifier) -> Unit = { itemModifier ->
        if (caseId != null) {
            NavigationItem(stringResource(R.string.cases), R.drawable.ic_cases, !showingInventory, itemModifier) {
                if (showingInventory) navController.popBackStack<InvestigationRoute>(inclusive = false)
            }
            NavigationItem(stringResource(R.string.inventory), R.drawable.ic_inventory, showingInventory, itemModifier) {
                if (!showingInventory) navController.navigate(InventoryRoute(caseId)) { launchSingleTop = true }
            }
        } else {
            Tab.entries.forEach { tab ->
                val selected = route == tab.route
                NavigationItem(stringResource(tab.label), tab.icon, selected, itemModifier) {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(Tab.Cases.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
    Scaffold(
        containerColor = colors.bg,
        contentColor = colors.text,
        bottomBar = {
            if (showNavigation && !sideNavigation) {
                Surface(
                    modifier = Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
                    color = colors.surface,
                    shape = AppShapes.control,
                    border = BorderStroke(1.dp, colors.outline),
                ) {
                    Row(Modifier.fillMaxWidth().padding(4.dp).selectableGroup()) {
                        navigationItems(Modifier.weight(1f))
                    }
                }
            }
        },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (showNavigation && sideNavigation) {
                Surface(
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .widthIn(min = 100.dp, max = 144.dp)
                        .padding(8.dp),
                    shape = AppShapes.control,
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline),
                ) {
                    Column(Modifier.width(128.dp).padding(4.dp).selectableGroup()) {
                        navigationItems(Modifier.fillMaxWidth())
                    }
                }
            }
            NavHost(
                navController,
                startDestination = Tab.Cases.route,
                modifier = Modifier.weight(1f).clipToBounds(),
                enterTransition = {
                    slideIntoContainer(forwardSlide, tween(800))
                },
                exitTransition = {
                    slideOutOfContainer(forwardSlide, tween(800))
                },
                popEnterTransition = {
                    slideIntoContainer(backSlide, tween(800))
                },
                popExitTransition = {
                    slideOutOfContainer(backSlide, tween(800))
                },
            ) {
                composable(Tab.Cases.route) {
                    CasesRoute(viewModel) { navController.navigate(InvestigationRoute(it)) }
                }
                composable<InvestigationRoute> {
                    InvestigationScreen(
                        viewModel = hiltViewModel(),
                        onBack = { navController.popBackStack() },
                        onStatusChanged = viewModel::updateCaseStatus,
                        onFindClue = { navController.navigate(ChooseClueChallengeRoute(it)) { launchSingleTop = true } },
                        onAccusation = { navController.navigate(AccusationRoute(it)) },
                        onInterrogate = { caseId, suspectId -> navController.navigate(InterrogationRoute(caseId, suspectId)) },
                    )
                }
                composable<InventoryRoute> {
                    InventoryScreen(hiltViewModel()) { navController.popBackStack() }
                }
                composable<ChooseClueChallengeRoute> { pickEntry ->
                    val picker: ClueChallengePickerViewModel = hiltViewModel()
                    ChooseClueChallengeScreen(
                        picker,
                        hiltViewModel(),
                        hiltViewModel(),
                        onChoice = { choice ->
                            if (navController.currentBackStackEntry == pickEntry) {
                                val options = navOptions {
                                    popUpTo<ChooseClueChallengeRoute> { inclusive = true }
                                }
                                when (choice.kind) {
                                    ClueChallengeKind.Camera -> navController.navigate(CameraClueRoute(picker.caseId, choice.puzzleId), options)
                                    ClueChallengeKind.Morse -> navController.navigate(MorseRoute(picker.caseId, choice.puzzleId), options)
                                    ClueChallengeKind.Code -> navController.navigate(CodeChallengeRoute(picker.caseId, choice.puzzleId), options)
                                }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<CameraClueRoute> { cameraEntry ->
                    CameraClueScreen(hiltViewModel(), hiltViewModel()) {
                        if (navController.currentBackStackEntry == cameraEntry) navController.popBackStack()
                    }
                }
                composable<MorseRoute> { morseEntry ->
                    MorseScreen(hiltViewModel(), hiltViewModel()) {
                        if (navController.currentBackStackEntry == morseEntry) navController.popBackStack()
                    }
                }
                composable<CodeChallengeRoute> { codeEntry ->
                    CodeChallengeScreen(hiltViewModel()) {
                        if (navController.currentBackStackEntry == codeEntry) navController.popBackStack()
                    }
                }
                composable<DeviceSettingsRoute> {
                    DeviceSettingsScreen(hiltViewModel(), hiltViewModel()) { navController.popBackStack() }
                }
                composable<AccusationRoute> {
                    AccusationScreen(hiltViewModel(), { navController.popBackStack() }, viewModel::updateCaseStatus)
                }
                composable<InterrogationRoute> {
                    InterrogationScreen(hiltViewModel()) { navController.popBackStack() }
                }
                composable(Tab.Profile.route) {
                    ProfileScreen(
                        hiltViewModel(),
                        auth,
                        onBack = { navController.popBackStack() },
                        onSettings = { navController.navigate(DeviceSettingsRoute) { launchSingleTop = true } },
                        onSignOut = onSignOut,
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(label: String, icon: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier,
        shape = AppShapes.control,
        color = if (selected) colors.surface2 else colors.surface,
    ) {
        Column(
            modifier = Modifier.selectable(selected, role = Role.Tab, onClick = onClick)
                .heightIn(min = 48.dp).padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(painterResource(icon), null, Modifier.size(16.dp), tint = if (selected) colors.text else colors.text2)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SplashScreen() {
    val colors = AppTheme.colors
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.app_title), style = MaterialTheme.typography.displayLarge)
            Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(56.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(190.dp),
                color = colors.accent,
                trackColor = colors.surface2,
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.checking_session), style = MaterialTheme.typography.bodySmall)
        }
    }
}
