package com.salmanajmal.hsk4mastery.ui.wordlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    viewModel: WordListViewModel = hiltViewModel(),
    onWordClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Dynamic title reflecting selected level
                    Text(text = "HSK${uiState.selectedLevel} Vocabulary")
                },
                actions = {
                    // Dropdown anchored to the top-right action icon
                    Box {
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Select HSK level")
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("HSK 1") },
                                onClick = {
                                    viewModel.onLevelSelected(1)
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("HSK 2") },
                                onClick = {
                                    viewModel.onLevelSelected(2)
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("HSK 3") },
                                onClick = {
                                    viewModel.onLevelSelected(3)
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("HSK 4") },
                                onClick = {
                                    viewModel.onLevelSelected(4)
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
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
