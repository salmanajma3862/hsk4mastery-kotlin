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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
    val canvasBg = Color(0xFF0B0F15)
    val controlsBg = Color(0xFF1F2937)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
                .background(canvasBg, RoundedCornerShape(12.dp))
        ) {
            // Centered translucent guide character (below strokes)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                if (showGuide && character.isNotEmpty()) {
                    val density = LocalDensity.current
                    // Fit font to box height and total width based on character count
                    val minSideDp: Dp = minOf(maxWidth, maxHeight)
                    val count = character.length.coerceAtLeast(1)
                    val heightLimitDp: Dp = minSideDp * 0.6f
                    val widthLimitDp: Dp = (maxWidth / count.toFloat()) * 0.9f
                    val fontDp: Dp = minOf(heightLimitDp, widthLimitDp)
                    val fontSizeSp = with(density) { fontDp.toSp() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        character.forEach { ch ->
                            if (ch == ' ') {
                                Spacer(modifier = Modifier.width(fontDp * 0.5f))
                            } else {
                                Text(
                                    text = ch.toString(),
                                    color = Color(0xFFE0E0E0).copy(alpha = 0.2f),
                                    fontSize = fontSizeSp
                                )
                            }
                        }
                    }
                }
            }

            // Drawing canvas and touch handling (on top of guide)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        coroutineScope {
                            awaitPointerEventScope {
                                while (isActive) {
                                    val event = awaitPointerEvent()
                                    val first = event.changes.firstOrNull() ?: continue
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
                val savedStrokeWidth = 6.dp.toPx()
                val currentStrokeWidth = 9.dp.toPx()

                fun drawStroke(points: List<Offset>) {
                    if (points.size < 2) return
                    val p = Path()
                    p.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val pt = points[i]
                        p.lineTo(pt.x, pt.y)
                    }
                    drawPath(p, color = strokeColor, alpha = 0.9f, style = Stroke(width = savedStrokeWidth))
                }

                paths.forEach { drawStroke(it) }
                if (currentPath.isNotEmpty()) {
                    val p = Path().apply {
                        moveTo(currentPath.first().x, currentPath.first().y)
                        currentPath.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(p, color = currentStrokeColor, alpha = 1f, style = Stroke(width = currentStrokeWidth))
                }
            }
        }

        // Controls row below the canvas, aligned to end
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { showGuide = !showGuide },
                colors = IconButtonDefaults.iconButtonColors(containerColor = controlsBg)
            ) {
                Icon(
                    imageVector = if (showGuide) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle guide",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (currentPath.isNotEmpty()) currentPath = emptyList() else if (paths.isNotEmpty()) paths = paths.dropLast(1)
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = controlsBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { paths = emptyList(); currentPath = emptyList() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = controlsBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
