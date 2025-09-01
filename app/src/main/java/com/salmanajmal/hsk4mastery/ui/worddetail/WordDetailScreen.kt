package com.salmanajmal.hsk4mastery.ui.worddetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salmanajmal.hsk4mastery.ui.worddetail.components.HandwritingCanvas
import com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    viewModel: WordDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = state.word?.hanzi ?: "Word Detail") })
    }) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val word = state.word
        if (word == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Word not found")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item("header") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(word.hanzi, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(word.pinyin, style = MaterialTheme.typography.titleMedium)
                    Text(word.meaning, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item("audio-speed") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Audio speed", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.5f, 0.75f, 1.0f).forEach { spd ->
                            FilterChip(
                                selected = state.audioSpeed == spd,
                                onClick = { viewModel.onAudioSpeedChange(spd) },
                                label = { Text("${spd}x") }
                            )
                        }
                        Button(onClick = { viewModel.playWordAudio() }) { Text("Play word") }
                    }
                }
            }

            if (state.parsed.examples.isNotEmpty()) {
                item("examples-title") { Text("Examples", style = MaterialTheme.typography.titleMedium) }
            }
            items(state.parsed.examples.withIndex().toList()) { pair ->
                val (idx, ex) = pair
                Card { 
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ex.tokens.forEach { token ->
                                SentenceToken(token = com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel(
                                    word = token.word,
                                    pinyin = token.pinyin,
                                    meaning = token.meaning
                                ))
                            }
                        }
                        Text(ex.translation ?: "", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { viewModel.playSentenceAudio(idx + 1) }) { Text("Play sentence") }
                    }
                }
            }

            item("comfort") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How comfortable are you?", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { level ->
                            val label = when(level){1->"Hard";2->"Okay";else->"Easy"}
                            Button(
                                onClick = { viewModel.onComfortLevelSelected(level) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when(level){1->MaterialTheme.colorScheme.error;2->MaterialTheme.colorScheme.tertiary;else->MaterialTheme.colorScheme.primary}
                                )
                            ) { Text(label) }
                        }
                    }
                }
            }

            item("handwriting") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Practice writing", style = MaterialTheme.typography.titleMedium)
                    HandwritingCanvas(character = word.hanzi)
                }
            }
        }
    }
}
