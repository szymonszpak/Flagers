package com.example.flagers.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            val sweepAngle = flagColor.percentage * 360f
            val parseColor = try {
                Color(flagColor.hex.toColorInt())
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
    }
}