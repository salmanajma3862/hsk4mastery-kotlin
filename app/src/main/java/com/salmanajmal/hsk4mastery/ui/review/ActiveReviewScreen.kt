package com.salmanajmal.hsk4mastery.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.salmanajmal.hsk4mastery.ui.review.components.Flashcard
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveReviewScreen(
    navController: NavController,
    viewModel: ActiveReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
        ) {
            if (uiState.isSessionFinished) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎯 Session Complete!", style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF1E293B)))
                            Spacer(Modifier.padding(top = 8.dp))
                            Text("No more cards in this session", color = Color(0xFF64748B))
                            Spacer(Modifier.padding(top = 24.dp))
                            GradientButton(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                                onClick = { navController.popBackStack() },
                                text = "Back to Dashboard"
                            )
                        }
                    }
                }
                return@Scaffold
            }

            val index = uiState.currentCardIndex + 1
            val total = uiState.sessionWords.size
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index of $total",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1E293B))
                )
                val modeLabel = when (uiState.currentCardMode) {
                    CardMode.CHARACTER -> "character"
                    CardMode.MEANING -> "meaning"
                    CardMode.LISTENING -> "listening"
                }
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF6366F1))),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Text(text = modeLabel, color = Color.White)
                }
            }

            val current = uiState.sessionWords.getOrNull(uiState.currentCardIndex)
            if (current != null) {
                Flashcard(
                    cardData = current,
                    mode = uiState.currentCardMode,
                    isFlipped = uiState.isFlipped,
                    onFlip = { viewModel.onCardFlipped() },
                    onPlayAudio = { viewModel.playWordAudio() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.padding(top = 20.dp))
                if (uiState.isFlipped) {
                    Text(
                        text = "How did you do?",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1E293B))
                    )
                    Spacer(Modifier.padding(top = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GradientButton(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                            onClick = { viewModel.submitAnswer(isCorrect = false) },
                            text = "Wrong",
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            colors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            onClick = { viewModel.submitAnswer(isCorrect = true) },
                            text = "Correct",
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            onClick = { viewModel.submitAnswer(isCorrect = true, markAsMastered = true) },
                            text = "Mastered",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientButton(
    colors: List<Color>,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White)
        }
    }
}
