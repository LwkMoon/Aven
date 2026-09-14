package com.aven.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aven.app.ui.theme.LocalAvenExtendedColors

/**
 * Aven Logo Mark:
 * Abstract geometric/organic mark per Section 13.5:
 * - Minimal and abstract: Not a literal leaf or plant cartoon.
 * - Containment arc representing the Pause, sheltering an ascending bud representing Awareness & Growth.
 * - Usable at any scale without losing clarity.
 */
@Composable
fun AvenLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val extended = LocalAvenExtendedColors.current
    val secondaryTint = extended.vegetationSecondary
    val baseLineTint = extended.quietStone

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Aven Logo Mark" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer calm containment arc (The Pause)
            val arcStroke = (w * 0.08f).coerceAtLeast(2f)
            val arcPath = Path().apply {
                moveTo(w * 0.22f, h * 0.72f)
                cubicTo(
                    w * 0.22f, h * 0.30f,
                    w * 0.78f, h * 0.30f,
                    w * 0.78f, h * 0.72f
                )
            }
            drawPath(
                path = arcPath,
                color = tint,
                style = Stroke(width = arcStroke, cap = StrokeCap.Round)
            )

            // Base horizon line
            drawLine(
                color = baseLineTint,
                start = Offset(w * 0.16f, h * 0.80f),
                end = Offset(w * 0.84f, h * 0.80f),
                strokeWidth = arcStroke * 0.75f,
                cap = StrokeCap.Round
            )

            // Ascending Seed / Bud Core
            val budPath = Path().apply {
                moveTo(w * 0.50f, h * 0.44f)
                cubicTo(
                    w * 0.64f, h * 0.58f,
                    w * 0.64f, h * 0.72f,
                    w * 0.50f, h * 0.72f
                )
                cubicTo(
                    w * 0.36f, h * 0.72f,
                    w * 0.36f, h * 0.58f,
                    w * 0.50f, h * 0.44f
                )
                close()
            }
            drawPath(
                path = budPath,
                color = secondaryTint
            )
        }
    }
}

/**
 * Wordmark: The mark paired with "AVEN" set with restrained letter-spacing.
 */
@Composable
fun AvenWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 32.dp,
    vertical: Boolean = false,
    letterSpacing: TextUnit = 5.sp
) {
    if (vertical) {
        Column(
            modifier = modifier.semantics { contentDescription = "Aven Brand Wordmark" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AvenLogoMark(size = markSize)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AVEN",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = letterSpacing,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    } else {
        Row(
            modifier = modifier.semantics { contentDescription = "Aven Brand Wordmark" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvenLogoMark(size = markSize)
            Text(
                text = "AVEN",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = letterSpacing,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}
