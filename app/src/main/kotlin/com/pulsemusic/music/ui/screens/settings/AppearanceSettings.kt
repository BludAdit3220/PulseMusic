/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsemusic.music.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pulsemusic.music.LocalPlayerAwareWindowInsets
import com.pulsemusic.music.R
import com.pulsemusic.music.constants.ChipSortTypeKey
import com.pulsemusic.music.constants.DarkModeKey
import com.pulsemusic.music.constants.DisableAnimationsKey
import com.pulsemusic.music.constants.EnableHapticFeedbackKey
import com.pulsemusic.music.constants.DefaultOpenTabKey
import com.pulsemusic.music.constants.DynamicThemeKey
import com.pulsemusic.music.constants.GridItemSize
import com.pulsemusic.music.constants.GridItemsSizeKey
import com.pulsemusic.music.constants.LibraryFilter
import com.pulsemusic.music.constants.PlayerDesignStyle
import com.pulsemusic.music.constants.PlayerDesignStyleKey
import com.pulsemusic.music.constants.PlayerBackgroundStyle
import com.pulsemusic.music.constants.PlayerBackgroundStyleKey
import com.pulsemusic.music.constants.PureBlackKey
import com.pulsemusic.music.constants.RandomThemeOnStartupKey
import com.pulsemusic.music.constants.UseSystemFontKey
import com.pulsemusic.music.constants.PlayerButtonsStyle
import com.pulsemusic.music.constants.PlayerButtonsStyleKey
import com.pulsemusic.music.constants.SliderStyle
import com.pulsemusic.music.constants.SliderStyleKey
import com.pulsemusic.music.constants.ShowLikedPlaylistKey
import com.pulsemusic.music.constants.ShowDownloadedPlaylistKey
import com.pulsemusic.music.constants.ShowHomeCategoryChipsKey
import com.pulsemusic.music.constants.ShowTopPlaylistKey
import com.pulsemusic.music.constants.ShowCachedPlaylistKey
import com.pulsemusic.music.constants.ShowTagsInLibraryKey
import com.pulsemusic.music.constants.QuickPicksDisplayMode
import com.pulsemusic.music.constants.QuickPicksDisplayModeKey
import com.pulsemusic.music.constants.SwipeThumbnailKey
import com.pulsemusic.music.constants.SwipeSensitivityKey
import com.pulsemusic.music.constants.SwipeToSongKey
import com.pulsemusic.music.constants.HidePlayerThumbnailKey
import com.pulsemusic.music.constants.PulseMusicCanvasKey
import com.pulsemusic.music.constants.ThumbnailCornerRadiusKey
import com.pulsemusic.music.constants.CropThumbnailToSquareKey
import com.pulsemusic.music.constants.DisableBlurKey
import com.pulsemusic.music.constants.BlurRadiusKey
import com.pulsemusic.music.ui.component.DefaultDialog
import com.pulsemusic.music.ui.component.EnumListPreference
import com.pulsemusic.music.ui.component.IconButton
import com.pulsemusic.music.ui.component.ListPreference
import com.pulsemusic.music.ui.component.PreferenceEntry
import com.pulsemusic.music.ui.component.PreferenceGroup
import com.pulsemusic.music.ui.component.SwitchPreference
import com.pulsemusic.music.ui.component.ThumbnailCornerRadiusSelectorButton
import com.pulsemusic.music.ui.player.StyledPlaybackSlider
import com.pulsemusic.music.ui.utils.backToMain
import com.pulsemusic.music.utils.isLowRamDevice
import com.pulsemusic.music.utils.rememberEnumPreference
import com.pulsemusic.music.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val defaultDisableAnimations = remember(context) { context.isLowRamDevice() }
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )
    val (randomThemeOnStartup, onRandomThemeOnStartupChange) = rememberPreference(
        RandomThemeOnStartupKey,
        defaultValue = false
    )
    val (darkMode, onDarkModeChange) = rememberEnumPreference(
        DarkModeKey,
        defaultValue = DarkMode.AUTO
    )
    val (playerDesignStyle, onPlayerDesignStyleChange) = rememberEnumPreference(
        PlayerDesignStyleKey,
        defaultValue = PlayerDesignStyle.V4
    )
    val (hidePlayerThumbnail, onHidePlayerThumbnailChange) = rememberPreference(
        HidePlayerThumbnailKey,
        defaultValue = false
    )
    val (archiveTuneCanvasEnabled, onPulseMusicCanvasEnabledChange) = rememberPreference(
        PulseMusicCanvasKey,
        defaultValue = false
    )
    val (thumbnailCornerRadius, onThumbnailCornerRadiusChange) = rememberPreference(
        key = ThumbnailCornerRadiusKey,
        defaultValue = 16f // default dp
    )
    val (cropThumbnailToSquare, onCropThumbnailToSquareChange) = rememberPreference(
        CropThumbnailToSquareKey,
        defaultValue = false
    )
    val (playerBackground, onPlayerBackgroundChange) =
        rememberEnumPreference(
            PlayerBackgroundStyleKey,
            defaultValue = PlayerBackgroundStyle.DEFAULT,
        )
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (disableBlur, onDisableBlurChange) = rememberPreference(DisableBlurKey, defaultValue = false)
    val (disableAnimations, onDisableAnimationsChange) = rememberPreference(
        DisableAnimationsKey,
        defaultValue = defaultDisableAnimations,
    )
    val (enableHapticFeedback, onEnableHapticFeedbackChange) = rememberPreference(
        EnableHapticFeedbackKey,
        defaultValue = true,
    )
    val (blurRadius, onBlurRadiusChange) = rememberPreference(BlurRadiusKey, defaultValue = 36f)
    val (useSystemFont, onUseSystemFontChange) = rememberPreference(UseSystemFontKey, defaultValue = false)
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(
        DefaultOpenTabKey,
        defaultValue = NavigationTab.HOME
    )
    val (playerButtonsStyle, onPlayerButtonsStyleChange) = rememberEnumPreference(
        PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.DEFAULT
    )
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(
        SliderStyleKey,
        defaultValue = SliderStyle.Standard
    )
    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(
        SwipeThumbnailKey,
        defaultValue = true
    )
    val (swipeSensitivity, onSwipeSensitivityChange) = rememberPreference(
        SwipeSensitivityKey,
        defaultValue = 0.73f
    )
    val (gridItemSize, onGridItemSizeChange) = rememberEnumPreference(
        GridItemsSizeKey,
        defaultValue = GridItemSize.SMALL
    )

    val (swipeToSong, onSwipeToSongChange) = rememberPreference(
        SwipeToSongKey,
        defaultValue = false
    )

    val (showLikedPlaylist, onShowLikedPlaylistChange) = rememberPreference(
        ShowLikedPlaylistKey,
        defaultValue = true
    )
    val (showDownloadedPlaylist, onShowDownloadedPlaylistChange) = rememberPreference(
        ShowDownloadedPlaylistKey,
        defaultValue = true
    )
    val (showTopPlaylist, onShowTopPlaylistChange) = rememberPreference(
        ShowTopPlaylistKey,
        defaultValue = true
    )
    val (showCachedPlaylist, onShowCachedPlaylistChange) = rememberPreference(
        ShowCachedPlaylistKey,
        defaultValue = true
    )
    val (showTagsInLibrary, onShowTagsInLibraryChange) = rememberPreference(
        ShowTagsInLibraryKey,
        defaultValue = true
    )
    val (showHomeCategoryChips, onShowHomeCategoryChipsChange) = rememberPreference(
        ShowHomeCategoryChipsKey,
        defaultValue = true
    )
    val (quickPicksDisplayMode, onQuickPicksDisplayModeChange) = rememberEnumPreference(
        QuickPicksDisplayModeKey,
        defaultValue = QuickPicksDisplayMode.CARD
    )

    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    val isPlayerBackgroundStyleEnabled =
        playerDesignStyle != PlayerDesignStyle.V8 && playerDesignStyle != PlayerDesignStyle.V9
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme =
        remember(darkMode, isSystemInDarkTheme) {
            if (darkMode == DarkMode.AUTO) isSystemInDarkTheme else darkMode == DarkMode.ON
        }

    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(
        key = ChipSortTypeKey,
        defaultValue = LibraryFilter.LIBRARY
    )

    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(isPlayerBackgroundStyleEnabled, playerBackground) {
        if (!isPlayerBackgroundStyleEnabled && playerBackground != PlayerBackgroundStyle.DEFAULT) {
            onPlayerBackgroundChange(PlayerBackgroundStyle.DEFAULT)
        }
    }

    if (showSliderOptionDialog) {
        val sliderStyles = remember {
            listOf(
                SliderStyle.Standard,
                SliderStyle.Wavy,
                SliderStyle.Thick,
                SliderStyle.Circular,
                SliderStyle.Simple
            )
        }
        DefaultDialog(
            buttons = {
                TextButton(
                    onClick = { showSliderOptionDialog = false },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismiss = {
                showSliderOptionDialog = false
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sliderStyles.chunked(3).forEach { styleRow ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        styleRow.forEach { style ->
                            SliderStyleOptionCard(
                                sliderStyle = style,
                                selected = sliderStyle == style,
                                onClick = {
                                    onSliderStyleChange(style)
                                    showSliderOptionDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - styleRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState()),
    ) {
        PreferenceGroup(title = stringResource(R.string.theme)) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.enable_dynamic_theme)) },
                    icon = { Icon(painterResource(R.drawable.palette), null) },
                    checked = dynamicTheme,
                    onCheckedChange = onDynamicThemeChange,
                )
            }

            item(visible = !dynamicTheme || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.random_theme_on_startup)) },
                    description = stringResource(R.string.random_theme_on_startup_desc),
                    icon = { Icon(painterResource(R.drawable.shuffle), null) },
                    checked = randomThemeOnStartup,
                    onCheckedChange = onRandomThemeOnStartupChange,
                )
            }

            item(visible = !dynamicTheme || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.color_palette)) },
                    description = stringResource(R.string.customize_theme_colors),
                    icon = { Icon(painterResource(R.drawable.format_paint), null) },
                    onClick = { navController.navigate("settings/appearance/palette_picker") }
                )
            }

            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.dark_theme)) },
                    icon = { Icon(painterResource(R.drawable.dark_mode), null) },
                    selectedValue = darkMode,
                    onValueSelected = onDarkModeChange,
                    valueText = {
                        when (it) {
                            DarkMode.ON -> stringResource(R.string.dark_theme_on)
                            DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                            DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                        }
                    },
                )
            }

            item(visible = useDarkTheme) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.pure_black)) },
                    icon = { Icon(painterResource(R.drawable.contrast), null) },
                    checked = pureBlack,
                    onCheckedChange = onPureBlackChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.disable_blur)) },
                    description = stringResource(R.string.disable_blur_desc),
                    icon = { Icon(painterResource(R.drawable.blur_off), null) },
                    checked = disableBlur,
                    onCheckedChange = onDisableBlurChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.disable_animations)) },
                    description = stringResource(R.string.disable_animations_desc),
                    icon = { Icon(painterResource(R.drawable.animation), null) },
                    checked = disableAnimations,
                    onCheckedChange = onDisableAnimationsChange,
                )
            }

            item {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.blur_intensity)) },
                    description = stringResource(R.string.blur_intensity_value, blurRadius.roundToInt()),
                    icon = { Icon(painterResource(R.drawable.blur_on), null) },
                    isEnabled = !disableBlur,
                    content = {
                        Spacer(modifier = Modifier.height(10.dp))
                        Slider(
                            value = blurRadius,
                            onValueChange = onBlurRadiusChange,
                            valueRange = 0f..48f,
                            steps = 47,
                            enabled = !disableBlur,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.use_system_font)) },
                    description = stringResource(R.string.use_system_font_desc),
                    icon = { Icon(painterResource(R.drawable.text_fields), null) },
                    checked = useSystemFont,
                    onCheckedChange = onUseSystemFontChange,
                )
            }
        }

        PreferenceGroup(title = stringResource(R.string.player)) {
            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.player_design_style)) },
                    icon = { Icon(painterResource(R.drawable.palette), null) },
                    selectedValue = playerDesignStyle,
                    onValueSelected = onPlayerDesignStyleChange,
                    valueText = {
                        when (it) {
                            PlayerDesignStyle.V1 -> stringResource(R.string.player_design_v1)
                            PlayerDesignStyle.V2 -> stringResource(R.string.player_design_v2)
                            PlayerDesignStyle.V3 -> stringResource(R.string.player_design_v3)
                            PlayerDesignStyle.V4 -> stringResource(R.string.player_design_v4)
                            PlayerDesignStyle.V5 -> stringResource(R.string.player_design_v5)
                            PlayerDesignStyle.V6 -> stringResource(R.string.player_design_v6)
                            PlayerDesignStyle.V7 -> stringResource(R.string.player_design_v7)
                            PlayerDesignStyle.V8 -> stringResource(R.string.player_design_v8)
                            PlayerDesignStyle.V9 -> stringResource(R.string.player_design_v9)
                        }
                    },
                )
            }

            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.player_background_style)) },
                    description = if (isPlayerBackgroundStyleEnabled) {
                        null
                    } else {
                        stringResource(R.string.player_background_style_v8_v9_desc)
                    },
                    icon = { Icon(painterResource(R.drawable.gradient), null) },
                    selectedValue = playerBackground,
                    onValueSelected = onPlayerBackgroundChange,
                    isEnabled = isPlayerBackgroundStyleEnabled,
                    valueText = {
                        when (it) {
                            PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                            PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                            PlayerBackgroundStyle.CUSTOM -> stringResource(R.string.custom)
                            PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                            PlayerBackgroundStyle.COLORING -> stringResource(R.string.coloring)
                            PlayerBackgroundStyle.BLUR_GRADIENT -> stringResource(R.string.blur_gradient)
                            PlayerBackgroundStyle.GLOW -> stringResource(R.string.glow)
                            PlayerBackgroundStyle.GLOW_ANIMATED -> "Glow Animated"
                        }
                    },
                )
            }

            item(visible = playerBackground == PlayerBackgroundStyle.CUSTOM) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.customized_background)) },
                    icon = { Icon(painterResource(R.drawable.image), null) },
                    onClick = { navController.navigate("customize_background") }
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.hide_player_thumbnail)) },
                    description = stringResource(R.string.hide_player_thumbnail_desc),
                    icon = { Icon(painterResource(R.drawable.hide_image), null) },
                    checked = hidePlayerThumbnail,
                    onCheckedChange = onHidePlayerThumbnailChange
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.pulsemusic_canvas)) },
                    description = stringResource(R.string.pulsemusic_canvas_desc),
                    icon = { Icon(painterResource(R.drawable.motion_photos_on), null) },
                    checked = archiveTuneCanvasEnabled,
                    onCheckedChange = onPulseMusicCanvasEnabledChange,
                )
            }

            item {
                ThumbnailCornerRadiusSelectorButton(
                    onRadiusSelected = {}
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.crop_thumbnail_to_square)) },
                    description = stringResource(R.string.crop_thumbnail_to_square_desc),
                    icon = { Icon(painterResource(R.drawable.image), null) },
                    checked = cropThumbnailToSquare,
                    onCheckedChange = onCropThumbnailToSquareChange
                )
            }

            item {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.aod_customize_title)) },
                    description = stringResource(R.string.aod_customize_entry_desc),
                    icon = { Icon(painterResource(R.drawable.bedtime), null) },
                    onClick = { navController.navigate("settings/appearance/aod_customized") }
                )
            }

            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.player_buttons_style)) },
                    icon = { Icon(painterResource(R.drawable.palette), null) },
                    selectedValue = playerButtonsStyle,
                    onValueSelected = onPlayerButtonsStyleChange,
                    valueText = {
                        when (it) {
                            PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                            PlayerButtonsStyle.SECONDARY -> stringResource(R.string.secondary_color_style)
                        }
                    },
                )
            }

            item {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.player_slider_style)) },
                    description = sliderStyleLabel(sliderStyle),
                    icon = { Icon(painterResource(R.drawable.sliders), null) },
                    onClick = {
                        showSliderOptionDialog = true
                    },
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.enable_swipe_thumbnail)) },
                    icon = { Icon(painterResource(R.drawable.swipe), null) },
                    checked = swipeThumbnail,
                    onCheckedChange = onSwipeThumbnailChange,
                )
            }

            item(visible = swipeThumbnail) {
                var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }

                if (showSensitivityDialog) {
                    var tempSensitivity by remember { mutableFloatStateOf(swipeSensitivity) }

                    DefaultDialog(
                        onDismiss = {
                            tempSensitivity = swipeSensitivity
                            showSensitivityDialog = false
                        },
                        buttons = {
                            TextButton(
                                onClick = {
                                    tempSensitivity = 0.73f
                                },
                                shapes = ButtonDefaults.shapes(),
                            ) {
                                Text(stringResource(R.string.reset))
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            TextButton(
                                onClick = {
                                    tempSensitivity = swipeSensitivity
                                    showSensitivityDialog = false
                                },
                                shapes = ButtonDefaults.shapes(),
                            ) {
                                Text(stringResource(android.R.string.cancel))
                            }
                            TextButton(
                                onClick = {
                                    onSwipeSensitivityChange(tempSensitivity)
                                    showSensitivityDialog = false
                                },
                                shapes = ButtonDefaults.shapes(),
                            ) {
                                Text(stringResource(android.R.string.ok))
                            }
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.swipe_sensitivity),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.sensitivity_percentage, (tempSensitivity * 100).roundToInt()),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Slider(
                                value = tempSensitivity,
                                onValueChange = { tempSensitivity = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                PreferenceEntry(
                    title = { Text(stringResource(R.string.swipe_sensitivity)) },
                    description = stringResource(R.string.sensitivity_percentage, (swipeSensitivity * 100).roundToInt()),
                    icon = { Icon(painterResource(R.drawable.tune), null) },
                    onClick = { showSensitivityDialog = true }
                )
            }
        }

        PreferenceGroup(title = stringResource(R.string.misc)) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.haptics)) },
                    description = stringResource(R.string.haptics_desc),
                    icon = { Icon(painterResource(R.drawable.vibration), null) },
                    checked = enableHapticFeedback,
                    onCheckedChange = onEnableHapticFeedbackChange,
                )
            }

            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.quick_picks_display_mode)) },
                    icon = { Icon(painterResource(R.drawable.grid_view), null) },
                    selectedValue = quickPicksDisplayMode,
                    onValueSelected = onQuickPicksDisplayModeChange,
                    valueText = {
                        when (it) {
                            QuickPicksDisplayMode.CARD -> stringResource(R.string.quick_picks_display_mode_card)
                            QuickPicksDisplayMode.LIST -> stringResource(R.string.quick_picks_display_mode_list)
                        }
                    },
                )
            }

            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.default_open_tab)) },
                    icon = { Icon(painterResource(R.drawable.nav_bar), null) },
                    selectedValue = defaultOpenTab,
                    onValueSelected = onDefaultOpenTabChange,
                    valueText = {
                        when (it) {
                            NavigationTab.HOME -> stringResource(R.string.home)
                            NavigationTab.SEARCH -> stringResource(R.string.search)
                            NavigationTab.MOODANDGENRES -> stringResource(R.string.mood_and_genres)
                            NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                        }
                    },
                )
            }

            item {
                ListPreference(
                    title = { Text(stringResource(R.string.default_lib_chips)) },
                    icon = { Icon(painterResource(R.drawable.tab), null) },
                    selectedValue = defaultChip,
                    values = listOf(
                        LibraryFilter.LIBRARY, LibraryFilter.PLAYLISTS, LibraryFilter.SONGS,
                        LibraryFilter.ALBUMS, LibraryFilter.ARTISTS
                    ),
                    valueText = {
                        when (it) {
                            LibraryFilter.SONGS -> stringResource(R.string.songs)
                            LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                            LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                            LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                            LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                        }
                    },
                    onValueSelected = onDefaultChipChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_home_category_chips)) },
                    description = stringResource(R.string.show_home_category_chips_desc),
                    icon = { Icon(painterResource(R.drawable.home_outlined), null) },
                    checked = showHomeCategoryChips,
                    onCheckedChange = onShowHomeCategoryChipsChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_tags_in_library)) },
                    description = stringResource(R.string.show_tags_in_library_desc),
                    icon = { Icon(painterResource(R.drawable.filter_alt), null) },
                    checked = showTagsInLibrary,
                    onCheckedChange = onShowTagsInLibraryChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.swipe_song_to_add)) },
                    icon = { Icon(painterResource(R.drawable.swipe), null) },
                    checked = swipeToSong,
                    onCheckedChange = onSwipeToSongChange
                )
            }

            item {
                EnumListPreference(
                    title = { Text(stringResource(R.string.grid_cell_size)) },
                    icon = { Icon(painterResource(R.drawable.grid_view), null) },
                    selectedValue = gridItemSize,
                    onValueSelected = onGridItemSizeChange,
                    valueText = {
                        when (it) {
                            GridItemSize.BIG -> stringResource(R.string.big)
                            GridItemSize.SMALL -> stringResource(R.string.small)
                        }
                    },
                )
            }
        }

        PreferenceGroup(title = stringResource(R.string.auto_playlists)) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_liked_playlist)) },
                    icon = { Icon(painterResource(R.drawable.favorite), null) },
                    checked = showLikedPlaylist,
                    onCheckedChange = onShowLikedPlaylistChange
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_downloaded_playlist)) },
                    icon = { Icon(painterResource(R.drawable.offline), null) },
                    checked = showDownloadedPlaylist,
                    onCheckedChange = onShowDownloadedPlaylistChange
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_top_playlist)) },
                    icon = { Icon(painterResource(R.drawable.trending_up), null) },
                    checked = showTopPlaylist,
                    onCheckedChange = onShowTopPlaylistChange
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_cached_playlist)) },
                    icon = { Icon(painterResource(R.drawable.cached), null) },
                    checked = showCachedPlaylist,
                    onCheckedChange = onShowCachedPlaylistChange
                )
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.appearance)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )
}

