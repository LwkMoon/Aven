package com.aven.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Palette - Warm off-white, deep charcoal, muted forest greens, earthy stone
val LightBackground = Color(0xFFF6F5F0)
val LightSurface = Color(0xFFFCFCFA)
val LightSurfaceVariant = Color(0xFFEBE8E0)
val LightOutline = Color(0xFFD4D0C5)
val LightOutlineVariant = Color(0xFFE4E1D7)
val LightOnBackground = Color(0xFF1B1F1C)
val LightOnSurface = Color(0xFF1B1F1C)
val LightOnSurfaceVariant = Color(0xFF5F6660)

val LightPrimary = Color(0xFF32543B)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD8EBDC)
val LightOnPrimaryContainer = Color(0xFF0F2615)

val LightSecondary = Color(0xFF706B5E)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFEBE6D8)
val LightOnSecondaryContainer = Color(0xFF26231A)

val LightTertiary = Color(0xFF7D5F4D)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFF4E4DB)
val LightOnTertiaryContainer = Color(0xFF2F1D11)

// Dark Palette - Deep charcoal / green-black, warm light text, muted vegetation
val DarkBackground = Color(0xFF101512)
val DarkSurface = Color(0xFF161C18)
val DarkSurfaceVariant = Color(0xFF202722)
val DarkOutline = Color(0xFF38423B)
val DarkOutlineVariant = Color(0xFF28312B)
val DarkOnBackground = Color(0xFFE4E8E3)
val DarkOnSurface = Color(0xFFE4E8E3)
val DarkOnSurfaceVariant = Color(0xFF9BA39B)

val DarkPrimary = Color(0xFF91BA99)
val DarkOnPrimary = Color(0xFF0F2615)
val DarkPrimaryContainer = Color(0xFF223C29)
val DarkOnPrimaryContainer = Color(0xFFD3EAD7)

val DarkSecondary = Color(0xFFAAA598)
val DarkOnSecondary = Color(0xFF232018)
val DarkSecondaryContainer = Color(0xFF38352D)
val DarkOnSecondaryContainer = Color(0xFFE5DFD1)

val DarkTertiary = Color(0xFFB89885)
val DarkOnTertiary = Color(0xFF2E1C11)
val DarkTertiaryContainer = Color(0xFF453023)
val DarkOnTertiaryContainer = Color(0xFFF0DDD1)

@Immutable
data class AvenExtendedColors(
    val vegetationPrimary: Color,
    val vegetationSecondary: Color,
    val soilEarth: Color,
    val quietStone: Color,
    val streamBlue: Color,
    val intentionalTag: Color,
    val uncertainTag: Color
)

val LightExtendedColors = AvenExtendedColors(
    vegetationPrimary = Color(0xFF487352),
    vegetationSecondary = Color(0xFF7BA284),
    soilEarth = Color(0xFF6B5D4E),
    quietStone = Color(0xFF8C8677),
    streamBlue = Color(0xFF60879D),
    intentionalTag = Color(0xFF2F5438),
    uncertainTag = Color(0xFF7A6A58)
)

val DarkExtendedColors = AvenExtendedColors(
    vegetationPrimary = Color(0xFF74A37E),
    vegetationSecondary = Color(0xFF9BC2A4),
    soilEarth = Color(0xFF857564),
    quietStone = Color(0xFFA6A092),
    streamBlue = Color(0xFF7EADC7),
    intentionalTag = Color(0xFF88B391),
    uncertainTag = Color(0xFFBAA793)
)

val LocalAvenExtendedColors = staticCompositionLocalOf { LightExtendedColors }
