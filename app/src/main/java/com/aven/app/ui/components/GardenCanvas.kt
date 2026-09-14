package com.aven.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aven.app.core.garden.GardenEngine
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.ui.theme.AvenExtendedColors
import com.aven.app.ui.theme.LocalAvenExtendedColors
import kotlin.math.sin

/**
 * GardenCanvas renders the persistent living world.
 *
 * It is the visual encoding of the user's aggregate behavioral pattern over time.
 * Calm, atmospheric, and respects reduced motion settings.
 */
@Composable
fun GardenCanvas(
    gardenState: GardenStateEntity,
    modifier: Modifier = Modifier,
    isReducedMotion: Boolean = false,
    onElementTapped: ((GardenEngine.EnvironmentalElement) -> Unit)? = null
) {
    val stage = GardenEngine.GardenStage.fromName(gardenState.stage)
    val extendedColors = LocalAvenExtendedColors.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    // Ambient motion: calm wind sway
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_wind")
    val swayPhase by if (isReducedMotion) {
        remember { mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sway"
        )
    }

    val streamFlow by if (isReducedMotion) {
        remember { mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "stream"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .testTag("garden_canvas_container")
            .semantics {
                contentDescription = "Visual representation of your garden world, currently in the ${stage.displayName} stage with ${gardenState.environmentState} environment."
            }
            .pointerInput(gardenState.stage) {
                detectTapGestures {
                    // Match to current prominent element
                    val matchingElement = when (stage) {
                        GardenEngine.GardenStage.SEED -> GardenEngine.allElements.first { it.id == "seed_pod" }
                        GardenEngine.GardenStage.SPROUT -> GardenEngine.allElements.first { it.id == "tender_sprout" }
                        GardenEngine.GardenStage.GROVE -> GardenEngine.allElements.first { it.id == "elder_pine" }
                        GardenEngine.GardenStage.HABITAT -> GardenEngine.allElements.first { it.id == "whispering_brook" }
                        GardenEngine.GardenStage.LIVING_WORLD -> GardenEngine.allElements.first { it.id == "ancient_redwood" }
                    }
                    onElementTapped?.invoke(matchingElement)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Atmospheric Sky Gradient
            val skyTop = if (isDark) Color(0xFF0F1713) else Color(0xFFEAE7DC)
            val skyBottom = if (isDark) Color(0xFF19241E) else Color(0xFFF7F5EE)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(skyTop, skyBottom),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Distant Mountains (Stages 3, 4, 5)
            if (stage >= GardenEngine.GardenStage.GROVE) {
                val mountainTint = if (isDark) Color(0xFF1E2C23) else Color(0xFFD3D8CF)
                val mtnPath = Path().apply {
                    moveTo(0f, h * 0.65f)
                    lineTo(w * 0.25f, h * 0.38f)
                    lineTo(w * 0.50f, h * 0.58f)
                    lineTo(w * 0.78f, h * 0.32f)
                    lineTo(w, h * 0.62f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(mtnPath, color = mountainTint.copy(alpha = 0.55f))
            }

            // 3. Middle rolling hill
            val hillTint = if (isDark) Color(0xFF1A261E) else Color(0xFFDCE2D8)
            val hillPath = Path().apply {
                moveTo(0f, h * 0.70f)
                cubicTo(
                    w * 0.35f, h * 0.60f,
                    w * 0.65f, h * 0.74f,
                    w, h * 0.64f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(hillPath, color = hillTint)

            // 4. Foreground Fertile Loam / Soil Base
            val earthBase = if (isDark) Color(0xFF171E19) else Color(0xFFD5CEBE)
            val groundPath = Path().apply {
                moveTo(0f, h * 0.76f)
                cubicTo(
                    w * 0.30f, h * 0.72f,
                    w * 0.70f, h * 0.79f,
                    w, h * 0.75f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(groundPath, color = earthBase)

            // 5. Stream (Stages 4 and 5)
            if (stage >= GardenEngine.GardenStage.HABITAT) {
                val streamPath = Path().apply {
                    moveTo(w * 0.40f, h * 0.73f)
                    cubicTo(
                        w * 0.46f + (swayPhase * 3f), h * 0.80f,
                        w * 0.36f - (swayPhase * 3f), h * 0.88f,
                        w * 0.42f, h
                    )
                }
                drawPath(
                    path = streamPath,
                    color = extendedColors.streamBlue.copy(alpha = 0.7f),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
            }

            // 6. Primary Flora by Stage
            when (stage) {
                GardenEngine.GardenStage.SEED -> {
                    drawSeedStage(w, h, extendedColors.soilEarth, extendedColors.vegetationPrimary)
                }
                GardenEngine.GardenStage.SPROUT -> {
                    drawSproutStage(w, h, swayPhase, extendedColors.vegetationPrimary, extendedColors.vegetationSecondary)
                }
                GardenEngine.GardenStage.GROVE -> {
                    drawGroveStage(w, h, swayPhase, extendedColors.vegetationPrimary, extendedColors.vegetationSecondary, extendedColors.soilEarth)
                }
                GardenEngine.GardenStage.HABITAT -> {
                    drawHabitatStage(w, h, swayPhase, extendedColors)
                }
                GardenEngine.GardenStage.LIVING_WORLD -> {
                    drawLivingWorldStage(w, h, swayPhase, extendedColors)
                }
            }

            // 7. Ambient flora details (gentle stones / grass tufts)
            val grassColor = extendedColors.vegetationSecondary.copy(alpha = 0.75f)
            drawCircle(color = grassColor, radius = 2.5f, center = Offset(w * 0.22f, h * 0.78f))
            drawCircle(color = grassColor, radius = 2.0f, center = Offset(w * 0.76f, h * 0.81f))
            drawCircle(color = extendedColors.quietStone, radius = 3.5f, center = Offset(w * 0.82f, h * 0.84f))
        }
    }
}

private fun DrawScope.drawSeedStage(w: Float, h: Float, soilColor: Color, plantColor: Color) {
    val cx = w * 0.5f
    val groundY = h * 0.74f

    // Dormant seed nestled in soil
    val seedPath = Path().apply {
        moveTo(cx, groundY - 14f)
        cubicTo(cx + 8f, groundY - 8f, cx + 8f, groundY + 4f, cx, groundY + 8f)
        cubicTo(cx - 8f, groundY + 4f, cx - 8f, groundY - 8f, cx, groundY - 14f)
        close()
    }
    drawPath(seedPath, color = soilColor)

    // Tiny emerging root shoot
    drawLine(
        color = plantColor,
        start = Offset(cx, groundY - 14f),
        end = Offset(cx, groundY - 22f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSproutStage(w: Float, h: Float, sway: Float, primaryGreen: Color, secondaryGreen: Color) {
    val cx = w * 0.5f
    val groundY = h * 0.75f
    val tipX = cx + (sway * 5f)
    val tipY = groundY - 48f

    // Stem
    val stemPath = Path().apply {
        moveTo(cx, groundY)
        cubicTo(cx - 2f, groundY - 20f, cx + (sway * 2f), groundY - 35f, tipX, tipY)
    }
    drawPath(stemPath, color = primaryGreen, style = Stroke(width = 4f, cap = StrokeCap.Round))

    // Left Leaf
    val leftLeaf = Path().apply {
        moveTo(cx - 2f, groundY - 28f)
        cubicTo(cx - 22f, groundY - 36f, cx - 24f, groundY - 20f, cx - 2f, groundY - 24f)
        close()
    }
    drawPath(leftLeaf, color = secondaryGreen)

    // Right Leaf
    val rightLeaf = Path().apply {
        moveTo(tipX, tipY + 8f)
        cubicTo(tipX + 24f, tipY - 4f, tipX + 22f, tipY + 14f, tipX, tipY + 12f)
        close()
    }
    drawPath(rightLeaf, color = primaryGreen)
}

private fun DrawScope.drawGroveStage(
    w: Float, h: Float, sway: Float,
    primaryGreen: Color, secondaryGreen: Color, barkColor: Color
) {
    val groundY = h * 0.75f

    // Left Sapling
    val lx = w * 0.32f
    drawLine(
        color = barkColor,
        start = Offset(lx, groundY),
        end = Offset(lx + (sway * 3f), groundY - 55f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = secondaryGreen,
        radius = 24f,
        center = Offset(lx + (sway * 3f), groundY - 68f)
    )

    // Right Sapling
    val rx = w * 0.68f
    drawLine(
        color = barkColor,
        start = Offset(rx, groundY),
        end = Offset(rx + (sway * 2f), groundY - 48f),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = primaryGreen.copy(alpha = 0.85f),
        radius = 20f,
        center = Offset(rx + (sway * 2f), groundY - 58f)
    )

    // Central Mature Elder Tree
    val cx = w * 0.50f
    drawLine(
        color = barkColor,
        start = Offset(cx, groundY),
        end = Offset(cx + (sway * 4f), groundY - 80f),
        strokeWidth = 7f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = primaryGreen,
        radius = 36f,
        center = Offset(cx + (sway * 4f), groundY - 100f)
    )
    drawCircle(
        color = secondaryGreen,
        radius = 26f,
        center = Offset(cx - 16f + (sway * 3f), groundY - 92f)
    )
}

private fun DrawScope.drawHabitatStage(
    w: Float, h: Float, sway: Float,
    extended: AvenExtendedColors
) {
    val groundY = h * 0.75f

    // Grove Trees background
    drawGroveStage(w, h, sway * 0.7f, extended.vegetationPrimary, extended.vegetationSecondary, extended.soilEarth)

    // Standing Stone Cairn on the right bank
    val cairnX = w * 0.78f
    drawRoundRect(
        color = extended.quietStone,
        topLeft = Offset(cairnX - 12f, groundY - 10f),
        size = Size(24f, 8f),
        cornerRadius = CornerRadius(4f)
    )
    drawRoundRect(
        color = extended.quietStone.copy(alpha = 0.9f),
        topLeft = Offset(cairnX - 9f, groundY - 18f),
        size = Size(18f, 7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f)
    )
    drawRoundRect(
        color = extended.quietStone.copy(alpha = 0.8f),
        topLeft = Offset(cairnX - 6f, groundY - 25f),
        size = Size(12f, 6f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
    )
}

private fun DrawScope.drawLivingWorldStage(
    w: Float, h: Float, sway: Float,
    extended: AvenExtendedColors
) {
    val groundY = h * 0.75f

    // Expansive canopy & layers
    drawHabitatStage(w, h, sway * 0.8f, extended)

    // Majestic Ancient Redwood on the left
    val cx = w * 0.24f
    drawLine(
        color = extended.soilEarth,
        start = Offset(cx, groundY),
        end = Offset(cx + (sway * 3f), groundY - 110f),
        strokeWidth = 9f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = extended.vegetationPrimary,
        radius = 42f,
        center = Offset(cx + (sway * 3f), groundY - 130f)
    )
    drawCircle(
        color = extended.vegetationSecondary,
        radius = 30f,
        center = Offset(cx + 18f + (sway * 2f), groundY - 120f)
    )

    // Ambient birds gliding quietly
    val birdColor = extended.quietStone
    val b1x = w * 0.65f + (sway * 8f)
    val b1y = h * 0.28f
    drawLine(color = birdColor, start = Offset(b1x - 6f, b1y + 3f), end = Offset(b1x, b1y), strokeWidth = 1.8f, cap = StrokeCap.Round)
    drawLine(color = birdColor, start = Offset(b1x, b1y), end = Offset(b1x + 6f, b1y + 3f), strokeWidth = 1.8f, cap = StrokeCap.Round)
}