@Composable
private fun SliderStyleOptionCard(
    sliderStyle: SliderStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember {
        mutableFloatStateOf(0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        StyledPlaybackSlider(
            sliderStyle = sliderStyle,
            value = sliderValue,
            valueRange = 0f..1f,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {},
            activeColor = MaterialTheme.colorScheme.primary,
            isPlaying = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Text(
            text = sliderStyleLabel(sliderStyle),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun sliderStyleLabel(sliderStyle: SliderStyle): String {
    return when (sliderStyle) {
        SliderStyle.Standard -> stringResource(R.string.slider_style_standard)
        SliderStyle.Wavy -> stringResource(R.string.slider_style_wavy)
        SliderStyle.Thick -> stringResource(R.string.slider_style_thick)
        SliderStyle.Circular -> stringResource(R.string.slider_style_circular)
        SliderStyle.Simple -> stringResource(R.string.slider_style_simple)
    }
}

enum class DarkMode {
    ON,
    OFF,
    AUTO,
}

enum class NavigationTab {
    HOME,
    SEARCH,
    MOODANDGENRES,
    LIBRARY,
}

enum class PlayerTextAlignment {
    SIDED,
    CENTER,
}

enum class LyricsPosition {
    LEFT,
    CENTER,
    RIGHT,
}
