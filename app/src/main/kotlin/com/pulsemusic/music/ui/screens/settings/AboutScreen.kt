/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsemusic.music.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.pulsemusic.music.BuildConfig
import com.pulsemusic.music.R
import com.pulsemusic.music.ui.component.IconButton
import com.pulsemusic.music.ui.utils.backToMain
import com.pulsemusic.music.LocalPlayerAwareWindowInsets
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.pulsemusic.music.currentBuildHash
import com.pulsemusic.music.ui.utils.resetHeightOffset

data class TeamMember(
    val avatarUrl: String,
    val name: String,
    val position: String,
    val profileUrl: String? = null,
    val github: String? = null,
    val website: String? = null,
    val discord: String? = null
)

@Composable
fun OutlinedIconChip(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    text: String? = null,
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = if (text.isNullOrBlank()) 8.dp else 12.dp,
            vertical = 6.dp,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = if (text.isNullOrBlank()) Modifier.size(32.dp) else Modifier,
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!text.isNullOrBlank()) {
            Spacer(Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AboutBadge(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
            )
            .padding(
                horizontal = 6.dp,
                vertical = 2.dp,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current
    val nightlyBuildHash = currentBuildHash

    val developer = TeamMember(
        avatarUrl = "https://github.com/BludAdit3220.png",
        name = "BludAdit3220",
        position = "Developer",
        profileUrl = "https://github.com/BludAdit3220",
        github = "https://github.com/BludAdit3220"
    )

    // Reset the shared TopAppBar scroll state so the "About" title is always
    // fully visible when this screen is opened, regardless of scroll position
    // on previously visited screens.
    LaunchedEffect(Unit) {
        scrollBehavior.state.resetHeightOffset()
        scrollBehavior.state.contentOffset = 0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                windowInsets = WindowInsets.statusBars,
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
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(innerPadding)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            Image(
                painter = painterResource(R.drawable.about_splash),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { },
            )

            Row(
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "PulseMusic",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AboutBadge(text = BuildConfig.VERSION_NAME)

                nightlyBuildHash?.let {
                    Spacer(Modifier.width(4.dp))
                    AboutBadge(text = it)
                }

                Spacer(Modifier.width(4.dp))

                if (BuildConfig.DEBUG) {
                    AboutBadge(text = "DEBUG")
                } else {
                    AboutBadge(text = BuildConfig.ARCHITECTURE.uppercase())
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionHeader(
                title = "Developer",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            DeveloperCard(
                member = developer,
                onOpenUri = uriHandler::openUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun DeveloperCard(
    member: TeamMember,
    onOpenUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = member.avatarUrl,
                contentDescription = member.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = member.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = member.position,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                member.github?.let { url ->
                    OutlinedIconChip(
                        iconRes = R.drawable.github,
                        contentDescription = "GitHub",
                        onClick = { onOpenUri(url) },
                    )
                }

                member.website?.takeIf { it.isNotBlank() }?.let { url ->
                    OutlinedIconChip(
                        iconRes = R.drawable.website,
                        contentDescription = "Website",
                        onClick = { onOpenUri(url) },
                    )
                }

                member.discord?.let { url ->
                    OutlinedIconChip(
                        iconRes = R.drawable.alternate_email,
                        contentDescription = "Discord",
                        onClick = { onOpenUri(url) },
                    )
                }
            }
        }
    }
}
