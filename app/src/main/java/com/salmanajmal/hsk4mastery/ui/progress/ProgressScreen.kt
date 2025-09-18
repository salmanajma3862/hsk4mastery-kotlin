@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)
package com.salmanajmal.hsk4mastery.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.salmanajmal.hsk4mastery.ui.progress.components.StatCard
import com.salmanajmal.hsk4mastery.ui.progress.components.OverallStatsCard
import com.salmanajmal.hsk4mastery.ui.progress.components.HskLevelProgressBar
import com.salmanajmal.hsk4mastery.ui.progress.components.HskLevelsProgressCard

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.fetchStats() }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Progress") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentPadding = PaddingValues(bottom = 220.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header removed to match compact TopAppBar layout
                
                if (state.isLoading) {
                    item {
                        CircularProgressIndicator()
                    }
                    return@LazyColumn
                }

                // HSK Level Progress Section: Single card with all levels
                if (state.hskLevelProgress.isNotEmpty()) {
                    item {
                        HskLevelsProgressCard(
                            levels = state.hskLevelProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Overall Statistics Section
                val stats = state.stats
                if (stats != null) {
                    item {
                        OverallStatsCard(
                            mastered = stats.mastered,
                            reviewed = stats.reviewed,
                            learning = stats.learning,
                            unseen = stats.unseen,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        Text("No stats available", color = Color(0xFF64748B))
                    }
                }
                
                // Bottom padding is handled by contentPadding above
            }
        }
    }
}
