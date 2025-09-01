package com.salmanajmal.hsk4mastery.ui.worddetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SentenceTokenModel(val word: String?, val pinyin: String?, val meaning: String?)

@Composable
fun SentenceToken(token: SentenceTokenModel, onClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Text(
            text = token.pinyin ?: "",
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = token.word ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1E293B), fontWeight = FontWeight.Medium)
        )
    }
}
