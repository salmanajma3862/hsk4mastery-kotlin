package com.salmanajmal.hsk4mastery.ui.listening

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ListeningScreen(viewModel: ListeningViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        LevelPicker(
            selected = uiState.selectedLevel,
            onSelect = { viewModel.onLevelSelected(it) }
        )
        Spacer(Modifier.height(12.dp))
        RangeInputs(
            start = uiState.startId,
            end = uiState.endId,
            onStartChange = viewModel::onStartIdChanged,
            onEndChange = viewModel::onEndIdChanged,
            onBuild = { viewModel.buildPlaylist() }
        )
        Spacer(Modifier.height(12.dp))
        ModeButtons(selected = uiState.selectedMode, onSelect = viewModel::onModeSelected)
        Spacer(Modifier.height(24.dp))

        // Play / Pause control
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FilledIconButton(onClick = { viewModel.onPlayPauseTapped() }, modifier = Modifier.size(96.dp)) {
                if (uiState.isPlaying) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // Current word display
        if (uiState.playlist.isNotEmpty()) {
            val current = uiState.playlist.getOrNull(uiState.currentTrackIndex)
            Text(
                text = current?.hanzi ?: "",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(Modifier.height(12.dp))

        // Progress indicator
        if (uiState.playlist.isNotEmpty()) {
            val progress = (uiState.currentTrackIndex + 1).toFloat() / uiState.playlist.size.toFloat()
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        }

        uiState.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            Text(text = err, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(18.dp))
        Text("Playlist (${uiState.playlist.size})", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(uiState.playlist.size) { idx ->
                val w = uiState.playlist[idx]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (idx == uiState.currentTrackIndex) Color(0xFFE0F2FE) else Color.Transparent)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(w.wordId?.toString() ?: "-", modifier = Modifier.width(60.dp))
                    Text(w.hanzi, modifier = Modifier.width(80.dp), fontWeight = FontWeight.SemiBold)
                    Text(w.meaning, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LevelPicker(selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("HSK Level $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (1..4).forEach { level ->
                DropdownMenuItem(text = { Text("HSK $level") }, onClick = {
                    onSelect(level)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun RangeInputs(start: String, end: String, onStartChange: (String) -> Unit, onEndChange: (String) -> Unit, onBuild: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = start,
            onValueChange = onStartChange,
            label = { Text("Start ID") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = end,
            onValueChange = onEndChange,
            label = { Text("End ID") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onBuild, modifier = Modifier.align(Alignment.CenterVertically)) { Text("Load") }
    }
}

@Composable
private fun ModeButtons(selected: ListeningMode, onSelect: (ListeningMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ListeningMode.values().forEach { mode ->
            val active = mode == selected
            FilterChip(
                selected = active,
                onClick = { onSelect(mode) },
                label = { Text(mode.name) }
            )
        }
    }
}
