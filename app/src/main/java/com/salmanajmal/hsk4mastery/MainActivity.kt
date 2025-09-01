package com.salmanajmal.hsk4mastery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.salmanajmal.hsk4mastery.ui.navigation.AppNavigation
import com.salmanajmal.hsk4mastery.ui.theme.HSK4MasteryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HSK4MasteryTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}
