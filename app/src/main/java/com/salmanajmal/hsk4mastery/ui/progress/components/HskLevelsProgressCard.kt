package com.salmanajmal.hsk4mastery.ui.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salmanajmal.hsk4mastery.data.repository.HskLevelProgress
import kotlin.math.roundToInt

@Composable
fun HskLevelsProgressCard(
    levels: List<HskLevelProgress>,
    modifier: Modifier = Modifier
) {
    if (levels.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "HSK Progress",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Bold
            )

            val sortedLevels = levels.sortedBy { it.level }
            sortedLevels.forEachIndexed { index, levelProgress ->
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HSK ${levelProgress.level}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${levelProgress.percentage.roundToInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(levelProgress.percentage / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF6366F1))
                    )
                }

                // Count text
                Text(
                    text = "${levelProgress.confidentCount} / ${levelProgress.totalCount} words confident",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )

                if (index != sortedLevels.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                    Divider(color = Color(0xFFE5E7EB))
                }
            }
        }
    }
}
