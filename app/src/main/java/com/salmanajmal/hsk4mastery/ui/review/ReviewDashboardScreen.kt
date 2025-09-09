package com.salmanajmal.hsk4mastery.ui.review

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavController
import com.salmanajmal.hsk4mastery.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDashboardScreen(
    viewModel: ReviewDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Review") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
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
                LazyColumn(contentPadding = PaddingValues(16.dp)) {

                    // Struggling Words
                    if (!data?.strugglingWords.isNullOrEmpty()) {
                        item {
                            ReviewDeckCard(
                                title = "Struggling Words",
                                count = data!!.strugglingWords.size,
                                backgroundColor = Color(0xFFDC2626), // fallback
                                gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                                subtitle = "High priority — tap to focus",
                                onClick = {
                                    // Placeholder retained; wrapper with NavController will handle actual navigation
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
                            gradientColors = if (dueCount > 0) listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)) else listOf(Color(0xFF94A3B8), Color(0xFF64748B)),
                            disabled = dueCount == 0,
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
                                        subtitle = "$size words",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDashboardScreenForNav(
    navController: NavController,
    viewModel: ReviewDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    // Delegate to the existing UI but with navigation-enabled onClicks
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Review") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
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
                LazyColumn(contentPadding = PaddingValues(16.dp)) {

                    if (!data?.strugglingWords.isNullOrEmpty()) {
                        item {
                            val ids = data!!.strugglingWords.joinToString(",") { it.id }
                            ReviewDeckCard(
                                title = "Struggling Words",
                                count = data.strugglingWords.size,
                                backgroundColor = Color(0xFFDC2626),
                                gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                                subtitle = "High priority — tap to focus",
                                onClick = { navController.navigate(Routes.ActiveReview.replace("{wordIds}", ids)) },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    item {
                        val dueCount = data?.dueWords?.size ?: 0
                        val ids = data?.dueWords?.joinToString(",") { it.id } ?: ""
                        ReviewDeckCard(
                            title = "Due for Review",
                            count = dueCount,
                            backgroundColor = if (dueCount > 0) Color(0xFF6366F1) else Color(0xFF94A3B8),
                            gradientColors = if (dueCount > 0) listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)) else listOf(Color(0xFF94A3B8), Color(0xFF64748B)),
                            disabled = dueCount == 0,
                            onClick = { if (dueCount > 0) navController.navigate(Routes.ActiveReview.replace("{wordIds}", ids)) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        Spacer(Modifier.padding(top = 8.dp))
                        Text(
                            text = "Reviewed by Count",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1E293B))
                        )
                        Spacer(Modifier.padding(top = 12.dp))
                    }

                    val groups = (data?.reviewedWordsByCount ?: emptyMap()).toList().sortedByDescending { it.first }
                    if (groups.isEmpty()) {
                        item { Text(text = "No reviewed words yet", color = Color(0xFF64748B)) }
                    } else {
                        val chunks = groups.chunked(2)
                        items(chunks) { rowItems ->
                            Row(modifier = Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { (count, words) ->
                                    val title = "${count}× reviewed"
                                    val ids = words.joinToString(",") { it.id }
                                    ReviewDeckCard(
                                        title = title,
                                        count = words.size,
                                        backgroundColor = Color(0xFFFFFFFF),
                                        contentColor = Color(0xFF1E293B),
                                        compact = true,
                                        subtitle = "${words.size} words",
                                        onClick = { navController.navigate(Routes.ActiveReview.replace("{wordIds}", ids)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item { Spacer(Modifier.padding(bottom = 24.dp)) }
                }
            }
            }
        }
    }
}
