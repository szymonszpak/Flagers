package com.example.flagers.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.flagers.data.FlagColor
import androidx.core.graphics.toColorInt

@Composable
fun PieChartCanvas(
    colors: List<FlagColor>,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp
) {
    Canvas(modifier = modifier.size(size)) {
        var startAngle = -90f

        colors.forEach { flagColor ->
            val normalizedPercentage = if (flagColor.percentage > 1f) flagColor.percentage / 100f else flagColor.percentage
            val sweepAngle = normalizedPercentage * 360f
            val parseColor = try {
                val hexString = if (flagColor.hex.startsWith("#")) flagColor.hex else "#${flagColor.hex}"
                Color(hexString.toColorInt())
            } catch (e: Exception) {
                Color.Gray
            }

            drawArc(
                color = parseColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.Black,
            style = Stroke(width = 4f)
        )
    }
}