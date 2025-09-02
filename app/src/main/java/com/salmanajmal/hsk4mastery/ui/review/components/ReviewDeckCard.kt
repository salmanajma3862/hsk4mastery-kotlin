package com.salmanajmal.hsk4mastery.ui.review.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReviewDeckCard(
    title: String,
    count: Int,
    backgroundColor: Color,
    onClick: () -> Unit,
    contentColor: Color = Color.White,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = contentColor, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.padding(top = if (compact) 4.dp else 8.dp))
            Text(
                text = "$count",
                style = (if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall)
                    .copy(color = contentColor, fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.padding(top = if (compact) 2.dp else 4.dp))
            Text(
                text = if (count > 0) "Tap to start" else "No items",
                style = MaterialTheme.typography.bodyMedium.copy(color = contentColor.copy(alpha = 0.9f))
            )
        }
    }
}
