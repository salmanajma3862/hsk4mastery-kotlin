package com.salmanajmal.hsk4mastery.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceToken
import com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun PracticeScreen(viewModel: PracticeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()

    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "HSK ${state.selectedLevel} Practice") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level picker action identical to WordListScreen
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Select HSK level")
                        }
                        DropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("HSK 1") }, onClick = {
                                viewModel.onLevelSelected(1); isMenuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("HSK 2") }, onClick = {
                                viewModel.onLevelSelected(2); isMenuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("HSK 3") }, onClick = {
                                viewModel.onLevelSelected(3); isMenuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("HSK 4") }, onClick = {
                                viewModel.onLevelSelected(4); isMenuExpanded = false
                            })
                        }
                    }
                }
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(top = 24.dp).padding(padding)) {
            // Header
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                val hanzi = state.practiceWord?.hanzi
                if (!hanzi.isNullOrBlank()) {
                    Text(text = hanzi, color = Color(0xFF475569))
                }
            }

            // Mode selector
            TabRow(selectedTabIndex = if (state.practiceMode == PracticeMode.TRANSLATION) 0 else 1, containerColor = Color(0xFFE5E7EB)) {
                Tab(
                    selected = state.practiceMode == PracticeMode.TRANSLATION,
                    onClick = { viewModel.onModeSelected(PracticeMode.TRANSLATION) },
                    text = { Text("Translation Challenge", color = if (state.practiceMode == PracticeMode.TRANSLATION) Color.White else Color(0xFF111827)) },
                    selectedContentColor = Color(0xFF111827),
                    unselectedContentColor = Color(0xFF111827)
                )
                Tab(
                    selected = state.practiceMode == PracticeMode.SCRAMBLE,
                    onClick = { viewModel.onModeSelected(PracticeMode.SCRAMBLE) },
                    text = { Text("Pure Scramble", color = if (state.practiceMode == PracticeMode.SCRAMBLE) Color.White else Color(0xFF111827)) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color(0xFF111827)
                )
            }

            // Clue area
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = "Clue", color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                val clue = if (state.practiceMode == PracticeMode.TRANSLATION) {
                    // Show the first example translation if available, fallback to active example
                    state.activeExampleTranslation ?: ""
                } else {
                    "Re-order the words for: ${state.practiceWord?.hanzi.orEmpty()}"
                }
                if (clue.isNotBlank()) Text(text = clue, color = Color(0xFF1F2937))
            }

            // Answer area
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Your Answer", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF334155), fontWeight = FontWeight.SemiBold))
                if (state.userAttempt.isEmpty()) {
                    Text("Tap words to build the sentence", color = Color(0xFF94a3b8))
                } else {
                    FlowRow(modifier = Modifier.fillMaxWidth()) {
                        state.userAttempt.forEachIndexed { idx, token ->
                            ChipToken(token) { viewModel.onAnswerTap(idx) }
                        }
                    }
                }
            }

            // Word bank
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Word Bank", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF334155), fontWeight = FontWeight.SemiBold))
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    state.scrambledTokens.forEachIndexed { idx, token ->
                        ChipToken(token) { viewModel.onWordBankTap(idx) }
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.checkAnswer() },
                    enabled = state.originalTokens.isNotEmpty() && state.userAttempt.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) { Text("Check Answer", color = Color.White, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { viewModel.onReset() },
                    enabled = state.originalTokens.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                ) { Text("Reset", color = Color.White, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { viewModel.fetchNewPuzzle() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) { Text("Next Puzzle", color = Color.White, fontWeight = FontWeight.Bold) }
            }

            // Feedback
            if (state.isCorrect != null) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.isCorrect == true) {
                        Text("Correct!", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        // Optional: could display pinyin/translation if we tracked it here
                    } else {
                        Text("Try Again", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        }
    }
}

@Composable
private fun ChipToken(token: SentenceTokenModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .background(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = token.word.orEmpty(), color = Color(0xFF111827), style = MaterialTheme.typography.bodyLarge)
    }
}

