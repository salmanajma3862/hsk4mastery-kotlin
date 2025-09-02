package com.salmanajmal.hsk4mastery.ui.worddetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Undo
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

    val strokeColor = Color(0xFF34D399)
    val currentStrokeColor = Color(0xFFA7F3D0)
    val guideGrid = Color(0xFF374151)
    val canvasBg = Color(0xFF0B0F15)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
            .background(canvasBg, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.TopEnd
    ) {
        val characterSnapshot = character
        Canvas(
            modifier = Modifier
                .fillMaxSize()
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
            drawLine(guideGrid.copy(alpha = 0.35f), start = Offset(w/2, 0f), end = Offset(w/2, h))
            drawLine(guideGrid.copy(alpha = 0.35f), start = Offset(0f, h/2), end = Offset(w, h/2))

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
                drawPath(p, color = strokeColor, alpha = 0.9f, style = Stroke(width = 6f))
            }

            paths.forEach { drawStroke(it) }
            if (currentPath.isNotEmpty()) {
                val p = Path().apply {
                    moveTo(currentPath.first().x, currentPath.first().y)
                    currentPath.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(p, color = currentStrokeColor, alpha = 1f, style = Stroke(width = 9f))
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { showGuide = !showGuide }) {
                Icon(
                    if (showGuide) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle guide",
                    tint = Color.White
                )
            }
            IconButton(onClick = {
                if (currentPath.isNotEmpty()) currentPath = emptyList() else if (paths.isNotEmpty()) paths = paths.dropLast(1)
            }) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
            }
            IconButton(onClick = { paths = emptyList(); currentPath = emptyList() }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
            }
        }
    }
}
