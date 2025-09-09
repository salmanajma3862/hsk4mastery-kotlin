package com.salmanajmal.hsk4mastery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.salmanajmal.hsk4mastery.ui.navigation.AppNavigation
import com.salmanajmal.hsk4mastery.ui.theme.HSK4MasteryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        // Use dark icons on transparent system bars so they remain visible over light app bars/backgrounds
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        
        // Ensure status bar is visible and properly configured
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            HSK4MasteryTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
