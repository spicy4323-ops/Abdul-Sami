package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderGlow: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val borderBrush = if (borderGlow) {
        Brush.horizontalGradient(
            colors = listOf(
                ElectricCyan.copy(alpha = 0.6f),
                CyberViolet.copy(alpha = 0.5f),
                ElectricCyan.copy(alpha = 0.2f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GlassBorder,
                GlassBorder.copy(alpha = 0.4f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassSurface.copy(alpha = 0.85f),
                        Color(0xFF0C1322).copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}
