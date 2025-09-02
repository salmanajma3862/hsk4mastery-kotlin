package com.salmanajmal.hsk4mastery.ui.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salmanajmal.hsk4mastery.ui.worddetail.components.HandwritingCanvas
import com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceToken
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
    viewModel: WordDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.word?.hanzi ?: "Word Detail") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                        Text(
                            "Loading word details...",
                            color = Color(0xFF6B7280),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
                return@Scaffold
            }

            val word = state.word
            if (word == null) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Word not found",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
                return@Scaffold
            }

            // Selected token dialog state
            var dialogToken: com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel? by remember { mutableStateOf(null) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                item("hero") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                spotColor = Color(0xFF6366F1),
                                ambientColor = Color(0xFF6366F1)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFF0F9FF),
                                            Color(0xFFE0F2FE)
                                        )
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Always-visible audio button like Expo
                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                Color(0xFF1E40AF),
                                                RoundedCornerShape(16.dp)
                                            )
                        .clickable { viewModel.playAudio(state.parsed.mainAudioUrl) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.VolumeUp,
                                            contentDescription = "Play audio",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        word.hanzi,
                                        fontSize = 56.sp,
                                        color = Color(0xFF1E40AF),
                                        fontWeight = FontWeight.W900,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 2.sp
                                    )
                                }
                                Text(
                                    word.pinyin,
                                    fontSize = 22.sp,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.W600,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    word.meaning,
                                    fontSize = 20.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.W500,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Character Breakdown
                if (state.parsed.characterBreakdown.isNotEmpty()) {
                    item("character-breakdown") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Character Breakdown",
                                    color = Color(0xFF1E293B),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.W700,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.parsed.characterBreakdown.forEach { char ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                char.hanzi ?: "",
                                                color = Color(0xFF1E293B),
                                                fontWeight = FontWeight.W800,
                                                fontSize = 20.sp,
                                                modifier = Modifier.width(40.dp)
                                            )
                                            Text(
                                                char.pinyin ?: "",
                                                color = Color(0xFF6366F1),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.W600,
                                                modifier = Modifier.width(80.dp)
                                            )
                                            Text(
                                                char.meaning ?: "",
                                                color = Color(0xFF475569),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.W500,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Example Sentences
                if (state.parsed.examples.isNotEmpty()) {
                    item("examples-header") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Example Sentence",
                                        color = Color(0xFF1E293B),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.W700
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(0.5f, 0.75f, 1.0f).forEach { speed ->
                                            val isSelected = state.audioSpeed == speed
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) Color(0xFF6366F1) else Color(0xFFF1F5F9),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { viewModel.onAudioSpeedChange(speed) }
                                                    .padding(vertical = 6.dp, horizontal = 12.dp)
                                            ) {
                                                Text(
                                                    "${speed}x",
                                                    color = if (isSelected) Color.White else Color(0xFF64748B),
                                                    fontWeight = FontWeight.W600,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    state.parsed.examples.forEachIndexed { idx, ex ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(
                                                            Color(0xFFFFFFFF),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable { viewModel.playSentenceAudio(idx + 1) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "🔊",
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                if (ex.translation != null) {
                                                    Text(
                                                        ex.translation,
                                                        color = Color(0xFF1E40AF),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.W500,
                                                        modifier = Modifier.weight(1f),
                                                        lineHeight = 22.sp
                                                    )
                                                }
                                            }
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                ex.tokens.forEach { token ->
                                                    SentenceToken(
                                                        token = com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel(
                                                            word = token.word,
                                                            pinyin = token.pinyin,
                                                            meaning = token.meaning
                                                        ),
                                                        onClick = {
                                                            dialogToken = com.salmanajmal.hsk4mastery.ui.worddetail.components.SentenceTokenModel(
                                                                word = token.word,
                                                                pinyin = token.pinyin,
                                                                meaning = token.meaning
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        if (idx < state.parsed.examples.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFFF1F5F9))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Writing Practice
                item("writing-practice") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "✍️ Practice Writing",
                            color = Color(0xFF1E293B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W700,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFF8FAFC),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HandwritingCanvas(character = word.hanzi)
                        }
                    }
                }

                // Comfort Level
                item("comfort") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "How well do you know this word?",
                                color = Color(0xFF1E293B),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W700,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val comfortOptions = listOf(
                                    Triple("Unfamiliar", 1, Color(0xFFEF4444)),
                                    Triple("Getting There", 2, Color(0xFFF59E0B)),
                                    Triple("Confident", 3, Color(0xFF10B981))
                                )
                                comfortOptions.forEach { (label, level, color) ->
                                    val isSelected = state.comfortLevel == level
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) color else Color(0xFFF8FAFC),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.onComfortLevelSelected(level) }
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = if (isSelected) Color.White else Color(0xFF1E293B),
                                            fontWeight = FontWeight.W600,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Token detail dialog
            val token = dialogToken
            if (token != null) {
                AlertDialog(
                    onDismissRequest = { dialogToken = null },
                    confirmButton = {
                        TextButton(onClick = { dialogToken = null }) { Text("Dismiss") }
                    },
                    title = { Text(text = token.word ?: "") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!token.pinyin.isNullOrBlank()) Text(token.pinyin!!, color = Color(0xFF6366F1))
                            if (!token.meaning.isNullOrBlank()) Text(token.meaning!!, color = Color(0xFF475569))
                        }
                    }
                )
            }
        }
    }
}
