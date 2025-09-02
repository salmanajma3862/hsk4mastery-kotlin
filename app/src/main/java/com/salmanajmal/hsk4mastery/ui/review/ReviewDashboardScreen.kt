package com.salmanajmal.hsk4mastery.ui.review

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salmanajmal.hsk4mastery.ui.review.components.ReviewDeckCard

@Composable
fun ReviewDashboardScreen(
    viewModel: ReviewDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                    Spacer(Modifier.padding(top = 12.dp))
                    Text("Loading review data...", color = Color(0xFF6B7280))
                }
            } else {
                val data = uiState.dashboardData
                LazyColumn(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Review Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF1E293B))
                        )
                        Spacer(Modifier.padding(top = 16.dp))
                    }

                    // Struggling Words
                    if (!data?.strugglingWords.isNullOrEmpty()) {
                        item {
                            ReviewDeckCard(
                                title = "Struggling Words",
                                count = data!!.strugglingWords.size,
                                backgroundColor = Color(0xFFDC2626), // red tone
                                onClick = {
                                    Toast.makeText(ctx, "Start Struggling deck", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    // Due for Review (primary)
                    item {
                        val dueCount = data?.dueWords?.size ?: 0
                        ReviewDeckCard(
                            title = "Due for Review",
                            count = dueCount,
                            backgroundColor = if (dueCount > 0) Color(0xFF6366F1) else Color(0xFF94A3B8),
                            onClick = {
                                Toast.makeText(ctx, "Start Due deck", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Reviewed by Count section label
                    item {
                        Spacer(Modifier.padding(top = 8.dp))
                        Text(
                            text = "Reviewed by Count",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1E293B))
                        )
                        Spacer(Modifier.padding(top = 12.dp))
                    }

                    // Small cards grid-ish: render as rows of two
                    val groups = (data?.reviewedWordsByCount ?: emptyMap()).toList()
                        .sortedByDescending { it.first }
                    if (groups.isEmpty()) {
                        item {
                            Text(
                                text = "No reviewed words yet",
                                color = Color(0xFF64748B)
                            )
                        }
                    } else {
                        // chunk by 2 for rows
                        val chunks: List<List<Pair<Int, List<*>>>> = groups.chunked(2)
                        items(chunks) { rowItems ->
                            Row(modifier = Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { (count, words) ->
                                    val size = words.size
                                    val title = "${count}× reviewed"
                                    // Use lighter card color
                                    ReviewDeckCard(
                                        title = title,
                                        count = size,
                                        backgroundColor = Color(0xFFFFFFFF),
                                        contentColor = Color(0xFF1E293B),
                                        compact = true,
                                        onClick = {
                                            Toast.makeText(ctx, "Open $title", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    // spacer to balance row
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.padding(bottom = 24.dp)) }
                }
            }
        }
    }
}
