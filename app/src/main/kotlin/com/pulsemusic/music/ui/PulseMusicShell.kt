/*
 * PulseMusic (2026)
 * © PulseMusic contributors — GPL-3.0
 * Derived from PulseMusic (© Aditya Parasher).
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.pulsemusic.music.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * LocalPlayerAwareWindowInsets — provides bottom inset that accounts for
 * the mini player height so content scrolls above the player.
 *
 * Port of PulseMusic's pattern, adapted for PulseMusic.
 */
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> {
    error("LocalPlayerAwareWindowInsets not provided")
}

/** Height of the mini player pill */
val MiniPlayerHeight = 64.dp
/** Gap between mini player and floating toolbar */
val MiniPlayerToolbarGap = 8.dp
/** Height of the floating toolbar pill */
val FloatingToolbarHeight = 56.dp

/**
 * PulseMusicShell — the root composable that owns:
 * - Adaptive navigation (FloatingNavigationToolbar on phones, NavigationRail on tablets)
 * - The pill MiniPlayer floating above the bottom toolbar
 * - The main NavHost content area
 * - Proper insets composition for the whole screen
 *
 * Layout stack (bottom to top):
 * ```
 * [SystemBars]
 * [NavContent fills remaining space, padded by playerAwareInsets]
 * [MiniPlayer — floating 8dp above toolbar]
 * [FloatingNavigationToolbar — above navigation bar system inset]
 * ```
 */
@Composable
fun PulseMusicShell(
    navController: NavHostController = rememberNavController(),
    pureBlack: Boolean = false,
    content: @Composable (NavHostController, PaddingValues) -> Unit,
) {
    val windowInfo = currentWindowAdaptiveInfo()
    val isExpandedWidth = windowInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    // Scrolling behavior for the floating toolbar — hides on scroll-down, shows on scroll-up
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.Bottom)

    // Total bottom space consumed by mini player + toolbar + nav bar
    val navBarInsetDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomChrome: Dp = navBarInsetDp + FloatingToolbarHeight + MiniPlayerToolbarGap + MiniPlayerHeight + 12.dp

    val playerAwareInsets = remember(bottomChrome) {
        WindowInsets(
            bottom = bottomChrome.value.toInt()
        )
    }

    CompositionLocalProvider(LocalPlayerAwareWindowInsets provides playerAwareInsets) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isExpandedWidth) {
                // ── Tablet / Landscape layout ─────────────────────────────
                // NavigationRail on the left, content on the right
                PulseMusicRailLayout(
                    navController = navController,
                    pureBlack = pureBlack,
                    content = content,
                )
            } else {
                // ── Phone / Portrait layout ───────────────────────────────
                // Floating toolbar at bottom center, mini player above it
                PulseMusicPhoneLayout(
                    navController = navController,
                    pureBlack = pureBlack,
                    scrollBehavior = scrollBehavior,
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun PulseMusicPhoneLayout(
    navController: NavHostController,
    pureBlack: Boolean,
    scrollBehavior: androidx.compose.material3.FloatingToolbarScrollBehavior,
    content: @Composable (NavHostController, PaddingValues) -> Unit,
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val toolbarBottomPadding = navBarPadding + 8.dp
    val miniPlayerBottomPadding = toolbarBottomPadding + FloatingToolbarHeight + MiniPlayerToolbarGap

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Main content area ─────────────────────────────────────────────
        content(
            navController,
            PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = miniPlayerBottomPadding + MiniPlayerHeight + 12.dp,
            )
        )

        // ── MiniPlayer — sits just above the floating toolbar ─────────────
        MiniPlayerSlot(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = miniPlayerBottomPadding)
                .padding(horizontal = 16.dp),
            navController = navController,
        )

        // ── FloatingNavigationToolbar ─────────────────────────────────────
        FloatingNavToolbarSlot(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = toolbarBottomPadding),
            navController = navController,
            pureBlack = pureBlack,
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
private fun PulseMusicRailLayout(
    navController: NavHostController,
    pureBlack: Boolean,
    content: @Composable (NavHostController, PaddingValues) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical))
    ) {
        // NavigationRail
        NavigationRailSlot(
            navController = navController,
            pureBlack = pureBlack,
        )

        // Main content takes remaining width
        Box(modifier = Modifier.weight(1f)) {
            content(
                navController,
                PaddingValues(0.dp),
            )
        }
    }
}

/**
 * Placeholder slot composables — these will be replaced with the real wired-up
 * implementations once MiniPlayer.kt, FloatingNavigationToolbar.kt, and
 * AppNavigation.kt (NavigationRail) are fully adapted.
 *
 * Keeping as concrete empty composables so the project compiles immediately
 * and can be swapped in incrementally.
 */
@Composable
internal fun MiniPlayerSlot(modifier: Modifier, navController: NavHostController) {
    // TODO: Replace with PulseMusicMiniPlayer connected to PlayerViewModel
    Box(modifier = modifier)
}

@Composable
internal fun FloatingNavToolbarSlot(
    modifier: Modifier,
    navController: NavHostController,
    pureBlack: Boolean,
    scrollBehavior: androidx.compose.material3.FloatingToolbarScrollBehavior,
) {
    // TODO: Replace with PulseMusicFloatingNavToolbar (adapted from FloatingNavigationToolbar.kt)
    Box(modifier = modifier)
}

@Composable
internal fun NavigationRailSlot(navController: NavHostController, pureBlack: Boolean) {
    // TODO: Replace with PulseMusicNavigationRail (adapted from AppNavigation.kt)
}
