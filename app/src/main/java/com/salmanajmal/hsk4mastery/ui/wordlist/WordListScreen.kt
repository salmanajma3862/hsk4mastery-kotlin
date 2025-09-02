package com.salmanajmal.hsk4mastery.ui.wordlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WordListScreen(
    viewModel: WordListViewModel = hiltViewModel(),
    onWordClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        // Background gradient approximation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Loading words...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                    )
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Text(
                            text = "HSK4 Vocabulary",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFF1E293B)
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${uiState.words.size} words to master",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val levels = listOf(1, 2, 3, 4)
                            levels.forEach { level ->
                                val selected = uiState.selectedLevel == level
                                if (selected) {
                                    Button(onClick = { /* no-op */ }) { Text("HSK $level") }
                                } else {
                                    OutlinedButton(onClick = { viewModel.onLevelSelected(level) }) { Text("HSK $level") }
                                }
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        items(uiState.words, key = { it.id }) { w ->
                            WordListItem(
                                word = w,
                                onClick = { onWordClick(w.id) },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}
