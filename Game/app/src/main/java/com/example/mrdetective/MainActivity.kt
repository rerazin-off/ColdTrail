package com.example.mrdetective

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.mrdetective.presentation.ui.ColdTrailNavigation
import com.example.mrdetective.presentation.ui.CasesViewModel
import com.example.mrdetective.presentation.ui.theme.ColdTrailTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val casesViewModel: CasesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            ColdTrailTheme(darkTheme = true) {
                ColdTrailNavigation(casesViewModel)
            }
        }
    }
}
