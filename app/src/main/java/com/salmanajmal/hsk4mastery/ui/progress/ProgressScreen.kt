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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salmanajmal.hsk4mastery.ui.progress.components.StatCard
import com.salmanajmal.hsk4mastery.ui.progress.components.HskLevelProgressBar

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.fetchStats() }

    Scaffold() { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header matching ReviewDashboardScreen style
                    Text(
                        text = "My Progress",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1E293B))
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 16.dp))
                }
                
                if (state.isLoading) {
                    item {
                        CircularProgressIndicator()
                    }
                    return@LazyColumn
                }

                // HSK Level Progress Section (removed duplicate title)
                if (state.hskLevelProgress.isNotEmpty()) {
                    items(state.hskLevelProgress) { levelProgress ->
                        HskLevelProgressBar(
                            levelProgress = levelProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Overall Statistics Section
                val stats = state.stats
                if (stats != null) {
                    
                    item {
                        val itemsList = listOf(
                            Triple("Mastered", stats.mastered, Color(0xFFE0F2FE)),
                            Triple("Reviewed", stats.reviewed, Color(0xFFF1F5F9)),
                            Triple("Learning", stats.learning, Color(0xFFEFF6FF)),
                            Triple("Unseen", stats.unseen, Color(0xFFFFF7ED)),
                        )
                        
                        // Use regular Column instead of nested LazyVerticalGrid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // First row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(
                                    label = itemsList[0].first,
                                    value = itemsList[0].second,
                                    color = itemsList[0].third,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = itemsList[1].first,
                                    value = itemsList[1].second,
                                    color = itemsList[1].third,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Second row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(
                                    label = itemsList[2].first,
                                    value = itemsList[2].second,
                                    color = itemsList[2].third,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = itemsList[3].first,
                                    value = itemsList[3].second,
                                    color = itemsList[3].third,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text("No stats available", color = Color(0xFF64748B))
                    }
                }
                
                // Bottom padding to prevent content being hidden by bottom tabs
                item {
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 24.dp))
                }
            }
        }
    }
}
