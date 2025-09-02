package com.salmanajmal.hsk4mastery.ui.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF8FAFC),
    icon: (@Composable (() -> Unit))? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    icon()
                }
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF64748B))
                )
            }
        }
    }
}
