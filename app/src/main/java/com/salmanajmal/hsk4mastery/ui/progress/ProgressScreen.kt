@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)
package com.salmanajmal.hsk4mastery.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.fetchStats() }

    Scaffold() { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Simple stable header replacing experimental TopAppBar
            Text("My Progress", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(6.dp))
            if (state.isLoading) {
                CircularProgressIndicator()
                return@Column
            }

            val stats = state.stats
            if (stats != null) {
                val itemsList = listOf(
                    Triple("Mastered", stats.mastered, Color(0xFFE0F2FE)),
                    Triple("Reviewed", stats.reviewed, Color(0xFFF1F5F9)),
                    Triple("Learning", stats.learning, Color(0xFFEFF6FF)),
                    Triple("Unseen", stats.unseen, Color(0xFFFFF7ED)),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(itemsList) { (label, value, color) ->
                        StatCard(label = label, value = value, color = color)
                    }
                }
            } else {
                Text("No stats available", color = Color(0xFF64748B))
            }
            }
        }
    }
}
