/*
 * PulseMusic (2026)
 * © PulseMusic contributors — GPL-3.0
 * Derived from PulseMusic (© Aditya Parasher).
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsemusic.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pulsemusic.music.ui.theme.PulseAmber
import com.pulsemusic.music.ui.theme.PulseSurface
import com.pulsemusic.music.ui.theme.PulseSurfaceContainer
import com.pulsemusic.music.ui.theme.PulseMusicTheme

/**
 * HeroCarouselItem — data model for each card in the hero carousel.
 */
data class HeroCarouselItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String,
    val accentColor: Color? = null,
)

/**
 * HeroCarouselSection — a [HorizontalMultiBrowseCarousel] that displays large
 * hero cards at the top of the Home Screen.
 *
 * Each card shows:
 * - Full-bleed album art / thumbnail (AsyncImage via Coil3)
 * - A vertical gradient scrim from transparent (top) to [PulseSurface] (bottom)
 * - Title and optional subtitle overlaid at the bottom
 * - Animated amber accent border on the focused/active card
 *
 * Designed to replace the static "Quick Picks" section from PulseMusic with
 * the dynamic multi-browse carousel pattern from PulseMusic's HomeScreen.
 */
@Composable
fun HeroCarouselSection(
    items: List<HeroCarouselItem>,
    onItemClick: (HeroCarouselItem) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 220.dp,
    preferredItemWidth: Dp = 186.dp,
    minSmallItemWidth: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    if (items.isEmpty()) return

    val carouselState = rememberCarouselState { items.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = preferredItemWidth,
        modifier = modifier,
        minSmallItemWidth = minSmallItemWidth,
        itemSpacing = 8.dp,
        contentPadding = contentPadding,
    ) { index ->
        val item = items[index]
        // Track the current snapped item as "focused" using CarouselState.currentItem
        val isFocused = index == carouselState.currentItem

        val borderColor by animateColorAsState(
            targetValue = if (isFocused)
                item.accentColor ?: PulseAmber
            else
                Color.Transparent,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "hero_border_$index",
        )

        HeroCarouselCard(
            item = item,
            isFocused = isFocused,
            borderColor = borderColor,
            itemHeight = itemHeight,
            onClick = {
                onItemClick(item)
            },
        )
    }
}

@Composable
private fun HeroCarouselCard(
    item: HeroCarouselItem,
    isFocused: Boolean,
    borderColor: Color,
    itemHeight: Dp,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .height(itemHeight)
            .fillMaxWidth()
            .maskClip(MaterialTheme.shapes.extraLarge),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PulseSurfaceContainer,
        ),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 2.dp,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Album Art ─────────────────────────────────────────────────
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // ── Bottom gradient scrim ──────────────────────────────────────
            GradientScrim(
                modifier = Modifier.fillMaxSize(),
                startFraction = 0.35f,
            )

            // ── Text overlay ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!item.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Vertical gradient scrim from transparent at the top to [PulseSurface] at the bottom.
 */
@Composable
private fun BoxScope.GradientScrim(
    modifier: Modifier = Modifier,
    startFraction: Float = 0.3f,
    scrimColor: Color = PulseSurface.copy(alpha = 0.85f),
) {
    Box(
        modifier = modifier
            .drawWithContent {
                drawContent()
            }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        startFraction to Color.Transparent,
                        1f to scrimColor,
                    ),
                )
            )
    )
}

/** Extension to mask clipping (Carousel requires clip to shape) */
private fun Modifier.maskClip(shape: androidx.compose.ui.graphics.Shape): Modifier =
    this.clip(shape)

@Preview
@Composable
fun HeroCarouselSectionPreview() {
    PulseMusicTheme {
        HeroCarouselSection(
            items = listOf(
                HeroCarouselItem(
                    id = "1",
                    title = "After Hours",
                    subtitle = "The Weeknd",
                    thumbnailUrl = "https://example.com/art.jpg",
                    accentColor = Color(0xFFE91E63)
                ),
                HeroCarouselItem(
                    id = "2",
                    title = "Vultures 1",
                    subtitle = "¥\$, Kanye West, Ty Dolla \$ign",
                    thumbnailUrl = "https://example.com/art2.jpg"
                ),
                HeroCarouselItem(
                    id = "3",
                    title = "Utopia",
                    subtitle = "Travis Scott",
                    thumbnailUrl = "https://example.com/art3.jpg",
                    accentColor = Color(0xFF795548)
                )
            ),
            onItemClick = {}
        )
    }
}
