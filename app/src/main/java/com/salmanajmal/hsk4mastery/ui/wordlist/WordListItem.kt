package com.salmanajmal.hsk4mastery.ui.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salmanajmal.hsk4mastery.data.local.model.WordBasic

@Composable
fun WordListItem(
    word: WordBasic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color(0xFFFFFFFF),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Reserve space to avoid overlap with the right-side pill
            Row(
                Modifier
                    .padding(vertical = 16.dp, horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(end = 56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left
                Column(Modifier.weight(2f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val color = statusColor(word.status, word.comfortLevel)
                        // show the small status dot for non-confident levels
                        if (word.comfortLevel != 3) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(color)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = word.hanzi,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = word.pinyin,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                // Right
                Column(Modifier.weight(3f)) {
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                    )
                    if (word.wordId != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "#${word.wordId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Confident pill shown at center-right when comfortLevel == 3
            if (word.comfortLevel == 3) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Confident",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun statusColor(status: String?, comfortLevel: Int?): Color {
    // Comfort level takes precedence
    when (comfortLevel) {
        1 -> return Color(0xFFEF4444) // red
        2 -> return Color(0xFFF59E0B) // yellow
        3 -> return Color(0xFF22C55E) // green
    }
    when (status?.lowercase()) {
        "mastered" -> return Color(0xFF22C55E)
        "learning" -> return Color(0xFFF59E0B)
        "reviewed", "review" -> return Color(0xFF3B82F6)
    }
    return Color(0xFF6B7280) // gray default
}
