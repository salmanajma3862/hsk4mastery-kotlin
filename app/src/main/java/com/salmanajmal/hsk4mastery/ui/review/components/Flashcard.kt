package com.salmanajmal.hsk4mastery.ui.review.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salmanajmal.hsk4mastery.data.local.model.WordEntity
import com.salmanajmal.hsk4mastery.ui.review.CardMode

@Composable
fun Flashcard(
    cardData: WordEntity,
    mode: CardMode,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onPlayAudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "flipRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onFlip() }
            // Apply the main rotation animation to the entire card
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // We use a Box to fill the card and center the content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Fixed height to allow space for buttons below
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Conditionally display Front or Back based on the rotation angle
            if (rotation <= 90f) {
                // FRONT FACE
                // Content is displayed normally as the card is not yet flipped past 90 degrees.
                FrontFaceContent(cardData, mode, onPlayAudio)
            } else {
                // BACK FACE
                // The Card is rotated past 90 degrees, so its coordinate system is mirrored.
                // To fix the mirrored text, we apply a second, corrective rotation
                // of 180 degrees to the content itself. This "un-mirrors" it.
                BackFaceContent(
                    cardData = cardData,
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f
                    }
                )
            }
        }
    }
}

@Composable
private fun FrontFaceContent(
    cardData: WordEntity,
    mode: CardMode,
    onPlayAudio: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (mode) {
            CardMode.CHARACTER -> {
                Text(
                    text = cardData.hanzi,
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
            CardMode.MEANING -> {
                Text(
                    text = cardData.meaning,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }
            CardMode.LISTENING -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { onPlayAudio?.invoke() }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF6366F1))
                    }
                    Text("Tap to listen", color = Color(0xFF64748B))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "Tap to flip", color = Color(0xFF94A3B8))
    }
}

@Composable
private fun BackFaceContent(
    cardData: WordEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier, // Apply the corrective rotation modifier here
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = cardData.hanzi,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = cardData.pinyin,
            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = cardData.meaning,
            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF334155))
        )
    }
}