package com.example.mrdetective

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.example.mrdetective.presentation.ui.AuthViewModel
import com.example.mrdetective.presentation.ui.CasesViewModel
import com.example.mrdetective.presentation.ui.ColdTrailNavigation
import com.example.mrdetective.presentation.ui.theme.AppTheme
import com.example.mrdetective.presentation.ui.theme.ColdTrailTheme
import dagger.hilt.android.AndroidEntryPoint

/** Размещает игровой интерфейс Compose в Android-активности. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val casesViewModel: CasesViewModel by viewModels()

    /** Настраивает оформление и навигацию в интерфейсе ColdTrail. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColdTrailTheme {
                val lightSystemBars = !AppTheme.colors.isDark
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = lightSystemBars
                        isAppearanceLightNavigationBars = lightSystemBars
                    }
                }
                ColdTrailNavigation(authViewModel) { casesViewModel }
            }
        }
    }
}
