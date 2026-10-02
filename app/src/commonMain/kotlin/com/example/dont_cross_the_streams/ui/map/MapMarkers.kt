package com.example.dont_cross_the_streams.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Wildlife crossing marker: a green rounded square with a bridge arch, deliberately a different
 * shape from the round collision markers so "helped across" and "killed here" read apart at a glance.
 */
fun DrawScope.drawWildlifeCrossingMarker(center: Offset, selected: Boolean = false) {
    val half = 11.dp.toPx()
    val stroke = 2.dp.toPx()
    if (selected) {
        drawCircle(
            color = WildlifeCrossingColor.copy(alpha = 0.35f),
            radius = half * 2.2f,
            center = center
        )
    }
    drawRoundRect(
        color = WildlifeCrossingColor,
        topLeft = Offset(center.x - half, center.y - half),
        size = Size(half * 2, half * 2),
        cornerRadius = CornerRadius(4.dp.toPx())
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(center.x - half, center.y - half),
        size = Size(half * 2, half * 2),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = stroke)
    )
    // Bridge glyph: a deck with an arch underneath.
    val deckY = center.y - half * 0.35f
    drawLine(
        color = Color.White,
        start = Offset(center.x - half * 0.65f, deckY),
        end = Offset(center.x + half * 0.65f, deckY),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    val arch = Path().apply {
        moveTo(center.x - half * 0.55f, center.y + half * 0.55f)
        quadraticTo(center.x, deckY - half * 0.2f, center.x + half * 0.55f, center.y + half * 0.55f)
    }
    drawPath(arch, color = Color.White, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/** Individual collision/carcass report: a small red dot, so hundreds of them stay readable. */
fun DrawScope.drawCollisionReportMarker(center: Offset, selected: Boolean = false) {
    val radius = (if (selected) 7.dp else 5.dp).toPx()
    drawCircle(color = Color(0x99000000), radius = radius + 1.5.dp.toPx(), center = center)
    drawCircle(color = CollisionReportColor, radius = radius, center = center)
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

/** Compact key explaining the two marker styles that are easiest to confuse. */
@Composable
fun CollisionCrossingLegend(
    showCollisionReports: Boolean,
    showCrossings: Boolean,
    modifier: Modifier = Modifier
) {
    if (!showCollisionReports && !showCrossings) return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showCollisionReports) {
                LegendRow(label = "Reported collision / carcass") { drawCollisionReportMarker(center) }
            }
            if (showCrossings) {
                LegendRow(label = "Wildlife crossing") { drawWildlifeCrossingMarker(center) }
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, drawMarker: DrawScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(24.dp), onDraw = drawMarker)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
