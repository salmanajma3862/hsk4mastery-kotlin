package com.salmanajmal.hsk4mastery.ui.listening

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ListeningScreen(viewModel: ListeningViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.screenState,
        transitionSpec = { fadeIn(tween(200)) with fadeOut(tween(200)) }, label = "listening_state"
    ) { state ->
        when (state) {
            ScreenState.SETUP -> SetupUI(ui = uiState, viewModel = viewModel)
            ScreenState.PLAYING -> PlayingUI(ui = uiState, viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupUI(ui: ListeningViewModel.ListeningUiState, viewModel: ListeningViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Listening Practice") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    LevelPickerAction(selected = ui.selectedLevel, onSelect = viewModel::onLevelSelected)
                }
            )
        }
    ) { padding ->
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF2FF))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Build a Playlist", style = MaterialTheme.typography.titleLarge)

                    OutlinedTextField(
                        value = ui.startId,
                        onValueChange = viewModel::onStartIdChanged,
                        label = { Text("Start Word ID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = ui.endId,
                        onValueChange = viewModel::onEndIdChanged,
                        label = { Text("End Word ID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { viewModel.onPresetSelected(PresetType.FIRST_50) }, label = { Text("First 50") })
                        AssistChip(onClick = { viewModel.onPresetSelected(PresetType.NEXT_50) }, label = { Text("Next 50") })
                        AssistChip(onClick = { viewModel.onPresetSelected(PresetType.RANDOM_20) }, label = { Text("Random 20") })
                    }

                    ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.startPlayback() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1), // match 'Due for Review' primary
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Play")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayingUI(ui: ListeningViewModel.ListeningUiState, viewModel: ListeningViewModel) {
    val current = ui.playlist.getOrNull(ui.currentTrackIndex)
    
    // Parse sentence data from fullData JSON
    val sentenceData = remember(current) {
        current?.let { word ->
            try {
                val json = org.json.JSONObject(word.fullData)
                val examplesArray = json.optJSONArray("examples")
                if (examplesArray != null && examplesArray.length() > 0) {
                    val firstExample = examplesArray.getJSONObject(0)
                    Triple(
                        firstExample.optString("hanzi", ""),
                        firstExample.optString("pinyin", ""),
                        firstExample.optString("translation", "")
                    )
                } else null
            } catch (_: Throwable) {
                null
            }
        }
    }
    
    val dynamicTitle = buildString {
        append("HSK ")
        append(ui.selectedLevel)
        append(": ")
        append("Words ")
        append(ui.startId)
        append("-")
        append(ui.endId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(dynamicTitle) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { viewModel.stopPlayback() }) {
                        Icon(Icons.Default.Close, contentDescription = "Stop")
                    }
                }
            )
        }
    ) { padding ->
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(Modifier.fillMaxWidth().weight(1f).padding(bottom = 20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Display content based on current segment
                        if (ui.currentSegment == PlaybackSegment.WORD) {
                            // Show word content
                            Text(current?.hanzi ?: "", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text(current?.pinyin ?: "", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(current?.meaning ?: "", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        } else {
                            // Show sentence content
                            sentenceData?.let { (hanzi, pinyin, translation) ->
                                Text(hanzi, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(8.dp))
                                Text(pinyin, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(8.dp))
                                Text(translation, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            } ?: run {
                                // Fallback if no sentence data
                                Text(current?.hanzi ?: "", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(8.dp))
                                Text(current?.pinyin ?: "", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                Text(current?.meaning ?: "", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    val progress = if (ui.playlist.isNotEmpty()) (ui.currentTrackIndex + 1f) / ui.playlist.size.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onPrevious() }) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Previous")
                    }
                    FilledIconButton(onClick = { viewModel.onPlayPauseTapped() }, modifier = Modifier.size(64.dp)) {
                        if (ui.isPlaying) Icon(Icons.Default.Pause, contentDescription = "Pause")
                        else Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                    IconButton(onClick = { viewModel.onNext() }) {
                        Icon(Icons.Default.FastForward, contentDescription = "Next")
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Speed segmented controls
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val speeds = listOf(0.5f, 0.75f, 1.0f)
                    speeds.forEach { s ->
                        val selected = ui.playbackSpeed == s
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onSpeedSelected(s) },
                            label = { Text("${s}x") }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun LevelPickerAction(selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "HSK $selected")
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Select Level")
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
