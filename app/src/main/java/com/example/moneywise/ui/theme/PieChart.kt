package com.example.moneywise

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class PieChartSlice(
    val value: Float,
    val color: Color,
    val label: String = ""
)

@Composable
fun PieChart(
    slices: List<PieChartSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 60f,
    isDonut: Boolean = true
) {
    if (slices.isEmpty() || slices.all { it.value == 0f }) {
        // Empty chart placeholder
        Box(
            modifier = modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        return
    }

    val totalValue = slices.sumOf { it.value.toDouble() }

    Canvas(
        modifier = modifier.size(200.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - strokeWidth / 2

        var startAngle = -90f // Start from the top (12 o'clock position)

        slices.forEach { slice ->
            if (slice.value <= 0f) return@forEach

            val sweepAngle = (slice.value / totalValue.toFloat()) * 360f

            if (isDonut) {
                // Draw donut segments
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
            } else {
                // Draw pie segments
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // To position labels or indicators if needed
            val sliceAngleRadians = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val labelRadius = radius * 0.8
            val labelX = center.x + (labelRadius * cos(sliceAngleRadians)).toFloat()
            val labelY = center.y + (labelRadius * sin(sliceAngleRadians)).toFloat()

            // Here you could draw labels if needed

            startAngle += sweepAngle
        }
    }
}

@Composable
fun DonutChart(
    values: Map<String, Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = values.values.sum()

    if (total <= 0 || values.isEmpty()) {
        Box(
            modifier = modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        return
    }

    val slices = values.entries.mapIndexed { index, entry ->
        PieChartSlice(
            value = entry.value.toFloat(),
            color = colors.getOrElse(index) { Color.Gray },
            label = entry.key
        )
    }

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            slices = slices,
            modifier = Modifier.fillMaxSize(),
            isDonut = true
        )

        // Center hole of donut
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}