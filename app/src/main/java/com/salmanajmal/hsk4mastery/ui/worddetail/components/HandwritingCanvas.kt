package com.salmanajmal.hsk4mastery.ui.worddetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

@Composable
fun HandwritingCanvas(
    character: String,
    width: Int = 350,
    height: Int = 350,
) {
    var showGuide by remember { mutableStateOf(true) }
    var paths by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val strokeColor = Color(0xFF0F172A)
    val guideColor = Color(0x201E293B)

    Box(
        modifier = Modifier
            .size(width.dp, height.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.TopEnd
    ) {
    val characterSnapshot = character
    Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    coroutineScope {
                        awaitPointerEventScope {
                            while (isActive) {
                                val event = awaitPointerEvent()
                                val changes = event.changes
                                val first = changes.firstOrNull() ?: continue
                                if (first.pressed) {
                                    val pos = first.position
                                    currentPath = currentPath + pos
                                    first.consume()
                                } else {
                                    if (currentPath.isNotEmpty()) {
                                        paths = paths + listOf(currentPath)
                                        currentPath = emptyList()
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // Guide grid
            val w = size.width
            val h = size.height
            drawLine(Color(0xFFE2E8F0), start = Offset(w/2, 0f), end = Offset(w/2, h))
            drawLine(Color(0xFFE2E8F0), start = Offset(0f, h/2), end = Offset(w, h/2))

            if (showGuide && characterSnapshot.isNotEmpty()) {
                // draw simple guide character using native canvas drawText is not available directly; skip text measurer in draw scope
                // Instead draw a light grid only; guide character rendering omitted to avoid composable calls here
            }

            fun drawStroke(points: List<Offset>) {
                if (points.size < 2) return
                val p = Path()
                p.moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pt = points[i]
                    p.lineTo(pt.x, pt.y)
                }
                drawPath(p, color = strokeColor, alpha = 0.9f, style = Stroke(width = 8f))
            }

            paths.forEach { drawStroke(it) }
            drawStroke(currentPath)
        }

        IconButton(onClick = { paths = emptyList(); currentPath = emptyList() }, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF334155))
        }
        IconButton(onClick = { showGuide = !showGuide }, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(if (showGuide) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle", tint = Color(0xFF334155))
        }
    }
}
